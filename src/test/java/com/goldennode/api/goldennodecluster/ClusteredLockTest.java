package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredLock;
import com.goldennode.testutils.RepeatRule.Repeat;

public class ClusteredLockTest {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredLockTest.class);
	private static final int THREAD_COUNT = 50;
	private static final int LOOP_COUNT = 10;
	private static final int PROTECTED_BLOK_TASK_DURATION_0 = 0;
	private static final int PROTECTED_BLOK_TASK_DURATION_100 = 100;
	private int counter;
	private Cluster[] c;
	Lock[] lock;
	private Thread[] th;
	private int index = 0;

	public synchronized int getCounter() {
		LOGGER.debug("returning counter" + counter);
		return counter;
	}

	public synchronized void setCounter(int counter) {
		LOGGER.debug("counter is being set to " + counter);
		this.counter = counter;
	}

	@Before
	public void init() throws ClusterException {
		c = new Cluster[THREAD_COUNT];
		lock = new Lock[THREAD_COUNT];
		th = new Thread[THREAD_COUNT];
		counter = 0;
	}

	@After
	public void teardown() throws ClusterException {
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i].stop();
		}
	}

	@Test
	public void testWithLock_No_Wait() throws Exception {
		index++;
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i] = ClusterFactory.getCluster(Integer.toString(THREAD_COUNT * (index - 1) + i), 30001);
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			lock[i] = c[i].newClusteredObjectInstance("lock1", ClusteredLock.class);
			th[i] = new Thread(new LockRunnerWithLockTest(this, i, LOOP_COUNT, PROTECTED_BLOK_TASK_DURATION_0),
					c[i].getOwner().getId());
			Thread.sleep(1000);
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			Assert.assertTrue("Leader info: " + getListContents(c[i].getPeers()),
					c[i].getPeers().size() == THREAD_COUNT - 1);
			th[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
		Assert.assertEquals(LOOP_COUNT * THREAD_COUNT, getCounter());
		System.out.println("Counter > " + getCounter());
	}

	@Test
	public void testWithLock_100ms_wait() throws Exception {
		index++;
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i] = ClusterFactory.getCluster(Integer.toString(THREAD_COUNT * (index - 1) + i), 30002);
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			lock[i] = c[i].newClusteredObjectInstance("lock2", ClusteredLock.class);
			th[i] = new Thread(new LockRunnerWithLockTest(this, i, LOOP_COUNT, PROTECTED_BLOK_TASK_DURATION_100),
					c[i].getOwner().getId());
			Thread.sleep(1000);
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			Assert.assertTrue("Leader info: " + getListContents(c[i].getPeers()),
					c[i].getPeers().size() == THREAD_COUNT - 1);
			th[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
		Assert.assertEquals(LOOP_COUNT * THREAD_COUNT, getCounter());
		System.out.println("Counter > " + getCounter());
	}

	@Test
	public void testWithoutLock() throws Exception {
		index++;
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i] = ClusterFactory.getCluster(Integer.toString(THREAD_COUNT * (index - 1) + i), 30003);
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			lock[i] = c[i].newClusteredObjectInstance("lock3", ClusteredLock.class);
			th[i] = new Thread(new LockRunnerWithLockTest(this, i, LOOP_COUNT, PROTECTED_BLOK_TASK_DURATION_0),
					c[i].getOwner().getId());
			Thread.sleep(5000);
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			Assert.assertTrue("Leader info: " + getListContents(c[i].getPeers()),
					c[i].getPeers().size() == THREAD_COUNT - 1);
			th[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
		Assert.assertTrue(LOOP_COUNT * THREAD_COUNT >= getCounter());
		System.out.println("Counter > " + getCounter());
	}

	String getListContents(Collection list) {
		StringBuffer sb = new StringBuffer();
		for (Object s : list) {
			sb.append(s.toString() + " ");
		}
		return sb.toString();
	}
}
