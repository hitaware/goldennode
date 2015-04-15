package com.goldennode.api.cluster;

import org.junit.Assert;
import org.junit.Test;

import com.goldennode.api.goldennodecluster.GoldenNodeCluster;

public class TestClusterFactory {
	@Test
	public void test1() throws ClusterException {
		Cluster c1 = ClusterFactory.getCluster();
		Cluster c2 = ClusterFactory.getCluster(ClusterType.GOLDENNODECLUSTER);
		Cluster c3 = ClusterFactory.getCluster(ClusterType.GOLDENNODECLUSTER);
		Assert.assertNotEquals(c1, c2);
		Assert.assertNotEquals(c1, c3);
		Assert.assertNotEquals(c2, c3);
		Assert.assertNotSame(c1, c2);
		Assert.assertNotSame(c1, c3);
		Assert.assertNotSame(c2, c3);
		Assert.assertTrue(c1 instanceof GoldenNodeCluster);
		Assert.assertTrue(c2 instanceof GoldenNodeCluster);
		Assert.assertTrue(c3 instanceof GoldenNodeCluster);
	}
}
