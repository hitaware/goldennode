package com.goldennode.api.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Server implements Serializable, Comparable<Server> {
    private static final long serialVersionUID = 1L;
    transient private OperationBase operationBase;
    transient private List<ServerStateListener> serverStateListeners = Collections
            .synchronizedList(new ArrayList<ServerStateListener>());
    transient private boolean started = false;
    private InetAddress host;
    private String id;
    private boolean master;
    public static ThreadLocal<String> processId = new ThreadLocal<String>();

    public Server() throws ServerException {
        this(java.util.UUID.randomUUID().toString());
    }

    public Server(String serverId) throws ServerException {
        try {
            setId(serverId);
            setHost(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            throw new ServerException(e);
        }
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

    public synchronized boolean isMaster() {
        return master;
    }

    public synchronized void setMaster(boolean master) {
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

    public String getShortId() {
        return id.length() > 4 ? id.toString().substring(id.length() - 4, id.length()) : id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public abstract int getMulticastPort();

    public abstract int getUnicastUDPPort();

    public abstract int getUnicastTCPPort();

    public abstract void start() throws ServerException;

    public abstract void start(int delay) throws ServerException;

    public abstract void stop(int delay) throws ServerException;

    public abstract void stop() throws ServerException;

    public abstract Request prepareRequest(String method, RequestOptions options, Object... params);

    public abstract Response unicastTCP(Server remoteServer, Request request) throws ServerException;

    public abstract Response unicastUDP(Server remoteServer, Request request) throws ServerException;

    public abstract void multicast(Request request) throws ServerException;

    public abstract List<Response> blockingMulticast(Request request) throws ServerException;

    @Override
    public String toString() {
        return " > Server [id=" + getShortId() + ", host=" + host + ", master=" + master + ", multicastPort="
                + getMulticastPort() + ", unicastUDPPort=" + getUnicastUDPPort() + ", unicastTCPPort="
                + getUnicastTCPPort() + "] ";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Server o) {
        if (o == null) {
            return 1;
        }
        return getId().compareTo(o.getId());
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
