package com.goldennode.api.core;

public interface LockService {
	public void acquireLock(String lockName, String processId);

	public void releaseLock(String lockName, String processId);

	public void createLock(String lockName);

	public void deleteLock(String lockName);

	public void acquireLock(String processId);

	public void releaseLock(String processId);
}
