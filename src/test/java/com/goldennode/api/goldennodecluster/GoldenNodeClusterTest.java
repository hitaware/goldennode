package com.goldennode.api.goldennodecluster;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.core.ServerAlreadyStartedException;
import com.goldennode.api.helper.ExceptionUtils;
import com.goldennode.testutils.PortUtils;

public class GoldenNodeClusterTest {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeClusterTest.class);

	@Test
	public void TestIllegalStartWithoutStopping() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster("0");
			c.start();
			validatePortsOpen();
			c.start();
			validatePortsOpen();
		} catch (ClusterException e) {
			ExceptionUtils.throwCauseIfThereIs(e, ServerAlreadyStartedException.class);
		} finally {
			c.stop();
			validateAllPortsClosed();
		}
	}

	@Test
	public void TestStopWithoutStarting() throws Exception {

		Cluster c = null;
		try {
			c = ClusterFactory.getCluster("1");
		} finally {
			c.stop();
			validateAllPortsClosed();
		}

	}

	@Test
	public void TestStop() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster("2");
			c.start();
			validatePortsOpen();
			
		} finally {
			c.stop();
			validateAllPortsClosed();
		}
	}

	@Test
	public void TestStart() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster("3");
			c.start();
			validatePortsOpen();
		} finally {
			c.stop();
		}
	}
	

	@Test
	public void TestStopStart() throws Throwable {
		Cluster c = null;
		try {
			c = ClusterFactory.getCluster("4");
			c.start();
			c.stop();
			c.start();
			c.stop();
			c.start();
			validatePortsOpen();
		} finally {
			c.stop();
		}
	}

	private void validateAllPortsClosed() {
		LOGGER.trace("validateAllPortsClosed");
		for (int i = 25002; i < 25010; i++) {
			Assert.assertTrue(i + " is open",PortUtils.portClosed(i));
		}

		for (int i = 26002; i < 26010; i++) {
			Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
		}
	}

	private void validatePortsOpen() {
		LOGGER.trace("validatePortsOpen");
		Assert.assertTrue("25002 is closed", !PortUtils.portClosed(25002));

		for (int i = 25003; i < 25010; i++) {
			Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
		}

		Assert.assertTrue("26002 is closed", !PortUtils.portClosed(26002));

		for (int i = 26003; i < 26010; i++) {
			Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
		}
	}


}
