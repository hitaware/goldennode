package com.goldennode.api.cluster;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

public class ClusteredLock extends ClusteredObject implements Lock {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredLock.class);
	private static final long serialVersionUID = 1L;

	public ClusteredLock() {
		super();
	}

	public ClusteredLock(String publicName) {
		super(publicName);
	}

	@Override
	public void lock() {
		try {
			getCluster().lock(this);
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unlock() {
		try {
			getCluster().unlock(this);
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	// TODO test all methods
	@Override
	public void lockInterruptibly() throws InterruptedException {
		try {
			getCluster().lockInterruptibly(this);
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean tryLock() {
		try {
			return getCluster().tryLock(this);
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		try {
			return getCluster().tryLock(this, timeout, unit);
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Condition newCondition() {
		int id;
		try {
			id = getCluster().newCondition(this);
			return new ConditionImpl(id);
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	class ConditionImpl implements Condition {
		private int id;

		public ConditionImpl(int id) {
			this.id = id;
		}

		@Override
		public void await() throws InterruptedException {
			try {
				getCluster().await(id);
			} catch (ClusterException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void awaitUninterruptibly() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long awaitNanos(long nanosTimeout) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean await(long time, TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean awaitUntil(Date deadline) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void signal() {
			try {
				getCluster().signal(id);
			} catch (ClusterException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void signalAll() {
			try {
				getCluster().signalAll(id);
			} catch (ClusterException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
