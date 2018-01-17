package com.goldennode.api.core;

public interface LockService {
    
    void readLock(String lockName, String processId);
    
    void writeLock(String lockName, String processId);

    void unlockReadLock(String lockName, String processId);
    
    void unlockWriteLock(String lockName, String processId);

    void createLock(String lockName, long lockTimeoutInMs);

    void deleteLock(String lockName);

}
