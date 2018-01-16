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
    public void unlock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lb = locks.get(lockName);
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
        LockContext.threadProcessId.set("self");
        Lock lb = locks.get(lockName);
        if (lb != null) {
            LOGGER.debug("will lock(trylock) > " + lockName + " processId > " + "self");
            boolean result = lb.tryLock();
            if (result) {
                locks.remove(lockName);
                lb.unlock();
                LOGGER.debug("unlocked > " + lockName + " processId > " + "self");
                LOGGER.debug("removed lock > " + lockName);
            } else {
                LOGGER.warn("can't delete Lock as it can't be acquired > " + lockName);
                throw new LockException("can't delete Lock as it can't be acquired > " + lockName);
            }
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public void lockInterruptibly(String lockName, String processId) throws InterruptedException {
        LockContext.threadProcessId.set(processId);
        Lock lb = locks.get(lockName);
        if (lb != null) {
            LOGGER.debug("will lock(lockInterruptibly) > " + lockName + " processId > " + processId);
            lb.lockInterruptibly();
            LOGGER.debug("acquired lock > " + lockName + " processId > " + processId);
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public boolean tryLock(String lockName, String processId, long timeout) {
        LockContext.threadProcessId.set(processId);
        Lock lb = locks.get(lockName);
        if (lb != null) {
            LOGGER.debug("will lock(trylock) > " + lockName + " processId > " + processId);
            boolean result = lb.tryLock();
            if (result) {
                LOGGER.debug("acquired lock > " + lockName + " processId > " + processId);
            } else {
                LOGGER.debug("failed to acquire lock > " + lockName + " processId > " + processId);
            }
            return result;
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public boolean tryLock(String lockName, String processId, long timeout, TimeUnit unit)
            throws InterruptedException {
        LockContext.threadProcessId.set(processId);
        Lock lb = locks.get(lockName);
        if (lb != null) {
            LOGGER.debug("will lock(trylock) > " + lockName + " processId > " + processId);
            boolean result = lb.tryLock(timeout, unit);
            if (result) {
                LOGGER.debug("acquired lock > " + lockName + " processId > " + processId);
            } else {
                LOGGER.debug("failed to acquire lock > " + lockName + " processId > " + processId);
            }
            return result;
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public void lock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lockBag = locks.get(lockName);
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
