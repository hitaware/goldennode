package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;

public interface LockService {
	void lock(String lockName, String processId, long timeout);

	void unlock(String lockName, String processId);

	void createLock(String lockName);

	void deleteLock(String lockName);

	void await(int conditionId, String processId) throws InterruptedException;

	void signal(int conditionId, String processId);

	void signalAll(int conditionId, String processId);

	int newCondition(String lockName);

	void lockInterruptibly(String lockName, String processId, long timeout) throws InterruptedException;

	boolean tryLock(String lockName, String processId, long timeout);

	boolean tryLock(String lockName, String processId, long timeout, TimeUnit unit, long lockTimeout)
			throws InterruptedException;
}
