package com.goldennode.api.goldennodegrid;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.GridException;

public class DistributedLock extends DistributedObject implements Lock {
    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedLock.class);

    public DistributedLock() {
        super();
    }

    public DistributedLock(String publicName) {
        super(publicName);
    }

    @Override
    public void lock() {
        try {
            getGrid().writeLock(this);
        } catch (GridException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unlock() {
        try {
            getGrid().unlockWriteLock(this);
        } catch (GridException e) {
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
