package com.goldennode.api.goldennodecluster;

import org.junit.Test;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.core.LockServiceImpl;
import com.goldennode.api.core.MockGoldenNodeServer;
import com.goldennode.api.core.ServerException;

public class TestMasterServer {
	@Test
	public void testGetMasterServer() throws ServerException, ClusterException {
		ClusteredServerManager csm = new ClusteredServerManager(new MockGoldenNodeServer(new LockServiceImpl(), "1"));
		csm.getMasterServer(1000);
	}
}
