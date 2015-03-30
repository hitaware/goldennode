package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Request;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.helper.LockHelper;

public abstract class Cluster {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Cluster.class);
	private Server server;
	protected LeaderSelector leaderSelector;
	private Object lockForMasterServer = new Object();
	protected HeartbeatTimer heartBeatTimer;
	public static ThreadLocal<String> processId = new ThreadLocal<String>();

	public Cluster(Server server) throws ClusterException {
		this.server = server;
		heartBeatTimer = new HeartbeatTimer(this);
		initLeaderSelector();
	}

	public Response unicastUDP(Server remoteServer, Operation operation) throws ClusterException {
		return unicastUDP(remoteServer, operation, new RequestOptions());
	}

	public Response unicastUDP(Server remoteServer, Operation operation, RequestOptions options)
			throws ClusterException {
		try {
			return server.unicastUDP(remoteServer, prepareRequest(operation, options));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public Response unicastTCP(Server remoteServer, Operation operation) throws ClusterException {

		return unicastTCP(remoteServer, operation, new RequestOptions());
	}

	public Response unicastTCP(Server remoteServer, Operation operation, RequestOptions options)
			throws ClusterException {

		try {
			return server.unicastTCP(remoteServer, prepareRequest(operation, options));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public void multicast(Operation operation) throws ClusterException {
		multicast(operation, new RequestOptions());
	}

	public void multicast(Operation operation, RequestOptions options) throws ClusterException {
		try {
			server.multicast(prepareRequest(operation, options));
		} catch (ServerException e) {
			throw new OperationException(e);
		}
	}

	public Object safeMulticast(Operation operation) {
		MultiResponse responses = tcpMulticast(getAllServers(), operation);
		try {
			Response response = responses.getResponseAssertAllResponsesSameAndSuccessful();
			return response.getReturnValue();
		} catch (ClusterException e) {
			// TODO Rollback successful servers
			throw new RuntimeException(e);// Notice that this is an unchecked
			// exception for client usage.
		}
	}

	public MultiResponse tcpMulticast(Collection<Server> servers, Operation operation) {
		return tcpMulticast(servers, operation, new RequestOptions());
	}

	public MultiResponse tcpMulticast(Collection<Server> servers, Operation operation, RequestOptions options) {
		try {
			LOGGER.trace("begin processOperationOnServers");
			MultiResponse mr = new MultiResponse();
			for (Server remoteServer : servers) {
				try {
					LOGGER.debug("Operation is on progress" + operation + "on server" + remoteServer);
					mr.addSuccessfulResponse(server, unicastTCP(remoteServer, operation, options));
				} catch (ClusterException e) {
					mr.addErroneusResponse(server, e);
					LOGGER.error("Error occured while processing operation" + operation + "on server" + remoteServer
							+ e.toString());
				}
			}
			return mr;
		} finally {
			LOGGER.trace("end processOperationOnServers");
		}
	}

	public void acquireLock(String processId) {
		server.acquireLock(processId);
	}

	public void acquireLock(String publicName, String processId) {
		server.acquireLock(publicName, processId);
	}

	public void releaseLock(String processId) {
		server.releaseLock(processId);
	}

	public void releaseLock(String publicName, String processId) {
		server.releaseLock(publicName, processId);
	}

	public void acquireDistributedLock() {
		try {
			// ClusterContext.get(ClusterContext.PROCESS_ID);
			LOGGER.debug("Will call acquireLock on server" + getMasterServer() + " processId > "
					+ getOwnerAndThreadBasedId());
			unicastTCP(getMasterServer(), new Operation(null, "acquireLock", getOwnerAndThreadBasedId()));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	public void acquireDistributedLock(ClusteredObject co) {
		try {
			LOGGER.debug("Will call acquireLock on server" + getMasterServer() + " processId > "
					+ getOwnerAndThreadBasedId());
			unicastTCP(co.getLockServer(), new Operation(null, "acquireLock", co.getPublicName(),
					getOwnerAndThreadBasedId()));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	public void releaseDistributedLock() {
		try {
			LOGGER.debug("Will call releaseLock on server" + getMasterServer() + " processId > "
					+ getOwnerAndThreadBasedId());
			unicastTCP(getMasterServer(), new Operation(null, "releaseLock", getOwnerAndThreadBasedId()));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	public void releaseDistributedLock(ClusteredObject co) {
		try {
			LOGGER.debug("Will call releaseLock on server" + getMasterServer() + " processId > "
					+ getOwnerAndThreadBasedId());
			unicastTCP(co.getLockServer(), new Operation(null, "releaseLock", co.getPublicName(),
					getOwnerAndThreadBasedId()));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() throws ClusterException {
		try {
			heartBeatTimer.start();
			server.start();

		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	public void stop() throws ClusterException {
		try {
			server.stop();
			heartBeatTimer.stop();
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	private void initLeaderSelector() {
		leaderSelector = new LeaderSelector(this, new LeaderSelectionListener() {

			@Override
			public void leaderChanged(String newLeaderId) {
				setMasterServer(newLeaderId);
				if (newLeaderId != null) {
					LockHelper.notify(lockForMasterServer);
				}
			}
		});

	}

	private void setMasterServer(String id) {
		for (Server server : getAllServers()) {
			if (server.getId().equals(id)) {
				LOGGER.debug("setting master server" + server);
				server.setMaster(true);
			} else {
				if (server.isMaster()) {
					// This shouldnt happen
					LOGGER.debug("setting master server back to non-master" + server);
					server.setMaster(false);
				}
			}
		}

	}

	protected Server getMasterServer() {
		while (true) {
			for (Server server : getAllServers()) {
				if (server.isMaster()) {
					return server;
				}
			}
			LockHelper.wait(lockForMasterServer, 0);

		}
	}

	private Request prepareRequest(Operation operation, RequestOptions options) {

		return server.prepareRequest(operation.getMethod(), options, operation);

	}

	private String getOwnerAndThreadBasedId() {

		return getOwner().getId() + "_" + Thread.currentThread().hashCode();
	}

	@Override
	public String toString() {
		return " > Cluster [server=" + server + "] ";
	}

	public Server getOwner() {
		return server;
	}

	public abstract ClusteredLock getLock(String publicName);

	public abstract <E> List<E> getList(E e, String publicName) throws ClusterException;

	public abstract Collection<Server> getAllServers();

	public abstract Server getServer(String id);

	public abstract Collection<Server> getPeers();

	protected abstract ClusteredObject getClusteredObject(String publicName);

}
