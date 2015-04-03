package com.goldennode.api.cluster;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

public class ClusteredLock extends ClusteredObject implements
		java.io.Serializable, Lock {
	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(ClusteredLock.class);

	private static final long serialVersionUID = 1L;

	public ClusteredLock() {
		super();
	}

	public ClusteredLock(String publicName, String ownerId) {
		super(publicName, ownerId);
	}

	public ClusteredLock(String publicName, String ownerId, Cluster cluster) {
		super(publicName, ownerId, cluster);
	}

	@Override
	public void lock() {

		getCluster().lock(this);

	}

	@Override
	public void unlock() {

		getCluster().unlock(this);

	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean tryLock() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}
}
