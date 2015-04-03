package com.goldennode.api.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.slf4j.LoggerFactory;

public class LockServiceImpl implements LockService {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LockServiceImpl.class);
	private Map<String, LockBag> locks = new ConcurrentHashMap<String, LockBag>();
	private Map<Integer, Condition> conditions = new HashMap<Integer, Condition>();
	private Timer lockReleaser = new Timer();

	public LockServiceImpl() {
		lockReleaser.schedule(new TimerTask() {
			@Override
			public void run() {
				for (Entry<String, LockBag> entry : locks.entrySet()) {
					String lockName = entry.getKey();
					LockBag lb = entry.getValue();
					if (lb.getLockedProcessId() != null) {
						Date lastAcquire = lb.getLastAcquire();
						if (System.currentTimeMillis() - lastAcquire.getTime() > lb.getTimeout()) {
							unlock(lockName, lb.getLockedProcessId());
						}
					}
				}
			}
		}, 0, 10);
	}

	@Override
	public void lock(String lockName, String processId, long timeout) {
		LockContext.threadProcessId.set(processId);
		if (locks.containsKey(lockName)) {
			LOGGER.debug("Locking " + lockName + " processId=" + processId);
			locks.get(lockName).getLock().lock();
			locks.get(lockName).setLastAcquire(new Date());
			locks.get(lockName).setLockedProcessId(processId);
			locks.get(lockName).setTimeout(timeout);
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}

	@Override
	public void unlock(String lockName, String processId) {
		LockContext.threadProcessId.set(processId);
		if (lockName == null) {
			LOGGER.warn("lock not acquired for processId > " + processId);
			throw new LockException("lock not acquired for processId > " + processId);
		}
		if (locks.containsKey(lockName)) {
			LOGGER.debug("Releasing " + lockName + " processId=" + processId);
			locks.get(lockName).getLock().unlock();
			locks.get(lockName).setLastAcquire(null);
			locks.get(lockName).setLockedProcessId(null);
			locks.get(lockName).setTimeout(0);
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}

	@Override
	public synchronized void createLock(String lockName) {
		if (!locks.containsKey(lockName)) {
			locks.put(lockName, new LockBag(new DistributedReentrantLock()));
		} else {
			LOGGER.warn("lock has already been created > " + lockName);
			throw new LockException("lock has already been created > " + lockName);
		}
	}

	@Override
	public synchronized void deleteLock(String lockName) {
		if (locks.containsKey(lockName)) {
			locks.remove(lockName);
			// TODO implement conditions.remove();
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}

	@Override
	public void await(int conditionId, String processId) throws InterruptedException {
		LockContext.threadProcessId.set(processId);
		conditions.get(conditionId).await();
	}

	@Override
	public void signal(int conditionId, String processId) {
		LockContext.threadProcessId.set(processId);
		conditions.get(conditionId).signal();
	}

	@Override
	public void signalAll(int conditionId, String processId) {
		LockContext.threadProcessId.set(processId);
		conditions.get(conditionId).signalAll();
	}

	@Override
	public int newCondition(String lockName) {
		if (locks.containsKey(lockName)) {
			Condition c = locks.get(lockName).getLock().newCondition();
			conditions.put(c.hashCode(), c);
			return c.hashCode();
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}

	@Override
	public void lockInterruptibly(String lockName, String processId, long timeout) throws InterruptedException {
		LockContext.threadProcessId.set(processId);
		if (locks.containsKey(lockName)) {
			locks.get(lockName).getLock().lockInterruptibly();
			locks.get(lockName).setLastAcquire(new Date());
			locks.get(lockName).setLockedProcessId(processId);
			locks.get(lockName).setTimeout(timeout);
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}

	@Override
	public boolean tryLock(String lockName, String processId, long timeout) {
		LockContext.threadProcessId.set(processId);
		if (locks.containsKey(lockName)) {
			boolean result = locks.get(lockName).getLock().tryLock();
			if (result) {
				locks.get(lockName).setLastAcquire(new Date());
				locks.get(lockName).setLockedProcessId(processId);
				locks.get(lockName).setTimeout(timeout);
			}
			return result;
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}

	@Override
	public boolean tryLock(String lockName, String processId, long timeout, TimeUnit unit, long lockTimeout)
			throws InterruptedException {
		LockContext.threadProcessId.set(processId);
		if (locks.containsKey(lockName)) {
			boolean result = locks.get(lockName).getLock().tryLock(timeout, unit);
			if (result) {
				locks.get(lockName).setLastAcquire(new Date());
				locks.get(lockName).setLockedProcessId(processId);
				locks.get(lockName).setTimeout(lockTimeout);
			}
			return result;
		} else {
			LOGGER.warn("lock not found > " + lockName);
			throw new LockException("lock not found > " + lockName);
		}
	}
}
