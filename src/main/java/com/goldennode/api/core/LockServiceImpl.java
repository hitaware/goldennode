package com.goldennode.api.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public class LockServiceImpl implements LockService {
	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(LockServiceImpl.class);

	private Map<String, LockObject> locks = new HashMap<String, LockObject>();

	public static ThreadLocal<String> threadLockName = new ThreadLocal<String>();

	@Override
	public void acquireLock(String lockName, String processId) {
		threadLockName.set(lockName);
		if (locks.containsKey(lockName)) {
			locks.get(lockName).acquireLock(processId);
		} else {
			LOGGER.warn("lock not found " + lockName);
		}

	}

	@Override
	public void releaseLock(String lockName, String processId) {
		threadLockName.set(lockName);
		if (locks.containsKey(lockName)) {
			locks.get(lockName).releaseLock(processId);

		} else {
			LOGGER.warn("lock not found " + lockName);
		}
	}

	@Override
	public synchronized void createLock(String lockName) {
		if (!locks.containsKey(lockName)) {
			LockObject lock = new LockObject();
			locks.put(lockName, lock);
		} else {
			LOGGER.warn("lock has already been initialized." + lockName);
		}
	}

	@Override
	public synchronized void deleteLock(String lockName) {
		if (locks.containsKey(lockName)) {
			locks.remove(lockName);
		} else {
			LOGGER.warn("lock not found " + lockName);
		}

	}

	@Override
	public void acquireLock(String processId) {
		acquireLock("$application", processId);

	}

	@Override
	public void releaseLock(String processId) {
		releaseLock("$application", processId);

	}

}
