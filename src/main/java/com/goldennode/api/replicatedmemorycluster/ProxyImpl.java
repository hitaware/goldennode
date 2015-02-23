package com.goldennode.api.replicatedmemorycluster;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterProxy;
import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Server;

public class ProxyImpl extends ClusterProxy {
	ReplicatedMemoryCluster cluster;

	ProxyImpl(ReplicatedMemoryCluster cluster) {
		this.cluster = cluster;
	}

	public void _announceServerLeaving(Server s) throws ClusterException {
		cluster.removeClusteredServer(s);
	}

	public void _announceServerJoining(Server s) throws ClusterException {
		cluster.addClusteredServer(s);
		// push this server
		cluster.unicastTCP(s, new Operation(null, "sendOwnServerIdentity", cluster.getOwner()));
		// push objects
		Collection<ClusteredObject> cos = cluster.getClusteredObjects();
		Iterator<ClusteredObject> iter = cos.iterator();
		while (iter.hasNext()) {
			ClusteredObject co = iter.next();

			if (co.getOwnerId().equals(cluster.getOwner().getId())) {
				if (ClusteredList.class.isAssignableFrom(co.getClass())) {

					cluster.unicastTCP(s, new Operation(null, "createClusteredObject", co.getPublicName(),
							ClusteredList.class, co.getOwnerId()));
					Iterator iterNew = ((ClusteredList) co).iterator();
					while (iterNew.hasNext()) {
						cluster.unicastTCP(s, new Operation(co.getPublicName(), "add", iterNew.next()));

					}
				} else {

					cluster.unicastTCP(s, new Operation(null, "addClusteredObject", co));
				}
			}
		}

	}

	public void _sendOwnServerIdentity(Server s) {
		cluster.addClusteredServer(s);
	}

	public void _addClusteredObject(ClusteredObject obj) throws ClusterException {
		_u_addClusteredObject(obj);
		// TODO createUndoRecord(new Operation(null, "u_removeClusteredObject",
		// obj.getPublicName()));
	}

	public void _u_addClusteredObject(ClusteredObject obj) throws ClusterException {

		if (cluster.clusteredObjects.containsKey(obj.getPublicName())) {
			throw new ClusterException("Object already exits at server:" + cluster.getOwner());
		}
		cluster.clusteredObjects.put(obj.getPublicName(), obj);

	}

	public void _removeClusteredObject(String publicName) throws ClusterException {
		ClusteredObject co = _u_removeClusteredObject(publicName);
		// TODO preset ownerid,publicname,cluster
		// TODO createUndoRecord(new Operation(null, "u_addClusteredObject",
		// co));

	}

	public ClusteredObject _u_removeClusteredObject(String publicName) throws ClusterException {
		if (!cluster.clusteredObjects.containsKey(publicName)) {
			throw new ClusterException("Object doesn't exist in the server:" + cluster.getOwner());
		}
		ClusteredObject co = cluster.clusteredObjects.get(publicName);
		co.setOwnerId(null);
		co.setPublicName(null);
		co.setCluster(null);
		return cluster.clusteredObjects.remove(publicName);
	}

	public void _createClusteredObject(String publicName, Class<? extends ClusteredObject> claz, String ownerId)
			throws ClusterException {

		try {
			if (cluster.clusteredObjects.containsKey(publicName)) {
				throw new ClusterException("Object already exits at server:" + cluster.getOwner());
			}
			Constructor<? extends ClusteredObject> c = claz.getDeclaredConstructor(String.class, String.class,
					Cluster.class);
			ClusteredObject co = c.newInstance(publicName, ownerId, cluster);
			cluster.clusteredObjects.put(co.getPublicName(), co);
			// TODO createUndoRecord(new Operation(null,
			// "u_removeClusteredObject", co));
		} catch (Exception e) {
			throw new ClusterException(e);
		}

	}

	public void _u_createClusteredObject(ClusteredObject co) throws ClusterException {

		try {
			if (!cluster.clusteredObjects.containsKey(co.getPublicName())) {
				throw new ClusterException("Object doesn't exit in the server:" + cluster.getOwner());
			}
			cluster.clusteredObjects.remove(co.getPublicName());
		} catch (Exception e) {
			throw new ClusterException(e);
		}

	}

	public String _ping(String str) {
		return "pong " + cluster.getOwner().getId();
	}

	@Override
	public Cluster getCluster() {
		return cluster;
	}

}
