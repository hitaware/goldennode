package com.goldennode.api.goldennodecluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class ClusterJoinTest {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusterJoinTest.class);
	private static int THREAD_COUNT;
	private ClusterRunner[] th;

	// @Test
	public void testJoining1() throws Exception {
		THREAD_COUNT = 5;
		th = new ClusterRunner[THREAD_COUNT];
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i] = new ClusterRunner(new Integer(i).toString());
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].start();
		}
		Thread.sleep(10000);
		Set<String> s = new HashSet<String>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			s.add(th[i].getLeaderId());
		}
		Assert.assertTrue(s.size() == 1);
		Assert.assertTrue(s.contains(new Integer(THREAD_COUNT - 1).toString()));
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].stopCluster();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
	}

	// @Test
	public void testJoining2() throws Exception {
		THREAD_COUNT = 10;
		th = new ClusterRunner[THREAD_COUNT];
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i] = new ClusterRunner(new Integer(i).toString());
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			if (i == 3) {
				Thread.sleep(10000);
			}
			if (i == 6) {
				Thread.sleep(5000);
			}
			if (i == 7) {
				Thread.sleep(2000);
			}
			if (i == 8) {
				Thread.sleep(1000);
			}
			th[i].start();
		}
		Thread.sleep(10000);
		Set<String> s = new HashSet<String>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			s.add(th[i].getLeaderId());
		}
		Assert.assertTrue(s.size() == 1);
		Assert.assertTrue(s.contains("2"));
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].stopCluster();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
	}

	@Test
	public void testJoining3() throws Exception {
		THREAD_COUNT = 10;
		th = new ClusterRunner[THREAD_COUNT];
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i] = new ClusterRunner(new Integer(i).toString());
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			if (i == 3) {
				Thread.sleep(10000);
			}
			if (i == 6) {
				Thread.sleep(5000);
			}
			if (i == 7) {
				Thread.sleep(2000);
			}
			if (i == 8) {
				Thread.sleep(1000);
			}
			th[i].start();
		}
		Thread.sleep(10000);
		Set<String> set = new HashSet<>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			set.add(th[i].getLeaderId());
		}
		Assert.assertTrue("Leader info: " + getListContents(set),set.size() == 1);
		Assert.assertTrue("Leader info: " + getListContents(set), set.contains("2"));
		th[2].stopCluster();
		Thread.sleep(10000);
		set = new HashSet<>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			if (i == 2) {
				continue;
			}
			set.add(th[i].getLeaderId());
		}
		Assert.assertTrue("Leader info: " + getListContents(set),set.size() == 1);
		Assert.assertTrue("Leader info: " + getListContents(set), set.contains("9"));
		for (int i = 0; i < THREAD_COUNT; i++) {
			if (i == 2) {
				continue;
			}
			th[i].stopCluster();
		}
		for (int i = 0; i < THREAD_COUNT; i++) {
			th[i].join();
		}
	}

	String getListContents(Set<String> list) {
		StringBuffer sb = new StringBuffer();
		for (String s : list) {
			sb.append(s + " ");
		}
		return sb.toString();
	}
}
