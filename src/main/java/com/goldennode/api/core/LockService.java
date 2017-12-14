package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;

public interface LockService {
    void lock(String lockName, String processId);

    void unlock(String lockName, String processId);

    void createLock(String lockName, long lockTimeoutInMs);

    void deleteLock(String lockName);

    void lockInterruptibly(String lockName, String processId) throws InterruptedException;

    boolean tryLock(String lockName, String processId, long timeout);

    boolean tryLock(String lockName, String processId, long timeout, TimeUnit unit) throws InterruptedException;
}
