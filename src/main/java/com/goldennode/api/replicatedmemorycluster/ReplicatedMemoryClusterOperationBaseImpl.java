package com.goldennode.api.replicatedmemorycluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterOperationBase;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.LeaderSelector;
import com.goldennode.api.core.Server;

public class ReplicatedMemoryClusterOperationBaseImpl extends ClusterOperationBase {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LeaderSelector.class);

	ReplicatedMemoryCluster cluster;

	ReplicatedMemoryClusterOperationBaseImpl(ReplicatedMemoryCluster cluster) {
		this.cluster = cluster;
	}

	public void _announceServerJoining(Server s) throws ClusterException {
		cluster.incomingServer(s);
		cluster.sendOwnServerIdentiy(s);
		cluster.replicateObjects(s);// TODO reguest objects from one server instead of
		// sending objects froms each server.

	}

	public void _sendOwnServerIdentity(Server s) {
		cluster.incomingServer(s);
	}

	public void _addClusteredObject(ClusteredObject obj) throws ClusterException {

		cluster.addClusteredObject(obj);

	}

	@Override
	public Cluster getCluster() {
		return cluster;
	}

	// public ClusteredObject _removeClusteredObject(String publicName)
	// throws ClusterException {
	//
	// return cluster.removeClusteredObject(publicName);
	// }
	// public void _createClusteredObject(String publicName,
	// Class<? extends ClusteredObject> claz, String ownerId)
	// throws ClusterException {
	//
	// try {
	// if (cluster.clusteredObjects.containsKey(publicName)) {
	// throw new ClusterException("Object already exits at server:"
	// + cluster.getOwner());
	// }
	// Constructor<? extends ClusteredObject> c = claz
	// .getDeclaredConstructor(String.class, String.class,
	// Cluster.class);
	// ClusteredObject co = c.newInstance(publicName, ownerId, cluster);
	// cluster.clusteredObjects.put(co.getPublicName(), co);
	// // TODO createUndoRecord(new Operation(null,
	// // "u_removeClusteredObject", co));
	// } catch (Exception e) {
	// throw new ClusterException(e);
	// }
	//
	// }

}
