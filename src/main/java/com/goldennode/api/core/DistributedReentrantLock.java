package com.goldennode.api.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

public class DistributedReentrantLock implements Lock {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedReentrantLock.class);
    final Sync sync;
    long lockTimeoutInMs;
    Timer lockReleaser;
    String lockName;

    public long getLockTimeoutInMs() {
        return lockTimeoutInMs;
    }

    public String getLockName() {
        return lockName;
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;
        private String processId;

        final void lock() {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            if (compareAndSetState(0, 1)) {
                setOwnerThread(LockContext.threadProcessId.get());
            } else {
                acquire(1);
            }
        }

        void setOwnerThread(String processId) {
            this.processId = processId;
        }

        String getOwnerThread() {
            return processId;
        }

        @Override
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }

        final boolean nonfairTryAcquire(int acquires) {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            final String current = LockContext.threadProcessId.get();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setOwnerThread(current);
                    return true;
                }
            } else if (current.equals(getOwnerThread())) {
                int nextc = c + acquires;
                if (nextc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }

        @Override
        protected final boolean tryRelease(int releases) {
            if (LockContext.threadProcessId.get() == null) {
                throw new RuntimeException("ThreadLocal variable LockContext.threadProcessId is not set");
            }
            int c = getState() - releases;
            if (!LockContext.threadProcessId.get().equals(getOwnerThread())) {
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (c == 0) {
                free = true;
                setOwnerThread(null);
            }
            setState(c);
            return free;
        }

        @Override
        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getOwnerThread().equals(LockContext.threadProcessId.get());
        }
    }

    public DistributedReentrantLock(String lockName, long lockTimeoutInMs) {
        sync = new Sync();
        this.lockTimeoutInMs = lockTimeoutInMs;
        this.lockName = lockName;
    }

    @Override
    public void lock() {
        sync.lock();
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

    @Override
    public void unlock() {
        if (lockReleaser != null) {
            lockReleaser.cancel();
            lockReleaser = null;
        }
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
        scheduleLockReleaser();
    }

    @Override
    public boolean tryLock() {
        boolean result = sync.nonfairTryAcquire(1);
        if (result) {
            scheduleLockReleaser();
        }
        return result;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (result) {
            scheduleLockReleaser();
        }
        return result;
    }
}
