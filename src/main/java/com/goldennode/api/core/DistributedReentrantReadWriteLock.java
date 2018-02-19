package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class DistributedReentrantReadWriteLock implements ReadWriteLock {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedReentrantReadWriteLock.class);
    /** Inner class providing readlock */
    private final DistributedReentrantReadWriteLock.ReadLock readerLock;
    /** Inner class providing writelock */
    private final DistributedReentrantReadWriteLock.WriteLock writerLock;
    /** Performs all synchronization mechanics */
    final Sync sync;

    /**
     * Creates a new {@code ReentrantReadWriteLock} with default (nonfair)
     * ordering properties.
     */
    public DistributedReentrantReadWriteLock(String lockName, long lockTimeoutInMs) {
        sync = new FairSync();
        readerLock = new ReadLock(this, lockName, lockTimeoutInMs);
        writerLock = new WriteLock(this, lockName, lockTimeoutInMs);
    }

    public DistributedReentrantReadWriteLock.WriteLock writeLock() {
        return writerLock;
    }

    public DistributedReentrantReadWriteLock.ReadLock readLock() {
        return readerLock;
    }

    /**
     * Synchronization implementation for ReentrantReadWriteLock. Subclassed
     * into fair and nonfair versions.
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 6317671515068378041L;
        /*
         * Read vs write count extraction constants and functions.
         * Lock state is logically divided into two unsigned shorts:
         * The lower one representing the exclusive (writer) lock hold count,
         * and the upper the shared (reader) hold count.
         */
        static final int SHARED_SHIFT = 16;
        static final int SHARED_UNIT = (1 << SHARED_SHIFT);
        static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

        /** Returns the number of shared holds represented in count */
        static int sharedCount(int c) {
            return c >>> SHARED_SHIFT;
        }

        /** Returns the number of exclusive holds represented in count */
        static int exclusiveCount(int c) {
            return c & EXCLUSIVE_MASK;
        }

        private String processId;

        void setOwnerThread(String processId) {
            this.processId = processId;
        }

        String getOwnerThread() {
            return processId;
        }

        /**
         * A counter for per-thread read hold counts. Maintained as a
         * ThreadLocal; cached in cachedHoldCounter
         */
        static final class HoldCounter {
            int count = 0;
            // Use id, not reference, to avoid garbage retention
            final String tid = LockContext.threadProcessId.get();
        }

        /**
         * ThreadLocal subclass. Easiest to explicitly define for sake of
         * deserialization mechanics.
         */
        static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        private transient ThreadLocalHoldCounter readHolds;
        private transient HoldCounter cachedHoldCounter;
        private transient String firstReader = null;
        private transient int firstReaderHoldCount;

        Sync() {
            readHolds = new ThreadLocalHoldCounter();
            setState(getState()); // ensures visibility of readHolds
        }

        abstract boolean readerShouldBlock();

        abstract boolean writerShouldBlock();

        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            if (free)
                setOwnerThread(null);
            setState(nextc);
            return free;
        }

        protected final boolean tryAcquire(int acquires) {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            String current = LockContext.threadProcessId.get();
            int c = getState();
            int w = exclusiveCount(c);
            if (c != 0) {
                // (Note: if c != 0 and w == 0 then shared count != 0)
                if (w == 0 || !current.equals(getOwnerThread()))
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // Reentrant acquire
                setState(c + acquires);
                return true;
            }
            if (writerShouldBlock() || !compareAndSetState(c, c + acquires))
                return false;
            setOwnerThread(current);
            return true;
        }

        protected final boolean tryReleaseShared(int unused) {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            String current = LockContext.threadProcessId.get();
            if (firstReader.equals(current)) {
                // assert firstReaderHoldCount > 0;
                if (firstReaderHoldCount == 1)
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || !rh.tid.equals(current))
                    rh = readHolds.get();
                int count = rh.count;
                if (count <= 1) {
                    readHolds.remove();
                    if (count <= 0)
                        throw unmatchedUnlockException();
                }
                --rh.count;
            }
            for (;;) {
                int c = getState();
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(c, nextc))
                    // Releasing the read lock has no effect on readers,
                    // but it may allow waiting writers to proceed if
                    // both read and write locks are now free.
                    return nextc == 0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
        }

        protected final int tryAcquireShared(int unused) {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            String current = LockContext.threadProcessId.get();
            int c = getState();
            if (exclusiveCount(c) != 0 && !getOwnerThread().equals(current))
                return -1;
            int r = sharedCount(c);
            if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
                if (r == 0) {
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader.equals(current)) {
                    firstReaderHoldCount++;
                } else {
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || !rh.tid.equals(current))
                        cachedHoldCounter = rh = readHolds.get();
                    else if (rh.count == 0)
                        readHolds.set(rh);
                    rh.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        final int fullTryAcquireShared(String current) {
            HoldCounter rh = null;
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0) {
                    if (!getOwnerThread().equals(current))
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                } else if (readerShouldBlock()) {
                    // Make sure we're not acquiring read lock reentrantly
                    if (firstReader.equals(current)) {
                        // assert firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || !rh.tid.equals(current)) {
                                rh = readHolds.get();
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0)
                            return -1;
                    }
                }
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (sharedCount(c) == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader.equals(current)) {
                        firstReaderHoldCount++;
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || !rh.tid.equals(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // cache for release
                    }
                    return 1;
                }
            }
        }

        final boolean tryWriteLock() {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            String current = LockContext.threadProcessId.get();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);
                if (w == 0 || !current.equals(getOwnerThread()))
                    return false;
                if (w == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
            }
            if (!compareAndSetState(c, c + 1))
                return false;
            setOwnerThread(current);
            return true;
        }

        final boolean tryReadLock() {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            String current = LockContext.threadProcessId.get();
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0 && !getOwnerThread().equals(current))
                    return false;
                int r = sharedCount(c);
                if (r == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (r == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader.equals(current)) {
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        if (rh == null || !rh.tid.equals(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getOwnerThread().equals(LockContext.threadProcessId.get());
        }

        // Methods relayed to outer class
        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final String getOwner() {
            // Must read state before owner to ensure memory consistency
            return ((exclusiveCount(getState()) == 0) ? null : getOwnerThread());
        }

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0)
                return 0;
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            String current = LockContext.threadProcessId.get();
            if (firstReader.equals(current))
                return firstReaderHoldCount;
            HoldCounter rh = cachedHoldCounter;
            if (rh != null && rh.tid.equals(current))
                return rh.count;
            int count = readHolds.get().count;
            if (count == 0)
                readHolds.remove();
            return count;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            readHolds = new ThreadLocalHoldCounter();
            setState(0); // reset to unlocked state
        }

        final int getCount() {
            return getState();
        }
    }

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;

        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }

        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;
        Timer lockReleaser;
        long lockTimeoutInMs;
        String lockName;

        protected ReadLock(DistributedReentrantReadWriteLock lock, String lockName, long lockTimeoutInMs) {
            sync = lock.sync;
            this.lockName = lockName + "_r";
            this.lockTimeoutInMs = lockTimeoutInMs;
        }

        public void lock() {
            sync.acquireShared(1);
            scheduleLockReleaser();
        }

        void scheduleLockReleaser() {
            lockReleaser = new Timer();
            lockReleaser.schedule(new TimerTask() {
                @Override
                public void run() {
                    String lockingProcessId = sync.getOwnerThread();
                    LockContext.threadProcessId.set(lockingProcessId);
                    LOGGER.debug("auto-released lock > " + lockName + " processId > " + lockingProcessId);
                    unlock();
                }
            }, lockTimeoutInMs);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
            scheduleLockReleaser();
        }

        public boolean tryLock() {
            boolean result = sync.tryReadLock();
            if (result) {
                scheduleLockReleaser();
            }
            return result;
        }

        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            boolean result = sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
            if (result) {
                scheduleLockReleaser();
            }
            return result;
        }

        public void unlock() {
            if (lockReleaser != null) {
                lockReleaser.cancel();
                lockReleaser = null;
            }
            sync.releaseShared(1);
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() + "[Read locks = " + r + "]";
        }

        public Object getLockTimeoutInMs() {
            return lockTimeoutInMs;
        }

        public String getLockName() {
            return lockName;
        }
    }

    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;
        Timer lockReleaser;
        long lockTimeoutInMs;
        String lockName;

        protected WriteLock(DistributedReentrantReadWriteLock lock, String lockName, long lockTimeoutInMs) {
            sync = lock.sync;
            this.lockName = lockName + "_w";
            this.lockTimeoutInMs = lockTimeoutInMs;
        }

        public void lock() {
            sync.acquire(1);
            scheduleLockReleaser();
        }

        void scheduleLockReleaser() {
            lockReleaser = new Timer();
            lockReleaser.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String lockingProcessId = sync.getOwnerThread();
                        LockContext.threadProcessId.set(lockingProcessId);
                        LOGGER.debug("auto-released lock > " + lockName + " processId > " + lockingProcessId);
                        unlock();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }, lockTimeoutInMs);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
            scheduleLockReleaser();
        }

        public boolean tryLock() {
            boolean result = sync.tryWriteLock();
            if (result) {
                scheduleLockReleaser();
            }
            return result;
        }

        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            boolean result = sync.tryAcquireNanos(1, unit.toNanos(timeout));
            if (result) {
                scheduleLockReleaser();
            }
            return result;
        }

        public void unlock() {
            if (lockReleaser != null) {
                lockReleaser.cancel();
                lockReleaser = null;
            }
            sync.release(1);
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            String o = sync.getOwner();
            return super.toString() + ((o == null) ? "[Unlocked]" : "[Locked by thread " + o + "]");
        }

        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }

        public Object getLockTimeoutInMs() {
            return lockTimeoutInMs;
        }

        public String getLockName() {
            return lockName;
        }
    }

    protected String getOwner() {
        return sync.getOwner();
    }

    public int getReadLockCount() {
        return sync.getReadLockCount();
    }

    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }

    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }

    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }

    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);
        return super.toString() + "[Write locks = " + w + ", Read locks = " + r + "]";
    }
}
