package com.goldennode.api.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.StringUtils;

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
            LOGGER.debug("unlocked > " + lockName + " processId > " + StringUtils.shortProcessId(processId));
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
            LOGGER.debug("unlocked > " + lockName + " processId > " + StringUtils.shortProcessId(processId));
        } else {
            throw new LockNotFoundException(lockName);
        }
    }

    @Override
    public synchronized void createLock(String lockName, long lockTimeoutInMs) {
        if (!locks.containsKey(lockName)) {
            DistributedReentrantReadWriteLock lock = new DistributedReentrantReadWriteLock(lockName, lockTimeoutInMs);
            locks.put(lockName + "_w", lock.writeLock());
            locks.put(lockName + "_r", lock.readLock());
        } else {
            throw new LockException("lock has already been created > " + lockName);
        }
    }

    @Override
    public synchronized void deleteLock(String lockName) {
        if (locks.remove(lockName + "_w") == null || locks.remove(lockName + "_r") == null)
            throw new LockNotFoundException(lockName);
    }

    @Override
    public void readLock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        Lock lockBag = locks.get(lockName + "_r");
        if (lockBag != null) {
            LOGGER.debug("will lock > " + lockName + " processId > " + StringUtils.shortProcessId(processId));
            lockBag.lock();
            LOGGER.debug("locked > " + lockName + " processId > " + StringUtils.shortProcessId(processId));
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
            LOGGER.debug("will lock > " + lockName + " processId > " + StringUtils.shortProcessId(processId));
            lockBag.lock();
            LOGGER.debug("locked > " + lockName + " processId > " + StringUtils.shortProcessId(processId));
        } else {
            LOGGER.warn("lock not found > " + lockName);
            throw new LockNotFoundException(lockName);
        }
    }
}
