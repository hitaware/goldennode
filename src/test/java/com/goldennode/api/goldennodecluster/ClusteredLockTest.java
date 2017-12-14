package com.goldennode.api.goldennodecluster;

import java.util.concurrent.locks.Lock;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusteredLock;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class ClusteredLockTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredLockTest.class);
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

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY
            + ClusteredLockTest.THREAD_COUNT * (LOOP_COUNT) * 200)
    @RepeatTest(times = 1)
    public void testWithLock_No_Wait() throws Exception {
        ClusterRunner[] cr = new ClusterRunner[ClusteredLockTest.THREAD_COUNT];
        lock = new Lock[ClusteredLockTest.THREAD_COUNT];
        Thread[] th = new Thread[ClusteredLockTest.THREAD_COUNT];
        counter = 0;
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i] = new ClusterRunner(Integer.toString(i));
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i].start();
        }
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            lock[i] = cr[i].getCluster().newClusteredObjectInstance("lock1", ClusteredLock.class);
            th[i] = new Thread(new LockRunnerWithLock(this, i, ClusteredLockTest.LOOP_COUNT,
                    ClusteredLockTest.PROTECTED_BLOK_TASK_DURATION_0), cr[i].getCluster().getOwner().getId());
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            Assert.assertTrue("Leader info: " + CollectionUtils.getContents(cr[i].getCluster().getPeers()),
                    cr[i].getCluster().getPeers().size() == ClusteredLockTest.THREAD_COUNT - 1);
            th[i].start();
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            th[i].join();
        }
        Assert.assertEquals(ClusteredLockTest.LOOP_COUNT * ClusteredLockTest.THREAD_COUNT, getCounter());
        LOGGER.debug("Counter > " + getCounter());
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i].getCluster().stop();
        }
    }

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY
            + ClusteredLockTest.THREAD_COUNT * (LOOP_COUNT * PROTECTED_BLOK_TASK_DURATION_100) * 3)
    @RepeatTest(times = 1)
    public void testWithLock_100ms_wait() throws Exception {
        ClusterRunner[] cr = new ClusterRunner[ClusteredLockTest.THREAD_COUNT];
        lock = new Lock[ClusteredLockTest.THREAD_COUNT];
        Thread[] th = new Thread[ClusteredLockTest.THREAD_COUNT];
        counter = 0;
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i] = new ClusterRunner(Integer.toString(i));
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i].start();
        }
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            lock[i] = cr[i].getCluster().newClusteredObjectInstance("lock2", ClusteredLock.class);
            th[i] = new Thread(new LockRunnerWithLock(this, i, ClusteredLockTest.LOOP_COUNT,
                    ClusteredLockTest.PROTECTED_BLOK_TASK_DURATION_100), cr[i].getCluster().getOwner().getId());
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            Assert.assertTrue("Leader info: " + CollectionUtils.getContents(cr[i].getCluster().getPeers()),
                    cr[i].getCluster().getPeers().size() == ClusteredLockTest.THREAD_COUNT - 1);
            th[i].start();
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            th[i].join();
        }
        Assert.assertEquals(ClusteredLockTest.LOOP_COUNT * ClusteredLockTest.THREAD_COUNT, getCounter());
        LOGGER.debug("Counter > " + getCounter());
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i].getCluster().stop();
        }
    }

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY + 5000)
    @RepeatTest(times = 1)
    public void testWithoutLock() throws Exception {
        ClusterRunner[] cr = new ClusterRunner[ClusteredLockTest.THREAD_COUNT];
        Thread[] th = new Thread[ClusteredLockTest.THREAD_COUNT];
        counter = 0;
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i] = new ClusterRunner(Integer.toString(i));
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i].start();
        }
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            th[i] = new Thread(new LockRunnerWithoutLock(this, ClusteredLockTest.LOOP_COUNT,
                    ClusteredLockTest.PROTECTED_BLOK_TASK_DURATION_0), cr[i].getCluster().getOwner().getId());
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            Assert.assertTrue("Leader info: " + CollectionUtils.getContents(cr[i].getCluster().getPeers()),
                    cr[i].getCluster().getPeers().size() == ClusteredLockTest.THREAD_COUNT - 1);
            th[i].start();
        }
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            th[i].join();
        }
        Assert.assertTrue(ClusteredLockTest.LOOP_COUNT * ClusteredLockTest.THREAD_COUNT >= getCounter());
        LOGGER.debug("Counter > " + getCounter());
        for (int i = 0; i < ClusteredLockTest.THREAD_COUNT; i++) {
            cr[i].getCluster().stop();
        }
    }
}
