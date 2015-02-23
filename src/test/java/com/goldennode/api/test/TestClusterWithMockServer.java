package com.goldennode.api.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.core.Server;
import com.goldennode.api.replicatedmemorycluster.ReplicatedMemoryCluster;
import com.goldennode.api.snippets.ListOperations;

public class TestClusterWithMockServer {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ListOperations.class);

	@Test
	public void test() {
		try {
			Server s = new MockGoldenNodeServer();
			ReplicatedMemoryCluster cluster = new ReplicatedMemoryCluster(s);
			Assert.assertEquals(cluster.getOwner(), s);
			cluster.stop();
		} catch (ClusterException e) {
			LOGGER.error("Error occured", e);
		}

	}
}
