package com.goldennode.api.replicatedmemorycluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.ClusteredLock;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.OperationException;
import com.goldennode.api.core.Request;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;

public class ReplicatedMemoryCluster implements Cluster {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCluster.class);

	Map<String, ClusteredObject> clusteredObjects;
	private Map<String, Server> clusteredServers;
	private Map<String, TimerTask> clusteredServerPingers;
	private Server server;
	private Timer pingTimer;
	private int PING_PERIOD;
	private int PING_INITIAL_DELAY;
	private int INIT_TIME;
	private Object obj = new Object();

	public ReplicatedMemoryCluster(Server server) throws ClusterException {

		loadConfig();
		this.server = server;
		server.setOperationBase(new ReplicatedMemoryClusterOperationBaseImpl(this));
		server.addServerStateListener(new ReplicatedMemoryClusterServerStateListenerImpl(this));
		clusteredServers = new ConcurrentHashMap<String, Server>();
		clusteredServerPingers = new ConcurrentHashMap<String, TimerTask>();
		clusteredObjects = new ConcurrentHashMap<String, ClusteredObject>();
		pingTimer = new Timer();
		start();
		try {
			synchronized (obj) {
				obj.wait(INIT_TIME);
			}
		} catch (InterruptedException e) {
			LOGGER.trace("interruption");
		}

	}

	private void loadConfig() {

		if (System.getProperty("replicatedmemorycluster.inittime") == null) {
			System.setProperty("replicatedmemorycluster.inittime", "1000");
		}
		if (System.getProperty("replicatedmemorycluster.pingperiod") == null) {
			System.setProperty("replicatedmemorycluster.pingperiod", "1000");
		}
		if (System.getProperty("replicatedmemorycluster.pinginitialdelay") == null) {
			System.setProperty("replicatedmemorycluster.pinginitialdelay", "1000");
		}

		INIT_TIME = Integer.parseInt(System.getProperty("replicatedmemorycluster.inittime"));
		PING_PERIOD = Integer.parseInt(System.getProperty("replicatedmemorycluster.pingperiod"));
		PING_INITIAL_DELAY = Integer.parseInt(System.getProperty("replicatedmemorycluster.pinginitialdelay"));
	}

	@Override
	public Collection<ClusteredObject> getClusteredObjects() {

		return Collections.unmodifiableCollection(clusteredObjects.values());
	}

	@Override
	public ClusteredObject getClusteredObject(String publicName) {

		return clusteredObjects.get(publicName);

	}

	Request prepareRequest(Operation operation) {

		return server.prepareRequest(operation.getMethod(), operation);

	}

	@Override
	public Server getOwner() {
		return server;
	}

	@Override
	public Response unicastUDP(Server remoteServer, Operation operation) throws ClusterException {
		try {
			return server.unicastUDP(remoteServer, prepareRequest(operation));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public Response unicastTCP(Server remoteServer, Operation operation) throws ClusterException {

		try {
			return server.unicastTCP(remoteServer, prepareRequest(operation));
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public void multicast(Operation operation) throws ClusterException {
		try {
			server.multicast(prepareRequest(operation));
		} catch (ServerException e) {
			throw new OperationException(e);
		}
	}

	private int getVersion(String publicName) throws ClusterException {

		return (Integer) unicastTCP(getOwner(), new Operation(publicName, "getVersion")).getReturnValue();

	}

	public void removeClusteredServer(Server server) {
		LOGGER.debug("Server removed from the cluster: " + server);
		clusteredServers.remove(server.getId());
		TimerTask tt = clusteredServerPingers.remove(server.getId());
		tt.cancel();
		Collection<ClusteredObject> cos = clusteredObjects.values();
		Iterator<ClusteredObject> iter = cos.iterator();
		List<String> ids = new ArrayList<String>();
		while (iter.hasNext()) {
			ClusteredObject co = iter.next();
			if (co.getOwnerId().equals(server.getId())) {
				ids.add(co.getPublicName());
			}
		}
		Iterator<String> iter2 = ids.iterator();
		while (iter2.hasNext()) {
			String id = iter2.next();
			clusteredObjects.remove(id);
		}
	};

	public void addClusteredServer(Server server) {
		LOGGER.debug("Server added to the cluster: " + server);
		clusteredServers.put(server.getId(), server);
		TimerTask tt = new ServerPinger(server, this);
		clusteredServerPingers.put(server.getId(), tt);
		pingTimer.schedule(tt, PING_INITIAL_DELAY, PING_PERIOD);

	}

	@Override
	public Collection<Server> getPeers() {
		return Collections.unmodifiableCollection(clusteredServers.values());
	}

	@Override
	public String toString() {
		return "Cluster [server=" + server + "]";
	}

	@Override
	public void start() throws ClusterException {
		try {
			server.start();
			pingTimer = new Timer();
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public void stop() throws ClusterException {
		try {
			server.stop();
			clusteredServers.clear();
			clusteredObjects.clear();
			pingTimer.cancel();
		} catch (ServerException e) {
			throw new ClusterException(e);
		}
	}

	@Override
	public List<Response> safeMulticast(Operation operation) {
		List<Server> servers = new ArrayList<Server>();
		servers.add(getOwner());
		servers.addAll(clusteredServers.values());

		try {
			lockObjects(servers, operation);
			// checkVersions(servers, operation);
			List<Response> responses = doOperLogic(servers, operation);
			return responses;
		} catch (Exception e) {

		} finally {
			unlockObjects(servers, operation);
		}

		return null;

	}

	private List<Response> doOperLogic(List<Server> servers, Operation operation) {
		List<?> doList = doOper(servers, operation);
		if (doList.get(0) instanceof Response) {
			// Success
		} else {
			return null;
			// Exception occured success servers returned with the list
			// TODOList<?> undoList = doOper((List<Server>) doList,
			// operation.undo());
			// if (undoList.get(0) instanceof Response) {
			// // Success
			// } else {
			// // Exception occured success servers returned with the list
			// resynchObject();
			// }
		}
		return (List<Response>) doList;
	}

	private List<?> doOper(List<Server> servers, Operation operation) {

		Iterator<Server> iter = servers.iterator();
		List<Server> successServers = new ArrayList<Server>();
		List<Response> responses = new ArrayList<Response>();
		while (iter.hasNext()) {
			Server remoteServer = iter.next();
			try {
				responses.add(unicastTCP(remoteServer, operation));
				successServers.add(remoteServer);

			} catch (ClusterException e) {
				return successServers;
			}
		}
		return responses;
	}

	private void unlockObjects(List<Server> servers, Operation operation) {
		// TODO Auto-generated method stub

	}

	private void resynchObject() {
		// TODO Auto-generated method stub

	}

	private void lockObjects(List<Server> servers, Operation operation) {
		// TODO Auto-generated method stub

	}

	private void checkVersions(List<Server> servers, Operation operation) {
		// TODO Auto-generated method stub

	}

	@Override
	public Lock createLock(ClusteredObject obj) {
		try {
			ClusteredLock cl = new ClusteredLock(obj.getPublicName() + UUID.randomUUID().toString(), getOwner().getId());

		} catch (ClusterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void attachObject(ClusteredObject co) throws ClusterException {

		try {

			if (clusteredObjects.containsKey(co.getPublicName())) {
				throw new ClusterException("Object already exits at server:" + getOwner());

			}
			co.setCluster(this);

			if (ClusteredList.class.isAssignableFrom(co.getClass())) {

				safeMulticast(new Operation(null, "createClusteredObject", co.getPublicName(), co.getClass(),
						co.getOwnerId()));

				Iterator iterNew = ((ClusteredList) co).iterator();
				while (iterNew.hasNext()) {
					safeMulticast(new Operation(co.getPublicName(), "add", iterNew.next()));

				}
			} else {

				safeMulticast(new Operation(null, "addClusteredObject", co));
			}

		} catch (Exception e) {
			throw new ClusterException(e);
		}
	}

	// @Override
	// public void detachObject(ClusteredObject obj) throws ClusterException {
	//
	// try {
	// if (!clusteredObjects.containsKey(obj.getPublicName())) {
	// throw new ClusterException("Object doesn't exit in the server:" +
	// getOwner());
	//
	// }
	//
	// safeMulticast(
	//
	// new Operation(null, "removeClusteredObject", obj.getPublicName()));
	// obj.setOwnerId(null);
	// obj.setPublicName(null);
	// obj.setCluster(null);
	//
	// } catch (Exception e) {
	// throw new ClusterException(e);
	// }
	// }

}
