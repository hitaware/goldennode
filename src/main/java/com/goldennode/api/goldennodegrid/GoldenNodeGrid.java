package com.goldennode.api.goldennodegrid;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.LockService;
import com.goldennode.api.core.Peer;
import com.goldennode.api.core.PeerAlreadyStartedException;
import com.goldennode.api.core.PeerAlreadyStoppedException;
import com.goldennode.api.core.PeerException;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.helper.ExceptionUtils;
import com.goldennode.api.helper.SystemUtils;

public class GoldenNodeGrid extends Grid {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeGrid.class);
    static final int DEFAULT_PEER_ANNOUNCING_DELAY = 5000;
    static final int PEER_ANNOUNCING_DELAY = Integer
            .parseInt(SystemUtils.getSystemProperty(String.valueOf(DEFAULT_PEER_ANNOUNCING_DELAY),
                    "com.goldennode.api.goldennodegrid.GoldenNodeGrid.peerAnnouncingDelay"));
    static final int WAITFORMASTER_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("10000",
            "com.goldennode.api.goldennodegrid.GoldenNodeGrid.waitForMasterDelay"));
    private static final int LOCK_TIMEOUT = Integer.parseInt(
            SystemUtils.getSystemProperty("600000", "com.goldennode.api.goldennodegrid.GoldenNodeGrid.lockTimeout"));
    DistributedObjectManager distributedObjectManager;
    PeerManager peerManager;
    LeaderSelector leaderSelector;
    HeartbeatTimer heartBeatTimer;
    PeerAnnounceTimer peerAnnounceTimer;
    LockService lockService;
    private boolean distributedObjectOperationEnabled = true;

    public GoldenNodeGrid(Peer peer, LockService lockService) {
        this.lockService = lockService;
        peer.setOperationBase(new GoldenNodeGridOperationBaseImpl(this));
        peer.addPeerStateListener(new GoldenNodeGridPeerStateListenerImpl(this));
        lockService.createLock(LockTypes.APPLICATION.toString(), GoldenNodeGrid.LOCK_TIMEOUT);
        lockService.createLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString(), GoldenNodeGrid.LOCK_TIMEOUT);
        lockService.createLock(LockTypes.PEER_MANAGER.toString(), GoldenNodeGrid.LOCK_TIMEOUT);
        distributedObjectManager = new DistributedObjectManager(this);
        peerManager = new PeerManager(peer);
        leaderSelector = new LeaderSelector(this, new LeaderSelectionListener() {
            @Override
            public void iAmSelectedAsLead(boolean rejoined) {
                getOwner().setMaster(true);
                if (rejoined) {
                    boolean locked = false;
                    try {
                        writeLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
                        locked = true;
                        List<String> lst = distributedObjectManager.getOrphanObjects();
                        for (String orphanObject : lst) {
                            tcpMulticast(peerManager.getAllPeers(),
                                    new Operation(null, "adoptOrphanObject", orphanObject, getOwner()),
                                    new RequestOptions());
                        }
                    } catch (Exception e) {
                        LOGGER.error("GridException", e);
                    } finally {
                        try {
                            if (locked)
                                unlockWriteLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
                        } catch (GridException e) {
                            LOGGER.error("GridException", e);
                        }
                    }
                }
            }
        });
        heartBeatTimer = new HeartbeatTimer(this);
        peerAnnounceTimer = new PeerAnnounceTimer(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Set<E> newReplicatedMemorySetInstance(String publicName, boolean eagerPropagation) throws GridException {
        return newDistributedObjectInstance(publicName, ReplicatedMemorySet.class, eagerPropagation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> newReplicatedMemoryListInstance(String publicName, boolean eagerPropagation)
            throws GridException {
        return newDistributedObjectInstance(publicName, ReplicatedMemoryList.class, eagerPropagation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName, boolean eagerPropagation)
            throws GridException {
        return newDistributedObjectInstance(publicName, ReplicatedMemoryMap.class, eagerPropagation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> newReplicatedMemoryMapInstance(boolean eagerPropagation) throws GridException {
        return newDistributedObjectInstance(ReplicatedMemoryMap.class, eagerPropagation);
    }

    @Override
    public <T extends DistributedObject> T newDistributedObjectInstance(String publicName, Class<T> claz)
            throws GridException {
        return newDistributedObjectInstance(publicName, claz, true);
    }

    @Override
    public <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName) throws GridException {
        return newReplicatedMemoryMapInstance(publicName, true);
    }

    @Override
    public <E> List<E> newReplicatedMemoryListInstance(String publicName) throws GridException {
        return newReplicatedMemoryListInstance(publicName, true);
    }

    @Override
    public <E> Set<E> newReplicatedMemorySetInstance(String publicName) throws GridException {
        return newReplicatedMemorySetInstance(publicName, true);
    }

    @Override
    public <K, V> Map<K, V> newReplicatedMemoryMapInstance() throws GridException {
        return newReplicatedMemoryMapInstance(true);
    }

    @Override
    public <T extends DistributedObject> T newDistributedObjectInstance(Class<T> claz) throws GridException {
        return newDistributedObjectInstance(claz, true);
    }

    @Override
    public <E> Set<E> newReplicatedMemorySetInstance() throws GridException {
        return newReplicatedMemorySetInstance(true);
    }

    @Override
    public <E> List<E> newReplicatedMemoryListInstance() throws GridException {
        return newReplicatedMemoryListInstance(true);
    }

    @Override
    public <T extends DistributedObject> T attach(T t) throws GridException {
        return attach(t, true);
    }

    @Override
    public <T extends DistributedObject> T attach(T t, boolean propagate) throws GridException {
        if (t.getGrid() != null) {
            throw new GridException("DistributedObject already attached" + t);
        }
        try {
            writeLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
            LOGGER.debug("Get Object");
            if (distributedObjectManager.contains(t.getPublicName())) {
                throw new GridException("DistributedObject already attached" + t);
            } else {
                LOGGER.debug("Will create object. Doesn't Contain object > " + t.getPublicName());
                t.setOwnerId(getOwner().getId());
                if (propagate)
                    safeMulticast(new Operation(null, "addDistributedObject", t));
                else
                    addDistributedObject(t);
                return (T) distributedObjectManager.getDistributedObject(t.getPublicName());
            }
        } finally {
            unlockWriteLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DistributedObject> T newDistributedObjectInstance(String publicName, Class<T> claz,
            boolean eagerPropagation) throws GridException {
        T tt;
        try {
            tt = claz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        tt.setOwnerId(getOwner().getId());
        tt.setPublicName(publicName);
        return (T) initDistributedObject(tt, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DistributedObject> T newDistributedObjectInstance(Class<T> claz, boolean eagerPropagation)
            throws GridException {
        T tt;
        try {
            tt = claz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        tt.setOwnerId(getOwner().getId());
        return (T) initDistributedObject(tt, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Set<E> newReplicatedMemorySetInstance(boolean eagerPropagation) throws GridException {
        return newDistributedObjectInstance(ReplicatedMemorySet.class, eagerPropagation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> newReplicatedMemoryListInstance(boolean eagerPropagation) throws GridException {
        return newDistributedObjectInstance(ReplicatedMemoryList.class, eagerPropagation);
    }

    @SuppressWarnings({ "PMD", "unchecked" })
    private <T extends DistributedObject> T initDistributedObject(T t, boolean propagate) throws GridException {
        try {
            writeLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
            LOGGER.debug("Get Object");
            if (distributedObjectManager.contains(t.getPublicName())) {
                LOGGER.debug("Contains object > " + t.getPublicName());
                return (T) distributedObjectManager.getDistributedObject(t.getPublicName());
            } else {
                Peer peer = getOwnerOf(t.getPublicName());
                if (peer != null) {
                    writeLock(peer, t.getPublicName());
                    // Possible Heavy Network I/O Operation
                    addDistributedObject((DistributedObject) unicastTCP(peer,
                            new Operation(null, "receiveDistributedObject", t.getPublicName()), new RequestOptions())
                                    .getReturnValue());
                    unlockWriteLock(peer, t.getPublicName());
                    return (T) distributedObjectManager.getDistributedObject(t.getPublicName());
                } else {
                    LOGGER.debug("Will create object. Doesn't Contain object > " + t.getPublicName());
                    if (propagate) {
                        safeMulticast(new Operation(null, "addDistributedObject", t));
                    } else {
                        addDistributedObject(t);
                    }
                    return (T) distributedObjectManager.getDistributedObject(t.getPublicName());
                }
            }
        } finally {
            unlockWriteLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
        }
    }

    private Peer getOwnerOf(String publicName) {
        MultiResponse mr = tcpMulticast(getPeers(), new Operation(null, "amIOwnerOf", publicName),
                new RequestOptions());
        Collection<Peer> col = mr.getPeersWithNoErrorAndExpectedResult(true);
        for (Peer peer : col) {
            return peer;// NOPMD
        }
        return null;
    }

    void addDistributedObject(DistributedObject co) throws GridException {
        if (distributedObjectManager.contains(co)) {
            throw new GridException("distributedObject already exits" + co);
        }
        LOGGER.debug("created DistributedObject" + co);
        co.setGrid(this);
        distributedObjectManager.addDistributedObject(co);
        if (co.getOwnerId().equals(getOwner().getId())) {
            createLock(co.getPublicName(), GoldenNodeGrid.LOCK_TIMEOUT);
        }
    }

    void peerIsDeadOperation(Peer peer) {
        peerManager.removePeer(peer);
        distributedObjectManager.makeObjectsOrphanFor(peer);
        LOGGER.debug("is dead peer master?");
        if (peer.isMaster()) {
            LOGGER.debug("yes, it is");
            leaderSelector.rejoinElection();
        } else {
            LOGGER.debug("no, it is not");
        }
    }

    // 1)no need to sync because we don't add to a list more than once, we only
    // set a leader if coming peer is master.
    // peerManager has a set.
    // only we may have more than one heartbeatstatuslistener
    void incomingPeer(final Peer peer) throws GridException {
        if (peerManager.getPeer(peer.getId()) == null) {
            peerManager.addPeer(peer);
            if (peer.isMaster()) {
                LOGGER.debug("joining peer is master" + peer);
                if (leaderSelector.getLeaderId() != null) {
                    LOGGER.warn("There is already a master peer: " + leaderSelector.getLeaderId());
                    throw new GridException("Master already set");
                }
                leaderSelector.setLeaderId(peer.getId());
            } else {
                LOGGER.debug("joining peer is non-master" + peer);
            }
            heartBeatTimer.schedule(peer, new HearbeatStatusListener() {
                @Override
                public void peerUnreachable(Peer peer) {
                    LOGGER.warn("peer is dead" + peer);
                    peerIsDeadOperation(peer);
                }
            });
        }
    }

    // private void nullifyOwnerIdDistributedObjects(Peer peer) {
    // for (DistributedObject co : distributedObjectManager.getDistributedObjects())
    // {
    // if (co.getOwnerId().equals(peer.getId())) {
    // co.setOwnerId(null);
    // if (getOwner().isMaster()) {
    // // TODO new voteforownerId
    // }
    // }
    // }
    // }
    void sendOwnPeerIdentiy(Peer toPeer) throws GridException {
        unicastTCP(toPeer, new Operation(null, "sendOwnPeerIdentity", getOwner()), new RequestOptions());
    }

    boolean amIOwnerOf(String publicName) {
        DistributedObject co = distributedObjectManager.getDistributedObject(publicName);
        if (co != null && co.getOwnerId().equals(getOwner().getId())) {
            return true;
        }
        return false;
    }

    @Override
    public Response unicastUDP(Peer remotePeer, Operation operation, RequestOptions options) throws GridException {
        try {
            return getOwner().unicastUDP(remotePeer,
                    getOwner().prepareRequest(operation.getMethod(), options, operation));
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    @Override
    public Response unicastTCP(Peer remotePeer, Operation operation, RequestOptions options) throws GridException {
        try {
            return getOwner().unicastTCP(remotePeer,
                    getOwner().prepareRequest(operation.getMethod(), options, operation));
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    @Override
    public void multicast(Operation operation, RequestOptions options) throws GridException {
        try {
            getOwner().multicast(getOwner().prepareRequest(operation.getMethod(), options, operation));
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    @Override
    public Object safeMulticast(Operation operation) throws GridException {
        MultiResponse responses = null;
        try {
            operation.setSafe(true);
            responses = tcpMulticast(peerManager.getAllPeers(), operation, new RequestOptions());
            Response response = responses.getResponseAssertAllResponsesSameAndSuccessful();
            operation = new Operation(operation.getObjectPublicName(), "commit");
            responses = tcpMulticast(peerManager.getAllPeers(), operation, new RequestOptions());
            try {
                response = responses.getResponseAssertAllResponsesSameAndSuccessful();
                return response.getReturnValue();
            } catch (GridException e) {
                throw e;
            }
        } catch (GridException e) {
            operation = new Operation(operation.getObjectPublicName(), "rollback");
            tcpMulticast(responses.getPeersWithNoErrorAndExpectedResult(Boolean.TRUE), operation, new RequestOptions());
            throw e;
        }
    }

    @Override
    public MultiResponse tcpMulticast(Collection<Peer> peers, Operation operation, RequestOptions options) {
        try {
            LOGGER.trace("begin processOperationOnPeers");
            MultiResponse mr = new MultiResponse();
            Map<Peer, Future<Response>> map = new HashMap<>();
            ExecutorService es = null;
            if (peers.size() > 0)
                es = Executors.newFixedThreadPool(peers.size());
            for (Peer remotePeer : peers) {
                LOGGER.debug("Operation is in progress. " + operation + " on peer " + remotePeer);
                Future<Response> f = es.submit(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        return unicastTCP(remotePeer, operation, options);
                    }
                });
                map.put(remotePeer, f);
            }
            for (Entry<Peer, Future<Response>> entry : map.entrySet()) {
                try {
                    mr.addSuccessfulResponse(entry.getKey(), entry.getValue().get());
                } catch (InterruptedException | ExecutionException e) {
                    if (ExceptionUtils.hasCause(e, GridException.class)) {
                        mr.addErroneusResponse(entry.getKey(), (GridException) e.getCause());
                        LOGGER.error("Error occured while processing operation" + operation + "on peer "
                                + entry.getKey() + e.toString());
                    }
                }
            }
            return mr;
        } finally {
            LOGGER.trace("end processOperationOnPeers");
        }
    }

    public void createLock(String lockName, long lockTimeoutInMs) {
        lockService.createLock(lockName, lockTimeoutInMs);
    }

    public void deleteLock(String lockName) {
        lockService.deleteLock(lockName);
    }

    @Override
    public void readLock(DistributedObject co) throws GridException {
        writeLock(co);
    }

    void writeLock(Peer peer, String lockName) throws GridException {
        unicastTCP(peer, new Operation(null, "writeLock", lockName), new RequestOptions());
    }

    void writeLock(String lockName) throws GridException {
        unicastTCP(peerManager.getPeer(leaderSelector.getLeaderId()), new Operation(null, "writeLock", lockName),
                new RequestOptions());
    }

    @Override
    public void writeLock(DistributedObject co) throws GridException {
        unicastTCP(peerManager.getPeer(co.getOwnerId()), new Operation(null, "writeLock", co.getPublicName()),
                new RequestOptions());
    }

    void unlockReadLock(String lockName) throws GridException {
        unlockWriteLock(lockName);
    }

    void unlockReadLock(Peer peer, String lockName) throws GridException {
        unlockWriteLock(peer, lockName);
    }

    @Override
    public void unlockReadLock(DistributedObject co) throws GridException {
        unlockWriteLock(co);
    }

    void unlockWriteLock(String lockName) throws GridException {
        unicastTCP(peerManager.getPeer(leaderSelector.getLeaderId()), new Operation(null, "unlockWriteLock", lockName),
                new RequestOptions());
    }

    void unlockWriteLock(Peer peer, String lockName) throws GridException {
        unicastTCP(peer, new Operation(null, "unlockWriteLock", lockName), new RequestOptions());
    }

    @Override
    public void unlockWriteLock(DistributedObject co) throws GridException {
        unicastTCP(peerManager.getPeer(co.getOwnerId()), new Operation(null, "unlockWriteLock", co.getPublicName()),
                new RequestOptions());
    }

    @Override
    public Peer getOwner() {
        return peerManager.getOwner();
    }

    @Override
    public void start() throws GridException {
        try {
            getOwner().start();
        } catch (PeerAlreadyStartedException e) {
            LOGGER.debug("Peer already started. Peer " + getOwner());
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    @Override
    public void stop() throws GridException {
        try {
            getOwner().stop();
        } catch (PeerAlreadyStoppedException e) {
            LOGGER.debug("Peer already stopped. Peer " + getOwner());
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    @Override
    public Collection<Peer> getPeers() {
        return peerManager.getPeers();
    }

    @Override
    public Peer getCandidatePeer() {
        return peerManager.getCandidatePeer();
    }

    public boolean isDistributedObjectOperationEnabled() {
        return distributedObjectOperationEnabled;
    }

    public void setDistributedObjectOperationEnabled(boolean distributedObjectOperationEnabled) {
        this.distributedObjectOperationEnabled = distributedObjectOperationEnabled;
    }
}
