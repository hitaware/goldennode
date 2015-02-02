package com.goldennode.api.replicatedmemorycluster;

import java.util.TimerTask;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Logger;
import com.goldennode.api.core.Server;

public class ServerPinger extends TimerTask {
	private Server serverToPing;
	ReplicatedMemoryCluster cluster;

	public ServerPinger(Server serverToPing, ReplicatedMemoryCluster cluster) {
		this.serverToPing = serverToPing;
		this.cluster = cluster;
	}

	@Override
	public void run() {

		try {
			cluster.unicastTCP(serverToPing, new Operation(null, "ping",
					cluster.getOwner().getId()));
		} catch (ClusterException e) {
			Logger.error("Can't ping peer: " + serverToPing);
			cluster.removeClusteredServer(serverToPing);
		}

	}

}
