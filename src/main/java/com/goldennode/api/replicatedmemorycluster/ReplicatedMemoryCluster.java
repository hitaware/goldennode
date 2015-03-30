package com.goldennode.api.replicatedmemorycluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.ClusteredLock;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.ClusteredObjectManager;
import com.goldennode.api.cluster.ClusteredServerManager;
import com.goldennode.api.cluster.HearbeatStatusListener;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Server;
import com.goldennode.api.helper.LockHelper;
import com.goldennode.api.helper.SystemUtils;

public class ReplicatedMemoryCluster extends Cluster {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCluster.class);

	private static final int HANDSHAKING_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("5000",
			"com.goldennode.api.replicatedmemorycluster.ReplicatedMemoryCluster.initTime"));

	protected ClusteredObjectManager clusteredObjectManager;
	protected ClusteredServerManager clusteredServerManager;

	public ReplicatedMemoryCluster(Server server) throws ClusterException {
		super(server);
		server.setOperationBase(new ReplicatedMemoryClusterOperationBaseImpl(this));
		server.addServerStateListener(new ReplicatedMemoryClusterServerStateListenerImpl(this));

		clusteredObjectManager = new ClusteredObjectManager();
		clusteredServerManager = new ClusteredServerManager();
		// start goldennodeserver
		start();
		// Wait for handshaking of peers
		LockHelper.sleep(HANDSHAKING_DELAY);
		// If eligible for lead go for it
		leaderSelector.joinElection();
		// Will wait until there is a master server(lead)
		getMasterServer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> getList(E e, String publicName) throws ClusterException {

		try {
			acquireDistributedLock();
			LOGGER.debug("Get List");
			if (clusteredObjectManager.contains(publicName)) {
				LOGGER.debug("Contains list > " + publicName);
				return (List<E>) clusteredObjectManager.getClusteredObject(publicName);
			} else {
				LOGGER.debug("Will create list. Doesn't Contain list > " + publicName);
				safeMulticast(new Operation(null, "addClusteredObject", new ClusteredList<E>(publicName, getOwner()
						.getId(), this)));
				return (List<E>) clusteredObjectManager.getClusteredObject(publicName);
			}
		} finally {
			releaseDistributedLock();
		}

	}

	@Override
	public ClusteredLock getLock(String publicName) {

		try {
			acquireDistributedLock();
			if (clusteredObjectManager.contains(publicName)) {
				return (ClusteredLock) clusteredObjectManager.getClusteredObject(publicName);
			} else {
				return (ClusteredLock) safeMulticast(new Operation(null, "addClusteredObject", new ClusteredLock(
						publicName, getOwner().getId(), this)));

			}
		} finally {
			releaseDistributedLock();
		}
	}

	void addClusteredObject(ClusteredObject co) throws ClusterException {
		if (clusteredObjectManager.contains(co)) {
			throw new ClusterException("Object already exits at server" + getOwner() + "clustered object" + co);
		}
		LOGGER.debug("Created clustered object" + co);
		co.setCluster(this);
		clusteredObjectManager.addClusteredObject(co);
		if (isLocalObject(co)) {
			getOwner().createLock(co.getPublicName());
		}
	}

	private boolean isLocalObject(ClusteredObject co) {
		return co.getOwnerId().equals(getOwner().getId());
	}

	void serverIsDeadOperation(Server server) throws ClusterException {

		clusteredServerManager.removeClusteredServer(server);
		// removeClusteredServersObjects(server);

		LOGGER.debug("is dead server master?");
		if (server.isMaster()) {
			LOGGER.debug("yes, it is");
			leaderSelector.rejoinElection();
		} else {
			LOGGER.debug("no, it is not");
		}

	}

	void incomingServer(final Server server) {

		clusteredServerManager.addClusteredServer(server);
		heartBeatTimer.schedule(server, new HearbeatStatusListener() {

			@Override
			public void serverUnreachable(Server server) {
				try {
					serverIsDeadOperation(server);
				} catch (ClusterException e1) {
					// Thread will die
				}

			}
		});

		if (server.isMaster()) {
			LOGGER.debug("new incoming master server" + server);
			leaderSelector.setLeaderId(server.getId());
		} else {
			LOGGER.debug("new incoming non-master server");
		}
	}

	void replicateObjects(Server toServer) throws ClusterException {

		for (ClusteredObject co : clusteredObjectManager.getClusteredObjects()) {

			if (isLocalObject(co)) {
				try {
					// acquireDistributedLock(co);
					acquireDistributedLock();
					// TODO maybe we need to send in parts if data is big
					unicastTCP(toServer, new Operation(null, "addClusteredObject", co));
				} finally {
					releaseDistributedLock();
					// releaseDistributedLock(co);
				}
			}
		}
	}

	void sendOwnServerIdentiy(Server toServer) throws ClusterException {

		unicastTCP(toServer, new Operation(null, "sendOwnServerIdentity", getOwner()));
	}

	@Override
	public void start() throws ClusterException {
		super.start();
	}

	@Override
	public void stop() throws ClusterException {
		super.stop();
		clusteredServerManager.clear();
		clusteredObjectManager.clear();
	}

	@Override
	public Collection<Server> getAllServers() {
		List<Server> servers = new ArrayList<Server>();
		servers.add(getOwner());
		servers.addAll(clusteredServerManager.getClusteredServers());
		return servers;

	}

	@Override
	public Server getServer(String id) {
		return clusteredServerManager.getClusteredServer(id);
	}

	@Override
	public Collection<Server> getPeers() {
		return clusteredServerManager.getClusteredServers();
	}

	@Override
	protected ClusteredObject getClusteredObject(String publicName) {
		return clusteredObjectManager.getClusteredObject(publicName);
	}

}
// public ClusteredObject removeClusteredObject(String publicName)
// throws ClusterException {
// if (!clusteredObjects.containsKey(publicName)) {
// throw new ClusterException("Object doesn't exist in the server:"
// + getOwner());
// }
// ClusteredObject co = clusteredObjects.get(publicName);
// co.setOwnerId(null);
// co.setPublicName(null);
// co.setCluster(null);
// return clusteredObjects.remove(publicName);
// }

// private void removeClusteredServersObjects(Server server) {
// Collection<ClusteredObject> cos = clusteredObjects.values();
// Iterator<ClusteredObject> iter = cos.iterator();
// List<String> ids = new ArrayList<String>();
// while (iter.hasNext()) {
// ClusteredObject co = iter.next();
// if (co.getOwnerId().equals(server.getId())) {
// ids.add(co.getPublicName());
// }
// }
// Iterator<String> iter2 = ids.iterator();
// while (iter2.hasNext()) {
// String id = iter2.next();
// clusteredObjects.remove(id);
// }
// }

