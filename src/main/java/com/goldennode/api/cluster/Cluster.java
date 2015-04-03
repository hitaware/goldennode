package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Request;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.helper.LockHelper;
import com.goldennode.api.helper.SystemUtils;

public abstract class Cluster {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Cluster.class);
	private Server server;
	protected LeaderSelector leaderSelector;
	private Object lockForMasterServer = new Object();
	protected HeartbeatTimer heartBeatTimer;
	private static final int LOCK_TIMEOUT = Integer.parseInt(SystemUtils.getSystemProperty("60",
			"com.goldennode.api.cluster.Cluster.lockTimeout"));

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

	public Object safeMulticast(Operation operation) throws ClusterException {
		MultiResponse responses = tcpMulticast(getAllServers(), operation);
		try {
			Response response = responses.getResponseAssertAllResponsesSameAndSuccessful();
			return response.getReturnValue();
		} catch (ClusterException e) {
			// TODO Rollback successful servers
			throw e;
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

	public void serverLock(String publicName, long timeout) {
		server.lock(publicName, timeout);
	}

	public void serverUnlock(String publicName) {
		server.unlock(publicName);
	}

	public int serverNewCondition(String lockName) {
		return server.newCondition(lockName);
	}

	public void serverAwait(int conditionId) throws InterruptedException {
		server.await(conditionId);
	}

	public void serverSignal(int conditionId) {
		server.signal(conditionId);
	}

	public void serverSignalAll(int conditionId) {
		server.signalAll(conditionId);
	}

	public void serverLockInterruptibly(String lockName, long timeout) throws InterruptedException {
		server.lockInterruptibly(lockName, timeout);
	}

	public boolean serverTryLock(String lockName, long timeout) {
		return server.tryLock(lockName, timeout);
	}

	public boolean serverTryLock(String lockName, long timeout, TimeUnit unit, long lockTimeout)
			throws InterruptedException {
		return server.tryLock(lockName, timeout, unit, lockTimeout);
	}

	public void lock(String lockName) throws ClusterException {
		lock(lockName, LOCK_TIMEOUT);
	}

	public void lock(String lockName, long timeout) throws ClusterException {
		LOGGER.debug("Will call acquireLock on server" + getMasterServer() + " processId > " + server.createProcessId());
		unicastTCP(getMasterServer(), new Operation(null, "lock", lockName, timeout));
	}

	public void lock(ClusteredObject co) throws ClusterException {
		lock(co, LOCK_TIMEOUT);
	}

	public void lock(ClusteredObject co, long timeout) throws ClusterException {
		LOGGER.debug("Will call acquireLock on server" + getMasterServer() + " processId > " + server.createProcessId());
		unicastTCP(co.getLockServer(), new Operation(null, "lock", co.getPublicName(), timeout));
	}

	public void unlock(String lockName) throws ClusterException {
		LOGGER.debug("Will call releaseLock on server" + getMasterServer() + " processId > " + server.createProcessId());
		unicastTCP(getMasterServer(), new Operation(null, "unlock", lockName));
	}

	public void unlock(ClusteredObject co) throws ClusterException {
		LOGGER.debug("Will call releaseLock on server" + getMasterServer() + " processId > " + server.createProcessId());
		unicastTCP(co.getLockServer(), new Operation(null, "unlock", co.getPublicName()));
	}

	public int newCondition(String lockName) throws ClusterException {
		return (int) unicastTCP(getMasterServer(), new Operation(null, "newCondition", lockName)).getReturnValue();
	}

	public int newCondition(ClusteredObject co) throws ClusterException {
		return (int) unicastTCP(co.getLockServer(), new Operation(null, "newCondition", co.getPublicName()))
				.getReturnValue();
	}

	public void await(int conditionId) throws InterruptedException, ClusterException {
		unicastTCP(getMasterServer(), new Operation(null, "await", conditionId));
	}

	public void signal(int conditionId) throws ClusterException {
		unicastTCP(getMasterServer(), new Operation(null, "signal", conditionId));
	}

	public void signalAll(int conditionId) throws ClusterException {
		unicastTCP(getMasterServer(), new Operation(null, "signalAll", conditionId));
	}

	public void lockInterruptibly(String lockName) throws ClusterException {
		lockInterruptibly(lockName, LOCK_TIMEOUT);
	}

	public void lockInterruptibly(String lockName, long timeout) throws ClusterException {
		unicastTCP(getMasterServer(), new Operation(null, "lockInterruptibly", lockName, timeout));
	}

	public void lockInterruptibly(ClusteredObject co) throws ClusterException {
		lockInterruptibly(co, LOCK_TIMEOUT);
	}

	public void lockInterruptibly(ClusteredObject co, long timeout) throws ClusterException {
		unicastTCP(co.getLockServer(), new Operation(null, "lockInterruptibly", co.getPublicName(), timeout));
	}

	public boolean tryLock(String lockName) throws ClusterException {
		return tryLock(lockName, LOCK_TIMEOUT);
	}

	public boolean tryLock(String lockName, long timeout) throws ClusterException {
		return (boolean) unicastTCP(getMasterServer(), new Operation(null, "tryLock", lockName, timeout))
				.getReturnValue();
	}

	public boolean tryLock(ClusteredObject co) throws ClusterException {
		return tryLock(co, LOCK_TIMEOUT);
	}

	public boolean tryLock(ClusteredObject co, long timeout) throws ClusterException {
		return (boolean) unicastTCP(co.getLockServer(), new Operation(null, "tryLock", co.getPublicName(), timeout))
				.getReturnValue();
	}

	public boolean tryLock(String lockName, long timeout, TimeUnit unit) throws ClusterException {
		return tryLock(lockName, timeout, unit, LOCK_TIMEOUT);
	}

	public boolean tryLock(String lockName, long timeout, TimeUnit unit, long lockTimeout) throws ClusterException {
		return (boolean) unicastTCP(getMasterServer(),
				new Operation(null, "tryLock", lockName, timeout, unit, lockTimeout)).getReturnValue();
	}

	public boolean tryLock(ClusteredObject co, long timeout, TimeUnit unit) throws ClusterException {
		return tryLock(co, timeout, unit, LOCK_TIMEOUT);
	}

	public boolean tryLock(ClusteredObject co, long timeout, TimeUnit unit, long lockTimeout) throws ClusterException {
		return (boolean) unicastTCP(co.getLockServer(),
				new Operation(null, "tryLock", co.getPublicName(), timeout, unit, lockTimeout)).getReturnValue();
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
					LockHelper.notifyAll(lockForMasterServer);
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

	@Override
	public String toString() {
		return " > Cluster [server=" + server + "] ";
	}

	public Server getOwner() {
		return server;
	}

	public abstract ClusteredLock getLock(String publicName) throws ClusterException;

	public abstract <E> List<E> getList(E e, String publicName) throws ClusterException;

	public abstract Collection<Server> getAllServers();

	public abstract Server getServer(String id);

	public abstract Collection<Server> getPeers();

	protected abstract ClusteredObject getClusteredObject(String publicName);
}
