package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;

public interface LockService {
	public void lock(String lockName, String processId, long timeout);

	public void unlock(String lockName, String processId);

	public void createLock(String lockName);

	public void deleteLock(String lockName);

	public void await(int conditionId, String processId) throws InterruptedException;

	public void signal(int conditionId, String processId);

	public void signalAll(int conditionId, String processId);

	public int newCondition(String lockName);

	public void lockInterruptibly(String lockName, String processId, long timeout) throws InterruptedException;

	public boolean tryLock(String lockName, String processId, long timeout);

	public boolean tryLock(String lockName, String processId, long timeout, TimeUnit unit, long lockTimeout)
			throws InterruptedException;
}
