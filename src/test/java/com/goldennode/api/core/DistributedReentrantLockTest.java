package com.goldennode.api.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.LockHelper;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;
import com.goldennode.testutils.ThreadUtils;

public class DistributedReentrantLockTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedReentrantLock.class);
    private Lock lock;
    private long lockTimeOut;
    private String lockName;

    @Before
    public void set() {
        lockTimeOut = 100;
        lockName = "testLock";
        lock = new DistributedReentrantLock(lockName, lockTimeOut);
    }

    @Test
    @RepeatTest(times = 1)
    public void getLockTimeoutInMs() {
        Assert.assertEquals(lockTimeOut, ((DistributedReentrantLock) lock).getLockTimeoutInMs());
    }

    @Test
    @RepeatTest(times = 1)
    public void getLockName() {
        Assert.assertEquals(lockName, ((DistributedReentrantLock) lock).getLockName());
    }

    @Test(expected = RuntimeException.class)
    @RepeatTest(times = 1)
    public void lock1_should_throw_RuntimeException_if_threadProcessId_is_not_set() {
        LockContext.threadProcessId.set(null);
        lock.lock();
    }

    @Test
    @RepeatTest(times = 1)
    public void lock2_lockReleaser_should_be_null_after_unlocking_the_lock() {
        LockContext.threadProcessId.set("1");
        lock.lock();
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser != null);
        LockContext.threadProcessId.set("1");
        lock.unlock();
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser == null);
    }

    @Test(expected = IllegalMonitorStateException.class)
    @RepeatTest(times = 1)
    public void lock3_unlocking_the_lock_with_another_thread_should_throw_exception() {
        LockContext.threadProcessId.set("3");
        lock.lock();
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser != null);
        LockContext.threadProcessId.set("4");
        lock.unlock();
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser == null);
    }

    @Test()
    @RepeatTest(times = 1)
    public void lock4_autorelease_should_happen() {
        LockContext.threadProcessId.set("5");
        lock.lock();
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser != null);
        LockHelper.sleep(lockTimeOut / 2);
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser != null);
        LockHelper.sleep((int) (lockTimeOut * 1.1));
        Assert.assertTrue(((DistributedReentrantLock) lock).lockReleaser == null);
    }

    @Test(expected = UnsupportedOperationException.class)
    @RepeatTest(times = 1)
    public void newCondition() {
        lock.newCondition();
    }

    @Test(expected = InterruptedException.class)
    @RepeatTest(times = 1)
    public void lockInterruptibly() throws InterruptedException {
        LockContext.threadProcessId.set("6");
        lock.lock();
        ThreadUtils.threadInterrupter(Thread.currentThread(), 10);
        LockContext.threadProcessId.set("7");
        lock.lockInterruptibly();
        Assert.fail();
    }

    @Test
    @RepeatTest(times = 1)
    public void tryLock() {
        LockContext.threadProcessId.set("8");
        Assert.assertTrue(lock.tryLock());
        LockContext.threadProcessId.set("9");
        Assert.assertFalse(lock.tryLock());
        LockContext.threadProcessId.set("8");
        lock.unlock();
    }

    @Test(expected = InterruptedException.class)
    @RepeatTest(times = 1)
    public void tryLockTimeOut() throws InterruptedException {
        LockContext.threadProcessId.set("10");
        lock.lock();
        ThreadUtils.threadInterrupter(Thread.currentThread(), lockTimeOut / 2);
        LockContext.threadProcessId.set("11");
        lock.tryLock(lockTimeOut, TimeUnit.MILLISECONDS);
        Assert.fail();
    }
}
