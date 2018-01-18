package com.goldennode.api.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.goldennode.api.helper.StringUtils;

public abstract class Peer implements Serializable, Comparable<Peer> {
    private static final long serialVersionUID = 1L;
    transient private OperationBase operationBase;
    transient private List<PeerStateListener> peerStateListeners = Collections
            .synchronizedList(new ArrayList<PeerStateListener>());
    transient private boolean started = false;
    private InetAddress host;
    private String id;
    private boolean master;
    public static ThreadLocal<String> processId = new ThreadLocal<String>();

    public Peer() throws PeerException {
        this(java.util.UUID.randomUUID().toString());
    }

    public Peer(String peerId) throws PeerException {
        try {
            setId(peerId);
            setHost(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            throw new PeerException(e);
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

    public PeerStateListener[] getPeerStateListeners() {
        return peerStateListeners.toArray(new PeerStateListener[0]);
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

    public void addPeerStateListener(PeerStateListener peerStateListener) {
        peerStateListeners.add(peerStateListener);
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

    public abstract void start() throws PeerException;

    public abstract void start(int delay) throws PeerException;

    public abstract void stop(int delay) throws PeerException;

    public abstract void stop() throws PeerException;

    public abstract Request prepareRequest(String method, RequestOptions options, Object... params);

    public abstract Response unicastTCP(Peer remotePeer, Request request) throws PeerException;

    public abstract Response unicastUDP(Peer remotePeer, Request request) throws PeerException;

    public abstract void multicast(Request request) throws PeerException;

    public abstract List<Response> blockingMulticast(Request request) throws PeerException;

    @Override
    public String toString() {
        return " > Peer [id=" +  StringUtils.shortId(getId()) + ", host=" + host + ", master=" + master + ", multicastPort="
                + getMulticastPort() + ", unicastUDPPort=" + getUnicastUDPPort() + ", unicastTCPPort="
                + getUnicastTCPPort() + "] ";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Peer o) {
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
        Peer other = (Peer) obj;
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
