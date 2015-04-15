package com.goldennode.api.goldennodecluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerStateListener;

public class GoldenNodeClusterServerStateListenerImpl implements ServerStateListener {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeClusterServerStateListenerImpl.class);
	/**
	 * A reference to cluster in case it is needed in this class
	 */
	private GoldenNodeCluster cluster;

	GoldenNodeClusterServerStateListenerImpl(GoldenNodeCluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * This method is called when local server is started. addServerToCluster is called within cluster
	 */
	@Override
	public void serverStarted(Server server) {
		try {
			LOGGER.debug("***server started... id : " + server.getId());
			cluster.multicast(new Operation(null, "announceServerJoining", server), new RequestOptions());
		} catch (ClusterException e) {
			LOGGER.error("Error Occured", e);
		}
	}

	@Override
	public void serverStopping(Server server) {
		LOGGER.debug("***server stopped... id : " + server.getId());
		// try {
		// cluster.safeMulticast(new Operation(null, "announceServerLeaving", server));
		// } catch (ClusterException e) {
		// LOGGER.error("Error Occured", e);
		// }
	}

	/**
	 * This method is called when local server is stopped. removeServerFromCluster is called within cluster
	 */
	@Override
	public void serverStopped(Server server) {
	}

	@Override
	public void serverStarting(Server server) {
		//
	}
}
