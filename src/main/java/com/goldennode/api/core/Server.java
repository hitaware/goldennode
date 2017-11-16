package com.goldennode.api.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Server implements Serializable {
	private static final long serialVersionUID = 1L;
	transient private OperationBase operationBase;
	transient private List<ServerStateListener> serverStateListeners = Collections
			.synchronizedList(new ArrayList<ServerStateListener>());
	transient private boolean started = false;
	private InetAddress host;
	private String id;
	private boolean master;
	private transient LockService lockService;
	public static ThreadLocal<String> processId = new ThreadLocal<String>();

	public Server(String serverId, LockService lockService) throws ServerException {
		try {
			if (serverId == null) {
				setId(java.util.UUID.randomUUID().toString());
			} else {
				setId(serverId);
			}
			setHost(InetAddress.getLocalHost());
			this.lockService = lockService;
		} catch (UnknownHostException e) {
			throw new ServerException(e);
		}
	}

	public void lock(String lockName) {
		lockService.lock(lockName, processId.get());
	}

	public void unlock(String lockName) {
		lockService.unlock(lockName, processId.get());
	}

	public void createLock(String lockName,long lockTimeoutInMs) {
		lockService.createLock(lockName, lockTimeoutInMs);
	}

	public void deleteLock(String lockName) {
		lockService.deleteLock(lockName);
	}

	public void lockInterruptibly(String lockName) throws InterruptedException {
		lockService.lockInterruptibly(lockName, processId.get());
	}

	public boolean tryLock(String lockName, long timeout) {
		return lockService.tryLock(lockName, processId.get(), timeout);
	}

	public boolean tryLock(String lockName, long timeout, TimeUnit unit) throws InterruptedException {
		return lockService.tryLock(lockName, processId.get(), timeout, unit);
	}

	public InetAddress getHost() {
		return host;
	}

	protected void setHost(InetAddress host) {
		this.host = host;
	}

	public OperationBase getOperationBase() {
		return operationBase;
	}

	public void setOperationBase(OperationBase operationBase) {
		this.operationBase = operationBase;
	}

	public ServerStateListener[] getServerStateListeners() {
		return serverStateListeners.toArray(new ServerStateListener[0]);
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public void addServerStateListener(ServerStateListener serverStateListener) {
		serverStateListeners.add(serverStateListener);
	}

	public String getId() {
		return id.toString();
	}

	protected void setId(String id) {
		this.id = id;
	}

	public abstract int getMulticastPort();

	public abstract int getUnicastUDPPort();

	public abstract int getUnicastTCPPort();

	public abstract void start() throws ServerException;

	public abstract void stop() throws ServerException;

	public abstract Request prepareRequest(String method, RequestOptions options, Object... params);

	public abstract Response unicastTCP(Server remoteServer, Request request) throws ServerException;

	public abstract Response unicastUDP(Server remoteServer, Request request) throws ServerException;

	public abstract void multicast(Request request) throws ServerException;

	public abstract List<Response> blockingMulticast(Request request) throws ServerException;

	@Override
	public String toString() {
		return " > Server [id=" + id + ", host=" + host + ", master=" + master + ", multicastPort="
				+ getMulticastPort() + ", unicastUDPPort=" + getUnicastUDPPort() + ", unicastTCPPort="
				+ getUnicastTCPPort() + "] ";
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Server other = (Server) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public String createProcessId() {
		return getId() + "_" + Thread.currentThread().getId();
	}
}
