package com.goldennode.api.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.slf4j.LoggerFactory;

public class LockServiceImpl implements LockService {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LockServiceImpl.class);
    private Map<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    public LockServiceImpl() {
    }

    @Override
    public void unlockReadLock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lb = locks.get(lockName + "_r");
        if (lb != null) {
            lb.unlock();
            LOGGER.debug("unlocked > " + lockName + " processId > " + processId);
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public void unlockWriteLock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lb = locks.get(lockName + "_w");
        if (lb != null) {
            lb.unlock();
            LOGGER.debug("unlocked > " + lockName + " processId > " + processId);
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public synchronized void createLock(String lockName, long lockTimeoutInMs) {
        if (!locks.containsKey(lockName)) {
            DistributedReentrantReadWriteLock lock = new DistributedReentrantReadWriteLock(lockName, lockTimeoutInMs);
            locks.put(lock.writeLock().lockName, lock.writeLock());
            locks.put(lock.readLock().lockName, lock.readLock());
        } else {
            throw new LockException("lock has already been created > " + lockName);
        }
    }

    @Override
    public synchronized void deleteLock(String lockName) {
        Lock lock = locks.remove(lockName + "_r");
        locks.remove(lockName + "_w");
        if (lock == null)
            throw new LockNotFoundException(lockName);
    }

    @Override
    public void readLock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lockBag = locks.get(lockName + "_r");
        if (lockBag != null) {
            LOGGER.debug("will lock > " + lockName + " processId > " + processId);
            lockBag.lock();
            LOGGER.debug("locked > " + lockName + " processId > " + processId);
        } else {
            LOGGER.warn("lock not found > " + lockName);
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public void writeLock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lockBag = locks.get(lockName + "_w");
        if (lockBag != null) {
            LOGGER.debug("will lock > " + lockName + " processId > " + processId);
            lockBag.lock();
            LOGGER.debug("locked > " + lockName + " processId > " + processId);
        } else {
            LOGGER.warn("lock not found > " + lockName);
            throw new LockNotFoundException(lockName);
        }
    }
}
