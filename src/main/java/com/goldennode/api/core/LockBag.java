package com.goldennode.api.core;

import java.util.Date;
import java.util.concurrent.locks.Lock;

public class LockBag {
	private Lock lock;
	private Date lastAcquire;
	private Date creationDate;
	private long timeoutInMs;
	private String lockedProcessId;

	public LockBag(Lock lock) {
		this.lock = lock;
		creationDate = new Date();
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public Date getLastAcquire() {
		return lastAcquire;
	}

	public void setLastAcquire(Date lastAcquire) {
		this.lastAcquire = lastAcquire;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public long getTimeoutInMs() {
		return timeoutInMs;
	}

	public void setTimeoutInMs(long timeoutInMs) {
		this.timeoutInMs = timeoutInMs;
	}

	public String getLockedProcessId() {
		return lockedProcessId;
	}

	public void setLockedProcessId(String lockedProcessId) {
		this.lockedProcessId = lockedProcessId;
	}
}
