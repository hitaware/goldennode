package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.TestTimedOutException;
import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.LockHelper;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;
import com.goldennode.testutils.ThreadUtils;

public class DistributedReentrantReadWriteLockTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedReentrantReadWriteLock.class);
    private ReadWriteLock lock;
    private long lockTimeOut;
    private String lockName;

    @Before
    public void set() {
        lockTimeOut = 100;
        lockName = "testLock";
        lock = new DistributedReentrantReadWriteLock(lockName, lockTimeOut);
    }

    @Test
    @RepeatTest(times = 1)
    public void getLockTimeoutInMs() {
        Assert.assertEquals(lockTimeOut, ((DistributedReentrantReadWriteLock) lock).readLock().getLockTimeoutInMs());
        Assert.assertEquals(lockTimeOut, ((DistributedReentrantReadWriteLock) lock).writeLock().getLockTimeoutInMs());
    }

    @Test
    @RepeatTest(times = 1)
    public void getLockName() {
        Assert.assertEquals(lockName+"_r", ((DistributedReentrantReadWriteLock) lock).readLock().getLockName());
        Assert.assertEquals(lockName+"_w", ((DistributedReentrantReadWriteLock) lock).writeLock().getLockName());
    }

    @Test(expected = RuntimeException.class)
    @RepeatTest(times = 1)
    public void readLock_should_throw_RuntimeException_if_threadProcessId_is_not_set() {
        LockContext.threadProcessId.set(null);
        lock.readLock().lock();
    }
    
    @Test(expected = RuntimeException.class)
    @RepeatTest(times = 1)
    public void writeLock_should_throw_RuntimeException_if_threadProcessId_is_not_set() {
        LockContext.threadProcessId.set(null);
        lock.writeLock().lock();
    }

    @Test
    @RepeatTest(times = 1)
    public void lock_lockReleaser_should_be_null_after_unlocking_the_lock() {
        LockContext.threadProcessId.set("1");
        lock.readLock().lock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser != null);
        LockContext.threadProcessId.set("1");
        lock.readLock().unlock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser == null);
        LockContext.threadProcessId.set("1");
        lock.writeLock().lock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser != null);
        LockContext.threadProcessId.set("1");
        lock.writeLock().unlock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser == null);
    }
    
    @Test(expected = IllegalMonitorStateException.class)
    @RepeatTest(times = 1)
    public void readLock_unlocking_the_lock_with_another_thread_should_throw_exception() {
        LockContext.threadProcessId.set("3");
        lock.readLock().lock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser != null);
        LockContext.threadProcessId.set("4");
        lock.readLock().unlock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser == null);
    }
    
    @Test(expected = IllegalMonitorStateException.class)
    @RepeatTest(times = 1)
    public void writeLock_unlocking_the_lock_with_another_thread_should_throw_exception() {
        LockContext.threadProcessId.set("3");
        lock.writeLock().lock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser != null);
        LockContext.threadProcessId.set("4");
        lock.writeLock().unlock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser == null);
    }

    @Test()
    @RepeatTest(times = 1)
    public void lock_autorelease_should_happen() {
        LockContext.threadProcessId.set("5");
        lock.readLock().lock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser != null);
        LockHelper.sleep(lockTimeOut / 2);
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser != null);
        LockHelper.sleep((int) (lockTimeOut * 1.1));
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).readLock().lockReleaser == null);
        lock.readLock().unlock();
        LockContext.threadProcessId.set("66");
        lock.writeLock().lock();
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser != null);
        LockHelper.sleep(lockTimeOut / 2);
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser != null);
        LockHelper.sleep((int) (lockTimeOut * 1.1));
        Assert.assertTrue(((DistributedReentrantReadWriteLock) lock).writeLock().lockReleaser == null);
    }

    @Test(expected = UnsupportedOperationException.class)
    @RepeatTest(times = 1)
    public void newConditionForReadLock() {
        lock.readLock().newCondition();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @RepeatTest(times = 1)
    public void newConditionForWriteLock() {
        lock.writeLock().newCondition();
    }

    @Test(expected = InterruptedException.class)
    @RepeatTest(times = 1)
    public void lockInterruptibly() throws InterruptedException {
        LockContext.threadProcessId.set("6");
        lock.writeLock().lockInterruptibly();
        ThreadUtils.threadInterrupter(Thread.currentThread(), 10);
        LockContext.threadProcessId.set("7");
        lock.writeLock().lockInterruptibly();
        Assert.fail();
    }

    @Test
    @RepeatTest(times = 1)
    public void tryLock() {
        LockContext.threadProcessId.set("8");
        Assert.assertTrue(lock.writeLock().tryLock());
        LockContext.threadProcessId.set("9");
        Assert.assertFalse(lock.writeLock().tryLock());
        LockContext.threadProcessId.set("8");
        lock.writeLock().unlock();
    }

    @Test(expected = InterruptedException.class)
    @RepeatTest(times = 1)
    public void tryLockTimeOut() throws InterruptedException {
        LockContext.threadProcessId.set("10");
        lock.writeLock().lock();
        ThreadUtils.threadInterrupter(Thread.currentThread(), lockTimeOut / 2);
        LockContext.threadProcessId.set("11");
        lock.writeLock().tryLock(lockTimeOut, TimeUnit.MILLISECONDS);
        Assert.fail();
    }
    
    
    @Test(timeout=50)
    @RepeatTest(times = 1)
    public void canAcquireMultipleReadLocks() throws InterruptedException {
        LockContext.threadProcessId.set("12");
        lock.readLock().lock();
        LockContext.threadProcessId.set("13");
        lock.readLock().lock();
        LockContext.threadProcessId.set("14");
        lock.readLock().tryLock();
    }
    
    @Test(timeout=50)
    @RepeatTest(times = 1)
    public void shouldntAcquireMultipleWriteLocks() throws InterruptedException {
        LockContext.threadProcessId.set("15");
        lock.writeLock().lock();
        LockContext.threadProcessId.set("16");
        Assert.assertFalse(lock.writeLock().tryLock());
    }
    
    @Test(timeout=50)
    @RepeatTest(times = 1)
    public void shouldntAcquireMultipleWriteLocks2() throws InterruptedException {
        LockContext.threadProcessId.set("17");
        lock.writeLock().lock();
        LockContext.threadProcessId.set("18");
        Assert.assertFalse(lock.writeLock().tryLock());
        LockContext.threadProcessId.set("17");
        lock.writeLock().unlock();
        LockContext.threadProcessId.set("19");
        Assert.assertTrue(lock.writeLock().tryLock());
        lock.writeLock().unlock();
        LockContext.threadProcessId.set("20");
        lock.writeLock().lock();
        LockContext.threadProcessId.set("21");
        Assert.assertFalse(lock.writeLock().tryLock());
    }
    
    @Test(timeout=50)
    @RepeatTest(times = 1)
    public void readLockAfterWriteLock() throws InterruptedException {
        LockContext.threadProcessId.set("22");
        lock.writeLock().lock();
        LockContext.threadProcessId.set("23");
        Assert.assertFalse(lock.readLock().tryLock());
        
    }
    
    @Test(timeout=50)
    @RepeatTest(times = 1)
    public void readLockBeforeWriteLock() throws InterruptedException {
        LockContext.threadProcessId.set("24");
        lock.readLock().lock();
        LockContext.threadProcessId.set("25");
        Assert.assertFalse(lock.writeLock().tryLock());
        
    }
}
