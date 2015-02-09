package com.goldennode.api.replicatedmemorycluster;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Logger;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerStateListener;

public class ServerStateListenerImpl implements ServerStateListener {
	/**
	 * A reference to cluster in case it is needed in this class
	 */

	private ReplicatedMemoryCluster cluster;

	ServerStateListenerImpl(ReplicatedMemoryCluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * This method is called when local server is started. addServerToCluster is
	 * called within cluster
	 */

	@Override
	public void serverStarted(Server server) {
		try {
			Logger.debug("***server started... id : " + server.getId());
			cluster.multicast(new Operation(null, "announceServerJoining",
					server));

		} catch (ClusterException e) {
			Logger.error(e);
		}
	}

	/**
	 * This method is called when local server is stopped.
	 * removeServerFromCluster is called within cluster
	 */
	@Override
	public void serverStopping(Server server) {
		try {
			Logger.debug("***server stopped... id : " + server.getId());
			cluster.multicast(new Operation(null, "announceServerLeaving",
					server));// TODO change to safemulticast
		} catch (ClusterException e) {
			Logger.error(e);
		}
	}

	@Override
	public void serverStopped(Server server) {
		//

	}

	@Override
	public void serverStarting(Server server) {
		//

	}
}
