package com.goldennode.api.goldennodecluster;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.core.ServerAlreadyStartedException;
import com.goldennode.api.core.ServerAlreadyStoppedException;
import com.goldennode.api.helper.ExceptionUtils;

public class GoldenNodeClusterTest {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeClusterTest.class);

	@Test(expected = ServerAlreadyStartedException.class)
	public void TestIllegalStart() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster();
			c.start();
			c.start();
		} catch (ClusterException e) {
			ExceptionUtils.throwCauseIfThereIs(e, ServerAlreadyStartedException.class);
		} finally {
			c.stop();
		}
	}

	@Test
	public void TestIlleagalStop() throws Exception {
		Cluster c = null;

		c = ClusterFactory.getCluster();
		c.stop();

	}

	@Test
	public void TestStop() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster();
			c.start();
			c.stop();
		} finally {
		}
	}

	@Test
	public void TestStart() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster();
			c.start();
		} finally {
			c.stop();
		}
	}

	@Test
	public void TestStopStart() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster();
			c.start();
			c.stop();
			c.start();
			c.stop();
			c.start();
		} finally {
			c.stop();
		}
	}
}
