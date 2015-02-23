package com.goldennode.api.replicatedmemorycluster;

import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Server;

public class ServerPinger extends TimerTask {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServerPinger.class);

	private Server serverToPing;
	ReplicatedMemoryCluster cluster;

	public ServerPinger(Server serverToPing, ReplicatedMemoryCluster cluster) {
		this.serverToPing = serverToPing;
		this.cluster = cluster;
	}

	@Override
	public void run() {

		try {
			cluster.unicastTCP(serverToPing, new Operation(null, "ping", cluster.getOwner().getId()));
		} catch (ClusterException e) {
			LOGGER.error("Can't ping peer: " + serverToPing);
			cluster.removeClusteredServer(serverToPing);
		}

	}

}
