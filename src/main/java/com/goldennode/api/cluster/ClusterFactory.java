package com.goldennode.api.cluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.bigmemorycluster.BigMemoryCluster;
import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.replicatedmemorycluster.ReplicatedMemoryCluster;

public class ClusterFactory {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusterFactory.class);

	private ClusterFactory() {
	}

	public static Cluster getCluster(ClusterType type) throws ClusterException {

		try {
			Server server = new GoldenNodeServer();
			if (type == ClusterType.BIGMEMORY) {
				return new BigMemoryCluster(server);
			} else if (type == ClusterType.REPLICATEDMEMORY) {
				return new ReplicatedMemoryCluster(server);
			}
			return null;
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public static Cluster getCluster() throws ClusterException {

		return getCluster(ClusterType.REPLICATEDMEMORY);

	}

}
