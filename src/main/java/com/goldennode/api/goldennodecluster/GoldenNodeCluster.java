package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.ClusteredObjectNotAvailableException;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.NoResponseException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.ReplicatedMemoryList;
import com.goldennode.api.cluster.ReplicatedMemoryMap;
import com.goldennode.api.cluster.ReplicatedMemorySet;
import com.goldennode.api.core.LockService;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerAlreadyStartedException;
import com.goldennode.api.core.ServerAlreadyStoppedException;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.helper.ExceptionUtils;
import com.goldennode.api.helper.SystemUtils;

public class GoldenNodeCluster extends Cluster {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeCluster.class);
    static final int DEFAULT_SERVER_ANNOUNCING_DELAY = 5000;
    static final int SERVER_ANNOUNCING_DELAY = Integer
            .parseInt(SystemUtils.getSystemProperty(String.valueOf(DEFAULT_SERVER_ANNOUNCING_DELAY),
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

    public GoldenNodeCluster(Server server, LockService lockService) {
        this.lockService = lockService;
        server.setOperationBase(new GoldenNodeClusterOperationBaseImpl(this));
        server.addServerStateListener(new GoldenNodeClusterServerStateListenerImpl(this));
        lockService.createLock(LockTypes.APPLICATION.toString(), GoldenNodeCluster.LOCK_TIMEOUT);
        lockService.createLock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString(), GoldenNodeCluster.LOCK_TIMEOUT);
        lockService.createLock(LockTypes.CLUSTERED_SERVER_MANAGER.toString(), GoldenNodeCluster.LOCK_TIMEOUT);
        clusteredObjectManager = new ClusteredObjectManager(this);
        clusteredServerManager = new ClusteredServerManager(server);
        leaderSelector = new LeaderSelector(this, new LeaderSelectionListener() {
            @Override
            public void iAmSelectedAsLead() {
                getOwner().setMaster(true);
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

    /*@SuppressWarnings("unchecked")
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
    }*/
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

    @SuppressWarnings("PMD")
    private ClusteredObject initClusteredObject(ClusteredObject co) throws ClusterException {
        try {
            writeLock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString());
            LOGGER.debug("Get List");
            if (clusteredObjectManager.contains(co.getPublicName())) {
                LOGGER.debug("Contains list > " + co.getPublicName());
                return clusteredObjectManager.getClusteredObject(co.getPublicName());
            } else {
                Server server = getOwnerOf(co.getPublicName());
                if (server != null) {
                    writeLock(server, co.getPublicName());
                    addClusteredObject((ClusteredObject) unicastTCP(server,
                            new Operation(null, "receiveClusteredObject", co.getPublicName()), new RequestOptions())
                                    .getReturnValue());
                    unlockWriteLock(server, co.getPublicName());
                    return clusteredObjectManager.getClusteredObject(co.getPublicName());
                } else {
                    LOGGER.debug("Will create list. Doesn't Contain list > " + co.getPublicName());
                    safeMulticast(new Operation(null, "addClusteredObject", co));
                    return clusteredObjectManager.getClusteredObject(co.getPublicName());
                }
            }
        } finally {
            unlockWriteLock(LockTypes.CLUSTERED_OBJECT_MANAGER.toString());
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
            createLock(co.getPublicName(), GoldenNodeCluster.LOCK_TIMEOUT);
        }
    }

    void serverIsDeadOperation(Server server) {
        clusteredServerManager.removePeer(server);
        // TODO nullifyOwnerIdClusteredObjects(server);
        LOGGER.debug("is dead server master?");
        if (server.isMaster()) {
            LOGGER.debug("yes, it is");
            leaderSelector.rejoinElection();
            /* NO NEED TO RESET FOR NOW
            heartBeatTimer.stop();
            clusteredServerManager.clear();
            clusteredObjectManager.clearRemoteObjects();
            leaderSelector.reset();
            serverAnnounceTimer.schedule();
            LockHelper.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY);
            serverAnnounceTimer.stop();
            leaderSelector.rejoinElection();
            */
        } else {
            LOGGER.debug("no, it is not");
        }
    }

    // 1)no need to sync because we don't add to a list more than once, we only
    // set a leader if coming server is master.
    // clusteredServerManager has a set.
    // only we may have more than one heartbeatstatuslistener
    void incomingServer(final Server server) throws ClusterException {
        if (clusteredServerManager.getServer(server.getId()) == null) {
            clusteredServerManager.addPeer(server);
            if (server.isMaster()) {
                LOGGER.debug("joining server is master" + server);
                if (leaderSelector.getLeaderId() != null) {
                    LOGGER.warn("There is already a master server: " + leaderSelector.getLeaderId());
                    throw new ClusterException("Master already set");
                }
                leaderSelector.setLeaderId(server.getId());
            } else {
                LOGGER.debug("joining server is non-master" + server);
            }
            heartBeatTimer.schedule(server, new HearbeatStatusListener() {
                @Override
                public void serverUnreachable(Server server) {
                    LOGGER.warn("server is dead" + server);
                    serverIsDeadOperation(server);
                }
            });
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
            operation = new Operation(operation.getObjectPublicName(), "commit", operation.getParams());
            responses = tcpMulticast(clusteredServerManager.getAllServers(), operation, new RequestOptions());
            try {
                response = responses.getResponseAssertAllResponsesSameAndSuccessful();
                return response;
            } catch (ClusterException e) {
                throw e;
            }
        } catch (ClusterException e) {
            operation = new Operation(operation.getObjectPublicName(), "rollback", operation.getParams());
            tcpMulticast(responses.getServersWithNoErrorAndExpectedResult(Boolean.TRUE), operation,
                    new RequestOptions());
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
                    mr.addErroneusResponse(remoteServer, e);
                    LOGGER.error("Error occured while processing operation" + operation + "on server" + remoteServer
                            + e.toString());
                    /*if (ExceptionUtils.hasCause(e, ClusteredObjectNotAvailableException.class)) {// TODO
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
                    }*/
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

    @Override
    protected void readLock(ClusteredObject co) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
                new Operation(null, "readLock", co.getPublicName()), new RequestOptions());
    }

    void writeLock(Server server, String lockName) throws ClusterException {
        unicastTCP(server, new Operation(null, "writeLock", lockName), new RequestOptions());
    }

    void writeLock(String lockName) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(leaderSelector.getLeaderId()),
                new Operation(null, "writeLock", lockName), new RequestOptions());
    }

    @Override
    protected void writeLock(ClusteredObject co) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
                new Operation(null, "writeLock", co.getPublicName()), new RequestOptions());
    }

    void unlockReadLock(String lockName) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(leaderSelector.getLeaderId()),
                new Operation(null, "unlockReadLock", lockName), new RequestOptions());
    }

    void unlockReadLock(Server server, String lockName) throws ClusterException {
        unicastTCP(server, new Operation(null, "unlockReadLock", lockName), new RequestOptions());
    }

    @Override
    protected void unlockReadLock(ClusteredObject co) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
                new Operation(null, "unlockReadLock", co.getPublicName()), new RequestOptions());
    }

    void unlockWriteLock(String lockName) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(leaderSelector.getLeaderId()),
                new Operation(null, "unlockWriteLock", lockName), new RequestOptions());
    }

    void unlockWriteLock(Server server, String lockName) throws ClusterException {
        unicastTCP(server, new Operation(null, "unlockWriteLock", lockName), new RequestOptions());
    }

    @Override
    protected void unlockWriteLock(ClusteredObject co) throws ClusterException {
        unicastTCP(clusteredServerManager.getServer(co.getOwnerId()),
                new Operation(null, "unlockWriteLock", co.getPublicName()), new RequestOptions());
    }

    @Override
    public Server getOwner() {
        return clusteredServerManager.getOwner();
    }

    @Override
    public void start() throws ClusterException {
        try {
            getOwner().start();
        } catch (ServerAlreadyStartedException e) {
            LOGGER.debug("Server already started. Server " + getOwner());
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
