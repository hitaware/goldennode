package com.goldennode.api.goldennodecluster;

import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredLock;
import com.goldennode.testutils.RepeatRule;
import com.goldennode.testutils.RepeatRule.Repeat;

public class ClusteredLockTest {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredLockTest.class);
	private static final int THREAD_COUNT = 5;
	private static final int LOOP_COUNT = 10;
	private static final int PROTECTED_BLOK_TASK_DURATION = 5;
	private int counter;
	private Cluster[] c;
	Lock[] lock;
	private Thread[] th;
	@Rule
	public RepeatRule repeatRule = new RepeatRule();

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
	@Repeat(times = 2)
	public void testWithLock() throws Exception {
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i] = ClusterFactory.getCluster();
			c[i].start();
			lock[i] = c[i].newClusteredObjectInstance("lock1", ClusteredLock.class);
			th[i] = new Thread(new LockRunnerWithLockTest(this, i, LOOP_COUNT, PROTECTED_BLOK_TASK_DURATION));
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
		Assert.assertEquals(LOOP_COUNT * THREAD_COUNT, getCounter());
		System.out.println("Counter > " + getCounter());
	}

	@Test
	@Repeat(times = 2)
	public void testWithoutLock() throws Exception {
		for (int i = 0; i < THREAD_COUNT; i++) {
			c[i] = ClusterFactory.getCluster();
			c[i].start();
			lock[i] = c[i].newClusteredObjectInstance("lock1", ClusteredLock.class);
			th[i] = new Thread(new LockRunnerWithoutLockTest(this, i, LOOP_COUNT, PROTECTED_BLOK_TASK_DURATION));
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].start();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
		Assert.assertNotEquals(LOOP_COUNT * THREAD_COUNT, getCounter());
		System.out.println("Counter > " + getCounter());
	}
}
