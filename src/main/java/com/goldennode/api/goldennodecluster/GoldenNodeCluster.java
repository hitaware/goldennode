package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.ClusteredObjectNotAvailableException;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.ReplicatedMemoryList;
import com.goldennode.api.cluster.ReplicatedMemoryMap;
import com.goldennode.api.cluster.ReplicatedMemorySet;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerAlreadyStoppedException;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.helper.ExceptionUtils;
import com.goldennode.api.helper.SystemUtils;

public class GoldenNodeCluster extends Cluster {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeCluster.class);
	static final int SERVER_ANNOUNCING_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("5000",
			"com.goldennode.api.goldennodecluster.GoldenNodeCluster.serverAnnouncingDelay"));
	static final int WAITFORMASTER_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("10000",
			"com.goldennode.api.goldennodecluster.GoldenNodeCluster.waitForMasterDelay"));
	private static final int LOCK_TIMEOUT = Integer.parseInt(SystemUtils.getSystemProperty("60000",
			"com.goldennode.api.goldennodecluster.GoldenNodeCluster.lockTimeout"));
	ClusteredObjectManager clusteredObjectManager;
	ClusteredServerManager clusteredServerManager;
	LeaderSelector leaderSelector;
	HeartbeatTimer heartBeatTimer;
	ServerAnnounceTimer serverAnnounceTimer;
	LockService lockService;

	public GoldenNodeCluster(Server server, LockService lockService) throws ClusterException {
		this.lockService = lockService;
		server.setOperationBase(new GoldenNodeClusterOperationBaseImpl(this));
		server.addServerStateListener(new GoldenNodeClusterServerStateListenerImpl(this));

		lockService.createLock(LockTypes.APPLICATION.toString(), LOCK_TIMEOUT);
		lockService.createLock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString(), LOCK_TIMEOUT);
		lockService.createLock(LockTypes.CLUSTERED_SERVER_MANAGER.toString(), LOCK_TIMEOUT);
		lockService.createLock(LockTypes.HANDSHAKING.toString(), LOCK_TIMEOUT);
		clusteredObjectManager = new ClusteredObjectManager();
		clusteredServerManager = new ClusteredServerManager(server);
		leaderSelector = new LeaderSelector(this, new LeaderSelectionListener() {
			@Override
			public void leaderChanged(String newLeaderId) {
				clusteredServerManager.setMasterServer(newLeaderId);
			}
		});
		heartBeatTimer = new HeartbeatTimer(this);
		serverAnnounceTimer = new ServerAnnounceTimer(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> Set<E> newReplicatedMemorySetInstance(String publicName) throws ClusterException {
		return newClusteredObjectInstance(publicName, ReplicatedMemorySet.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> newReplicatedMemoryListInstance(String publicName) throws ClusterException {
		return newClusteredObjectInstance(publicName, ReplicatedMemoryList.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName) throws ClusterException {
		return newClusteredObjectInstance(publicName, ReplicatedMemoryMap.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ClusteredObject> T attach(T t) throws ClusterException {
		try {// TODO test
			lock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString());
			if (t.getCluster() != null) {
				throw new ClusterException("ClusteredObject already attached" + t);
			}
			if (clusteredObjectManager.contains(t)) {
				throw new ClusterException("ClusteredObject already exists" + t);
			}
			Server server = getOwnerOf(t.getPublicName());
			if (server != null) {
				throw new ClusterException("ClusteredObject already exists" + t);
			}
			t.setOwnerId(getOwner().getId());
			t.setCluster(this);
			LOGGER.debug("will create object" + t);
			safeMulticast(new Operation(null, "addClusteredObject", t));
			return (T) clusteredObjectManager.getClusteredObject(t.getPublicName());
		} finally {
			unlock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ClusteredObject> T newClusteredObjectInstance(String publicName, Class<T> claz)
			throws ClusterException {
		T tt;
		try {
			tt = claz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		tt.setOwnerId(getOwner().getId());
		tt.setPublicName(publicName);
		return (T) initClusteredObject(tt);
	}

	private ClusteredObject initClusteredObject(ClusteredObject co) throws ClusterException {
		try {
			lock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString());
			LOGGER.debug("Get List");
			if (clusteredObjectManager.contains(co.getPublicName())) {
				LOGGER.debug("Contains list > " + co.getPublicName());
				return clusteredObjectManager.getClusteredObject(co.getPublicName());
			} else {
				Server server = getOwnerOf(co.getPublicName());
				if (server != null) {
					lock(server, co.getPublicName());
					addClusteredObject((ClusteredObject) unicastTCP(server,
							new Operation(null, "receiveClusteredObject", co.getPublicName()), new RequestOptions())
									.getReturnValue());
					unlock(server, co.getPublicName());
					return clusteredObjectManager.getClusteredObject(co.getPublicName());
				} else {
					LOGGER.debug("Will create list. Doesn't Contain list > " + co.getPublicName());
					safeMulticast(new Operation(null, "addClusteredObject", co));
					return clusteredObjectManager.getClusteredObject(co.getPublicName());
				}
			}
		} finally {
			unlock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString());
		}
	}

	private Server getOwnerOf(String publicName) {
		MultiResponse mr = tcpMulticast(getPeers(), new Operation(null, "amIOwnerOf", publicName),
				new RequestOptions());
		Collection<Server> col = mr.getServersWithNoErrorAndExpectedResult(true);
		for (Server server : col) {
			return server;// NOPMD
		}
		return null;
	}

	void addClusteredObject(ClusteredObject co) throws ClusterException {
		if (clusteredObjectManager.contains(co)) {
			throw new ClusterException("clusteredObject already exits" + co);
		}
		LOGGER.debug("created ClusteredObject" + co);
		co.setCluster(this);
		clusteredObjectManager.addClusteredObject(co);
		if (co.getOwnerId().equals(getOwner().getId())) {
			createLock(co.getPublicName(), LOCK_TIMEOUT);
		}
	}

	void serverIsDeadOperation(Server server) {
		clusteredServerManager.removePeer(server);
		// TODO nullifyOwnerIdClusteredObjects(server);
		LOGGER.debug("is dead server master?");
		if (server.isMaster()) {
			LOGGER.debug("yes, it is");
			leaderSelector.rejoinElection();
		} else {
			LOGGER.debug("no, it is not");
		}
	}

	void incomingServer(final Server server) throws ClusterException {
		if (clusteredServerManager.getServer(server.getId()) == null) {

			if (!clusteredServerManager.addPeer(server)) {
				LOGGER.warn("2 masters" + server + "and  master server" + clusteredServerManager.getMasterServer());
				throw new ClusterException("Master already set");
			}

			heartBeatTimer.schedule(server, new HearbeatStatusListener() {
				@Override
				public void serverUnreachable(Server server) {
					LOGGER.warn("server is dead" + server);
					serverIsDeadOperation(server);
				}
			});
			if (server.isMaster()) {
				LOGGER.debug("joining server is master" + server);
				leaderSelector.setLeaderId(server.getId());
			} else {
				LOGGER.debug("joining server is non-master" + server);
			}
		}
	}

	// private void nullifyOwnerIdClusteredObjects(Server server) {
	// for (ClusteredObject co : clusteredObjectManager.getClusteredObjects()) {
	// if (co.getOwnerId().equals(server.getId())) {
	// co.setOwnerId(null);
	// if (getOwner().isMaster()) {
	// // TODO new voteforownerId
	// }
	// }
	// }
	// }
	void sendOwnServerIdentiy(Server toServer) throws ClusterException {
		unicastTCP(toServer, new Operation(null, "sendOwnServerIdentity", getOwner()), new RequestOptions());
	}

	boolean amIOwnerOf(String publicName) {
		ClusteredObject co = clusteredObjectManager.getClusteredObject(publicName);
		if (co != null && co.getOwnerId().equals(getOwner().getId())) {
			return true;
		}
		return false;
	}

	@Override
	public Response unicastUDP(Server remoteServer, Operation operation, RequestOptions options)
			throws ClusterException {
		try {
			return getOwner().unicastUDP(remoteServer,
					getOwner().prepareRequest(operation.getMethod(), options, operation));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public Response unicastTCP(Server remoteServer, Operation operation, RequestOptions options)
			throws ClusterException {
		try {
			return getOwner().unicastTCP(remoteServer,
					getOwner().prepareRequest(operation.getMethod(), options, operation));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public void multicast(Operation operation, RequestOptions options) throws ClusterException {
		try {
			getOwner().multicast(getOwner().prepareRequest(operation.getMethod(), options, operation));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public Object safeMulticast(Operation operation) throws ClusterException {
		MultiResponse responses = tcpMulticast(clusteredServerManager.getAllServers(), operation, new RequestOptions());
		try {
			Response response = responses.getResponseAssertAllResponsesSameAndSuccessful();
			return response.getReturnValue();
		} catch (ClusterException e) {
			// TODO Rollback successful servers
			throw e;
		}
	}

	@Override
	public MultiResponse tcpMulticast(Collection<Server> servers, Operation operation, RequestOptions options) {
		try {
			LOGGER.trace("begin processOperationOnServers");
			MultiResponse mr = new MultiResponse();
			for (Server remoteServer : servers) {
				try {
					LOGGER.debug("Operation is in progress" + operation + "on server" + remoteServer);
					mr.addSuccessfulResponse(remoteServer, unicastTCP(remoteServer, operation, options));// TODO
																										 // run
																										 // tcp
																										 // requests
																										 // in
																										 // threads
				} catch (ClusterException e) {
					if (ExceptionUtils.hasCause(e, ClusteredObjectNotAvailableException.class)) {// TODO
																								 // what
																								 // the
																								 // hell
																								 // clusteredobject
																								 // related
						// things are here?
						LOGGER.debug("ClusteredObjectNotAvailable " + operation + "server" + remoteServer);
					} else {
						mr.addErroneusResponse(remoteServer, e);
						LOGGER.error("Error occured while processing operation" + operation + "on server" + remoteServer
								+ e.toString());
					}
				}
			}
			return mr;
		} finally {
			LOGGER.trace("end processOperationOnServers");
		}
	}

	public void createLock(String lockName, long lockTimeoutInMs) {
		lockService.createLock(lockName, lockTimeoutInMs);
	}

	public void deleteLock(String lockName) {
		lockService.deleteLock(lockName);
	}

	private void lock(Server server, String lockName) throws ClusterException {
		unicastTCP(server, new Operation(null, "lock", lockName), new RequestOptions());
	}

	private void lock(String lockName) throws ClusterException {
		unicastTCP(clusteredServerManager.getMasterServer(), new Operation(null, "lock", lockName),
				new RequestOptions());
	}

	@Override
	protected void lock(ClusteredObject co) throws ClusterException {
		unicastTCP(clusteredServerManager.getServer(co.getOwnerId()), new Operation(null, "lock", co.getPublicName()),
				new RequestOptions());
	}

	private void unlock(String lockName) throws ClusterException {
		unicastTCP(clusteredServerManager.getMasterServer(), new Operation(null, "unlock", lockName),
				new RequestOptions());
	}

	private void unlock(Server server, String lockName) throws ClusterException {
		unicastTCP(server, new Operation(null, "unlock", lockName), new RequestOptions());
	}

	@Override
	protected void unlock(ClusteredObject co) throws ClusterException {
		unicastTCP(clusteredServerManager.getServer(co.getOwnerId()), new Operation(null, "unlock", co.getPublicName()),
				new RequestOptions());
	}

	@Override
	protected void lockInterruptibly(ClusteredObject co) throws ClusterException {
		unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
				new Operation(null, "lockInterruptibly", co.getPublicName()), new RequestOptions());
	}

	@Override
	protected boolean tryLock(ClusteredObject co) throws ClusterException {
		return (boolean) unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
				new Operation(null, "tryLock", co.getPublicName()), new RequestOptions()).getReturnValue();
	}

	@Override
	protected boolean tryLock(ClusteredObject co, long timeout, TimeUnit unit) throws ClusterException {
		return (boolean) unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
				new Operation(null, "tryLock", co.getPublicName(), timeout, unit), new RequestOptions())
						.getReturnValue();
	}

	@Override
	public Server getOwner() {
		return clusteredServerManager.getOwner();
	}

	@Override
	public void start() throws ClusterException {

		try {
			getOwner().start();
		} catch (ServerException e) {
			throw new ClusterException(e);
		}

	}

	@Override
	public void stop() throws ClusterException {
		try {
			getOwner().stop();
		} catch (ServerAlreadyStoppedException e) {
			LOGGER.debug("Server already stopped. Server " + getOwner());
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public Collection<Server> getPeers() {
		return clusteredServerManager.getPeers();
	}

	@Override
	public Server getCandidateServer() {
		return clusteredServerManager.getCandidateServer();
	}

}
