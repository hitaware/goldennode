package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

public class DistributedReentrantLock implements Lock {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedReentrantLock.class);
	private final Sync sync;

	private static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = -5179523762034025860L;
		private String processId;

		final void lock() {
			if (compareAndSetState(0, 1)) {
				setOwnerThread(LockContext.threadProcessId.get());
			} else {
				acquire(1);
			}
		}

		private void setOwnerThread(String processId) {
			this.processId = processId;
		}

		private String getOwnerThread() {
			return processId;
		}

		@Override
		protected final boolean tryAcquire(int acquires) {
			return nonfairTryAcquire(acquires);
		}

		final boolean nonfairTryAcquire(int acquires) {
			// final Thread current = Thread.currentThread();
			String current = LockContext.threadProcessId.get();
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

		final ConditionObject newCondition() {
			return new ConditionObject();
		}
	}

	public DistributedReentrantLock() {
		sync = new Sync();
	}

	@Override
	public void lock() {
		sync.lock();
	}

	@Override
	public void unlock() {
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		Condition c = sync.newCondition();
		return c;
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		return sync.nonfairTryAcquire(1);
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}
}
