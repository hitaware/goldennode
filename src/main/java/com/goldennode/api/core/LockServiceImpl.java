package com.goldennode.api.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

public class LockServiceImpl implements LockService {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LockServiceImpl.class);
    private Map<String, LockBag> locks = new ConcurrentHashMap<String, LockBag>();
    private Map<Integer, Condition> conditions = new HashMap<Integer, Condition>();
    private Map<String, List<Integer>> conditionsLocks = new HashMap<String, List<Integer>>();
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
                        // TODO sync lockbag
                        if (System.currentTimeMillis() - lastAcquire.getTime() > lb.getTimeoutInMs()) {
                            LOGGER.debug(
                                    "will auto-release lock > " + lockName + " processId > " + lb.getLockedProcessId());
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
            LOGGER.debug("will lock > " + lockName + " processId > " + processId);
            LockBag lockBag = locks.get(lockName);
            lockBag.getLock().lock();
            LOGGER.debug("locked > " + lockName + " processId > " + processId);
            lockBag.setLastAcquire(new Date());
            lockBag.setLockedProcessId(processId);
            lockBag.setTimeoutInMs(timeout);
        } else {
            LOGGER.warn("lock not found > " + lockName);
            throw new LockException("lock not found > " + lockName);
        }
    }

    @Override
    public void unlock(String lockName, String processId) {
        LockContext.threadProcessId.set(processId);
        if (lockName == null) {
            throw new LockException("lock not acquired for processId > " + processId);
        }
        if (locks.containsKey(lockName)) {
            LOGGER.debug("will unlock > " + lockName + " processId > " + processId);
            locks.get(lockName).getLock().unlock();
            LOGGER.debug("unlocked > " + lockName + " processId > " + processId);
            locks.get(lockName).setLastAcquire(null);// FIXME null pointer when
                                                     // deleteLock is called
            locks.get(lockName).setLockedProcessId(null);
            locks.get(lockName).setTimeoutInMs(0);
        } else {
            throw new LockException("lock not found > " + lockName);
        }
    }

    @Override
    public synchronized void createLock(String lockName) {
        if (!locks.containsKey(lockName)) {
            locks.put(lockName, new LockBag(new DistributedReentrantLock()));
        } else {
            throw new LockException("lock has already been created > " + lockName);
        }
    }

    @Override
    public void deleteLock(String lockName) {
        if (locks.containsKey(lockName)) {
            Lock lock = locks.get(lockName).getLock();
            lock.lock();
            locks.remove(lockName);
            
            //TODO maybe sync
            List<Integer> lst = conditionsLocks.get(lockName);
            if (lst != null) {
                for (Integer ingr : lst) {
                    conditions.remove(ingr);
                }
            }
            conditionsLocks.remove(lockName);
            
            lock.unlock();            
            LOGGER.debug("removed lock > " + lockName);
        } else {
            throw new LockException("lock not found > " + lockName);
        }
    }

    @Override
    public void await(int conditionId, String processId) throws InterruptedException {
        LockContext.threadProcessId.set(processId);
        LOGGER.debug("will await conditionId > " + conditionId + " processId > " + processId);
        conditions.get(conditionId).await();
    }

    @Override
    public void signal(int conditionId, String processId) {
        LockContext.threadProcessId.set(processId);
        LOGGER.debug("will await signal > " + conditionId + " processId > " + processId);
        conditions.get(conditionId).signal();
    }

    @Override
    public void signalAll(int conditionId, String processId) {
        LockContext.threadProcessId.set(processId);
        LOGGER.debug("will await signalAll > " + conditionId + " processId > " + processId);
        conditions.get(conditionId).signalAll();
    }

    @Override
    public int newCondition(String lockName) {
        if (locks.containsKey(lockName)) {
            Condition c = locks.get(lockName).getLock().newCondition();
            conditions.put(c.hashCode(), c);
            List<Integer> list = conditionsLocks.get(lockName);
            if (list == null) {
                list = new ArrayList<Integer>();
                conditionsLocks.put(lockName, list);
            }
            list.add(c.hashCode());
            LOGGER.debug("created new condition > " + c.hashCode());
            return c.hashCode();
        } else {
            throw new LockException("lock not found > " + lockName);
        }
    }

    @Override
    public void lockInterruptibly(String lockName, String processId, long timeout) throws InterruptedException {
        LockContext.threadProcessId.set(processId);
        if (locks.containsKey(lockName)) {
            LOGGER.debug("will lockInterruptibly > " + lockName + " processId > " + processId);
            locks.get(lockName).getLock().lockInterruptibly();
            locks.get(lockName).setLastAcquire(new Date());
            locks.get(lockName).setLockedProcessId(processId);
            locks.get(lockName).setTimeoutInMs(timeout);
        } else {
            throw new LockException("lock not found > " + lockName);
        }
    }

    @Override
    public boolean tryLock(String lockName, String processId, long timeout) {
        LockContext.threadProcessId.set(processId);
        if (locks.containsKey(lockName)) {
            LOGGER.debug("will trylock > " + lockName + " processId > " + processId);
            boolean result = locks.get(lockName).getLock().tryLock();
            if (result) {
                locks.get(lockName).setLastAcquire(new Date());
                locks.get(lockName).setLockedProcessId(processId);
                locks.get(lockName).setTimeoutInMs(timeout);
            }
            return result;
        } else {
            throw new LockException("lock not found > " + lockName);
        }
    }

    @Override
    public boolean tryLock(String lockName, String processId, long timeout, TimeUnit unit, long lockTimeout)
            throws InterruptedException {
        LockContext.threadProcessId.set(processId);
        if (locks.containsKey(lockName)) {
            LOGGER.debug("will trylock > " + lockName + " processId > " + processId);
            boolean result = locks.get(lockName).getLock().tryLock(timeout, unit);
            if (result) {
                locks.get(lockName).setLastAcquire(new Date());
                locks.get(lockName).setLockedProcessId(processId);
                locks.get(lockName).setTimeoutInMs(lockTimeout);
            }
            return result;
        } else {
            throw new LockException("lock not found > " + lockName);
        }
    }
}
