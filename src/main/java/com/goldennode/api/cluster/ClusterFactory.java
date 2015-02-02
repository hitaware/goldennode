package com.goldennode.api.cluster;

import com.goldennode.api.replicatedmemorycluster.ReplicatedMemoryCluster;

public class ClusterFactory {

	private ClusterFactory() {
	}

	public static Cluster getCluster(ClusterType type) throws ClusterException {
		if (type == ClusterType.BIGMEMORY) {
			return new ReplicatedMemoryCluster();// TODO return bigmemorycluster
		} else if (type == ClusterType.REPLICATEDMEMORY) {
			return new ReplicatedMemoryCluster();
		}
		return null;
	}

	public static Cluster getCluster() throws ClusterException {

		return new ReplicatedMemoryCluster();

	}

}
