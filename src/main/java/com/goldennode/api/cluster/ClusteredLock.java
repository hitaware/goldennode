package com.goldennode.api.cluster;

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
        throw new UnsupportedOperationException();
    }

}
