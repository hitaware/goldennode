package com.goldennode.api.cluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.goldennodecluster.GoldenNodeCluster;
import com.goldennode.api.goldennodecluster.LockService;
import com.goldennode.api.goldennodecluster.LockServiceImpl;

public class ClusterFactory {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusterFactory.class);

	private ClusterFactory() {
	}

	public static Cluster getCluster(String serverId, int multicastPort, ClusterType type) throws ClusterException {
		try {
			LockService lockService = new LockServiceImpl();
			Server server = new GoldenNodeServer(serverId, multicastPort);
			if (type == ClusterType.GOLDENNODECLUSTER) {
				return new GoldenNodeCluster(server, lockService);
			}
			return null;
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public static Cluster getCluster(ClusterType type) throws ClusterException {
		try {
			LockService lockService = new LockServiceImpl();
			Server server = new GoldenNodeServer();
			if (type == ClusterType.GOLDENNODECLUSTER) {
				return new GoldenNodeCluster(server, lockService);
			}
			return null;
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public static Cluster getCluster(String serverId, ClusterType type) throws ClusterException {
		try {
			LockService lockService = new LockServiceImpl();
			Server server = new GoldenNodeServer(serverId);
			if (type == ClusterType.GOLDENNODECLUSTER) {
				return new GoldenNodeCluster(server, lockService);
			}
			return null;
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public static Cluster getCluster(int multicastPort, ClusterType type) throws ClusterException {
		try {
			LockService lockService = new LockServiceImpl();
			Server server = new GoldenNodeServer(multicastPort);
			if (type == ClusterType.GOLDENNODECLUSTER) {
				return new GoldenNodeCluster(server, lockService);
			}
			return null;
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public static Cluster getCluster(String serverId, int multicastPort) throws ClusterException {
		return getCluster(serverId, multicastPort, ClusterType.GOLDENNODECLUSTER);
	}

	public static Cluster getCluster() throws ClusterException {
		return getCluster(ClusterType.GOLDENNODECLUSTER);
	}

	public static Cluster getCluster(String serverId) throws ClusterException {
		return getCluster(serverId, ClusterType.GOLDENNODECLUSTER);
	}

	public static Cluster getCluster(int multicastPort) throws ClusterException {
		return getCluster(multicastPort, ClusterType.GOLDENNODECLUSTER);
	}
}
