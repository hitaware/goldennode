package com.goldennode.api.core;

import org.slf4j.LoggerFactory;

public class LockObject {

	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(LockServiceImpl.class);

	boolean isLocked = false;
	String lockedByProcessId = null;
	int lockedCount = 0;

	public synchronized boolean acquireLock(String processId) {

		while (isLocked && !lockedByProcessId.equals(processId)) {
			try {
				LOGGER.debug("Will wait to acquire lock. Name > "
						+ LockServiceImpl.threadLockName.get()
						+ " processId > " + processId);
				wait();
				LOGGER.debug("Been notified. Name > "
						+ LockServiceImpl.threadLockName.get()
						+ " processId > " + processId);
			} catch (InterruptedException e) {
				// Uninterruptable
			}
		}
		LOGGER.debug("Lock acquired. Name > "
				+ LockServiceImpl.threadLockName.get() + " processId > "
				+ processId);
		isLocked = true;
		lockedCount++;
		lockedByProcessId = processId;
		return true;
	}

	public synchronized boolean releaseLock(String processId) {
		if (lockedByProcessId != null && lockedByProcessId.equals(processId)) {
			lockedCount--;
			if (lockedCount == 0) {
				isLocked = false;
				lockedByProcessId = null;
				notify();
				LOGGER.debug("Released lock. Name >  "
						+ LockServiceImpl.threadLockName.get()
						+ " processId > " + processId);
				return true;
			}
		}
		return false;
	}

}
