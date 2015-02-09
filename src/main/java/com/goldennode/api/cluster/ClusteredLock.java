package com.goldennode.api.cluster;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ClusteredLock extends ClusteredObject implements Lock,
java.io.Serializable {
	private static final long serialVersionUID = 7373984872572414699L;
	private final Sync sync;

	private static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = -5179523762034025860L;

		final void lock() {
			if (compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
			} else {
				acquire(1);
			}
		}

		@Override
		protected final boolean tryAcquire(int acquires) {
			return nonfairTryAcquire(acquires);
		}

		final boolean nonfairTryAcquire(int acquires) {
			final Thread current = Thread.currentThread();
			int c = getState();
			if (c == 0) {
				if (compareAndSetState(0, acquires)) {
					setExclusiveOwnerThread(current);
					return true;
				}
			} else if (current == getExclusiveOwnerThread()) {
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
			if (Thread.currentThread() != getExclusiveOwnerThread()) {
				throw new IllegalMonitorStateException();
			}
			boolean free = false;
			if (c == 0) {
				free = true;
				setExclusiveOwnerThread(null);
			}
			setState(c);
			return free;
		}

		@Override
		protected final boolean isHeldExclusively() {
			// While we must in general read state before owner,
			// we don't need to do so to check if current thread is owner
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		final ConditionObject newCondition() {
			return new ConditionObject();
		}

		final Thread getOwner() {
			return getState() == 0 ? null : getExclusiveOwnerThread();
		}

	}

	public ClusteredLock() {
		super();
		sync = new Sync();
	}

	public ClusteredLock(String publicName, String ownerId)
			throws ClusterException {
		super(publicName, ownerId);
		sync = new Sync();
	}

	@Override
	public void lock() {
		sendLockRequestToPeers();
		sync.lock();
	}

	private void sendLockRequestToPeers() {
		// TODO implement

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
	public boolean tryLock(long timeout, TimeUnit unit)
			throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}

	@Override
	public void unlock() {
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		return sync.newCondition();
	}

	@Override
	public String toString() {
		Thread o = sync.getOwner();
		return super.toString()
				+ (o == null ? "[Unlocked]" : "[Locked by thread "
						+ o.getName() + "]");
	}

}