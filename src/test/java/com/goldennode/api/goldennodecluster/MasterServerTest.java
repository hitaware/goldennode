package com.goldennode.api.goldennodecluster;

import org.junit.Assert;
import org.junit.Test;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.core.MockGoldenNodeServer;
import com.goldennode.api.core.ServerException;
import com.goldennode.testutils.ThreadUtils;

public class MasterServerTest {
	@Test
	public void testGetMasterServer() throws ServerException, ClusterException, InterruptedException {
		final ClusteredServerManager csm = new ClusteredServerManager(new MockGoldenNodeServer("1"));
		ThreadUtils.run(new Runnable() {
			@Override
			public void run() {
				csm.setMasterServer("1");
			}
		}, 1000);
		Assert.assertEquals("1", csm.getMasterServer(2000).getId());
	}
}
