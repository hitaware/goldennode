package com.goldennode.api.replicatedmemorycluster;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.core.LockServiceImpl;
import com.goldennode.api.core.MockGoldenNodeServer;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;

public class TestClusterWithMockServer {
	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(TestListOperations.class);

	@Test
	public void test() {
		try {
			Server s = new MockGoldenNodeServer(new LockServiceImpl());
			ReplicatedMemoryCluster cluster = new ReplicatedMemoryCluster(s);
			Assert.assertEquals(cluster.getOwner(), s);
			cluster.stop();
		} catch (ClusterException e) {
			LOGGER.error("Error occured", e);
		} catch (ServerException e) {
			LOGGER.error("Error occured", e);
		}

	}
}
