package com.goldennode.api.cluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.LockService;
import com.goldennode.api.core.LockServiceImpl;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.goldennodecluster.GoldenNodeCluster;

public class ClusterFactory {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusterFactory.class);

	private ClusterFactory() {
	}

	public static Cluster getCluster(String serverId, ClusterType type) throws ClusterException {
		try {
			LockService lockService = new LockServiceImpl();
			Server server = new GoldenNodeServer(serverId, lockService);
			if (type == ClusterType.GOLDENNODECLUSTER) {
				return new GoldenNodeCluster(server);
			}
			return null;
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public static Cluster getCluster(ClusterType type) throws ClusterException {
		return getCluster(null, type);
	}

	public static Cluster getCluster() throws ClusterException {
		return getCluster(null, ClusterType.GOLDENNODECLUSTER);
	}

	public static Cluster getCluster(String name) throws ClusterException {
		return getCluster(name, ClusterType.GOLDENNODECLUSTER);
	}
}
