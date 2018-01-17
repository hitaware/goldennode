package com.goldennode.api.cluster;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

public class ClusteredLock extends ClusteredObject implements Lock {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredLock.class);

    public ClusteredLock() {
        super();
    }

    public ClusteredLock(String publicName) {
        super(publicName);
    }

    @Override
    public void lock() {
        try {
            getCluster().writeLock(this);
        } catch (ClusterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unlock() {
        try {
            getCluster().unlockWriteLock(this);
        } catch (ClusterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
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
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
