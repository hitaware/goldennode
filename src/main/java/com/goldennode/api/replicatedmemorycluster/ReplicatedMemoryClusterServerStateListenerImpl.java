package com.goldennode.api.replicatedmemorycluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerStateListener;

public class ReplicatedMemoryClusterServerStateListenerImpl implements ServerStateListener {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServerStateListener.class);

	/**
	 * A reference to cluster in case it is needed in this class
	 */

	private ReplicatedMemoryCluster cluster;

	ReplicatedMemoryClusterServerStateListenerImpl(ReplicatedMemoryCluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * This method is called when local server is started. addServerToCluster is called within cluster
	 */

	@Override
	public void serverStarted(Server server) {
		try {
			LOGGER.debug("***server started... id : " + server.getId());
			cluster.multicast(new Operation(null, "announceServerJoining", server));

		} catch (ClusterException e) {
			LOGGER.error("Error Occured", e);
		}
	}

	@Override
	public void serverStopping(Server server) {

	}

	/**
	 * This method is called when local server is stopped. removeServerFromCluster is called within cluster
	 */
	@Override
	public void serverStopped(Server server) {
		LOGGER.debug("***server stopped... id : " + server.getId());
		// We cancelled multicast, we will detect it via heartbeat detector.
		// cluster.multicast(new Operation(null, "announceServerLeaving",
		// server));

	}

	@Override
	public void serverStarting(Server server) {
		//

	}
}
