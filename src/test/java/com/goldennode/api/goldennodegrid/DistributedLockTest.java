package com.goldennode.api.goldennodegrid;

import java.util.concurrent.locks.Lock;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class DistributedLockTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedLockTest.class);
    private static final int THREAD_COUNT = 3;
    private static final int LOOP_COUNT = 10;
    private static final int PROTECTED_BLOK_TASK_DURATION_0 = 0;
    private static final int PROTECTED_BLOK_TASK_DURATION_100 = 100;
    private int counter;
    Lock[] lock;

    public synchronized int getCounter() {
        LOGGER.debug("returning counter" + counter);
        return counter;
    }

    public synchronized void setCounter(int counter) {
        LOGGER.debug("counter is being set to " + counter);
        this.counter = counter;
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY
            + DistributedLockTest.THREAD_COUNT * (LOOP_COUNT) * 200)
    @RepeatTest(times = 1)
    public void testWithLock_No_Wait() throws Exception {
        GridRunner[] cr = new GridRunner[DistributedLockTest.THREAD_COUNT];
        lock = new Lock[DistributedLockTest.THREAD_COUNT];
        Thread[] th = new Thread[DistributedLockTest.THREAD_COUNT];
        counter = 0;
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i] = new GridRunner(Integer.toString(i));
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i].start();
        }
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            lock[i] = cr[i].getGrid().newDistributedObjectInstance("lock1", DistributedLock.class);
            th[i] = new Thread(new LockRunnerWithLock(this, i, DistributedLockTest.LOOP_COUNT,
                    DistributedLockTest.PROTECTED_BLOK_TASK_DURATION_0), cr[i].getGrid().getOwner().getId());
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            Assert.assertTrue("Leader info: " + CollectionUtils.getContents(cr[i].getGrid().getPeers()),
                    cr[i].getGrid().getPeers().size() == DistributedLockTest.THREAD_COUNT - 1);
            th[i].start();
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            th[i].join();
        }
        Assert.assertEquals(DistributedLockTest.LOOP_COUNT * DistributedLockTest.THREAD_COUNT, getCounter());
        LOGGER.debug("Counter > " + getCounter());
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i].getGrid().stop();
        }
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY
            + DistributedLockTest.THREAD_COUNT * (LOOP_COUNT * PROTECTED_BLOK_TASK_DURATION_100) * 3)
    @RepeatTest(times = 1)
    public void testWithLock_100ms_wait() throws Exception {
        GridRunner[] cr = new GridRunner[DistributedLockTest.THREAD_COUNT];
        lock = new Lock[DistributedLockTest.THREAD_COUNT];
        Thread[] th = new Thread[DistributedLockTest.THREAD_COUNT];
        counter = 0;
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i] = new GridRunner(Integer.toString(i));
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i].start();
        }
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            lock[i] = cr[i].getGrid().newDistributedObjectInstance("lock2", DistributedLock.class);
            th[i] = new Thread(new LockRunnerWithLock(this, i, DistributedLockTest.LOOP_COUNT,
                    DistributedLockTest.PROTECTED_BLOK_TASK_DURATION_100), cr[i].getGrid().getOwner().getId());
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            Assert.assertTrue("Leader info: " + CollectionUtils.getContents(cr[i].getGrid().getPeers()),
                    cr[i].getGrid().getPeers().size() == DistributedLockTest.THREAD_COUNT - 1);
            th[i].start();
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            th[i].join();
        }
        Assert.assertEquals(DistributedLockTest.LOOP_COUNT * DistributedLockTest.THREAD_COUNT, getCounter());
        LOGGER.debug("Counter > " + getCounter());
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i].getGrid().stop();
        }
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY + 5000)
    @RepeatTest(times = 1)
    public void testWithoutLock() throws Exception {
        GridRunner[] cr = new GridRunner[DistributedLockTest.THREAD_COUNT];
        Thread[] th = new Thread[DistributedLockTest.THREAD_COUNT];
        counter = 0;
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i] = new GridRunner(Integer.toString(i));
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i].start();
        }
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            th[i] = new Thread(new LockRunnerWithoutLock(this, DistributedLockTest.LOOP_COUNT,
                    DistributedLockTest.PROTECTED_BLOK_TASK_DURATION_0), cr[i].getGrid().getOwner().getId());
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            Assert.assertTrue("Leader info: " + CollectionUtils.getContents(cr[i].getGrid().getPeers()),
                    cr[i].getGrid().getPeers().size() == DistributedLockTest.THREAD_COUNT - 1);
            th[i].start();
        }
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            th[i].join();
        }
        Assert.assertTrue(DistributedLockTest.LOOP_COUNT * DistributedLockTest.THREAD_COUNT >= getCounter());
        LOGGER.debug("Counter > " + getCounter());
        for (int i = 0; i < DistributedLockTest.THREAD_COUNT; i++) {
            cr[i].getGrid().stop();
        }
    }
}
