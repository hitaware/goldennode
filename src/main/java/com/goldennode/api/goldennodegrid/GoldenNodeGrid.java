package com.goldennode.api.goldennodegrid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.LockService;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.core.Peer;
import com.goldennode.api.core.PeerAlreadyStartedException;
import com.goldennode.api.core.PeerAlreadyStoppedException;
import com.goldennode.api.core.PeerException;
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
            public void iAmSelectedAsLead() {
                getOwner().setMaster(true);
            }
        });
        heartBeatTimer = new HeartbeatTimer(this);
        peerAnnounceTimer = new PeerAnnounceTimer(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Set<E> newReplicatedMemorySetInstance(String publicName) throws GridException {
        return newDistributedObjectInstance(publicName, ReplicatedMemorySet.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> newReplicatedMemoryListInstance(String publicName) throws GridException {
        return newDistributedObjectInstance(publicName, ReplicatedMemoryList.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName) throws GridException {
        return newDistributedObjectInstance(publicName, ReplicatedMemoryMap.class);
    }

    /*@SuppressWarnings("unchecked")
    @Override
    public <T extends DistributedObject> T attach(T t) throws GridException {
        try {// TODO test
            lock(LockTypes.DISTRIBUTED_OBJECT_MANAGER.toString());
            if (t.getGrid() != null) {
                throw new GridException("DistributedObject already attached" + t);
            }
            if (distributedObjectManager.contains(t)) {
                throw new GridException("DistributedObject already exists" + t);
            }
            Peer peer = getOwnerOf(t.getPublicName());
            if (peer != null) {
                throw new GridException("DistributedObject already exists" + t);
            }
            t.setOwnerId(getOwner().getId());
            t.setGrid(this);
            LOGGER.debug("will create object" + t);
            safeMulticast(new Operation(null, "addDistributedObject", t));
            return (T) distributedObjectManager.getDistributedObject(t.getPublicName());
        } finally {
            unlock(LockTypes.DISTRIBUTED_OBJECT_MANAGER.toString());
        }
    }*/
    @SuppressWarnings("unchecked")
    @Override
    public <T extends DistributedObject> T newDistributedObjectInstance(String publicName, Class<T> claz)
            throws GridException {
        T tt;
        try {
            tt = claz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        tt.setOwnerId(getOwner().getId());
        tt.setPublicName(publicName);
        return (T) initDistributedObject(tt);
    }

    @SuppressWarnings("PMD")
    private DistributedObject initDistributedObject(DistributedObject co) throws GridException {
        try {
            writeLock(LockTypes.DISTRUBUTED_OBJECT_MANAGER.toString());
            LOGGER.debug("Get Object");
            if (distributedObjectManager.contains(co.getPublicName())) {
                LOGGER.debug("Contains object > " + co.getPublicName());
                return distributedObjectManager.getDistributedObject(co.getPublicName());
            } else {
                Peer peer = getOwnerOf(co.getPublicName());
                if (peer != null) {
                    writeLock(peer, co.getPublicName());
                    //Possible Heavy Network I/O Operation
                    addDistributedObject((DistributedObject) unicastTCP(peer,
                            new Operation(null, "receiveDistributedObject", co.getPublicName()), new RequestOptions())
                                    .getReturnValue());
                    unlockWriteLock(peer, co.getPublicName());
                    return distributedObjectManager.getDistributedObject(co.getPublicName());
                } else {
                    LOGGER.debug("Will create object. Doesn't Contain object > " + co.getPublicName());
                    safeMulticast(new Operation(null, "addDistributedObject", co));
                    return distributedObjectManager.getDistributedObject(co.getPublicName());
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
        // TODO nullifyOwnerIdDistributedObjects(peer);
        LOGGER.debug("is dead peer master?");
        if (peer.isMaster()) {
            LOGGER.debug("yes, it is");
            leaderSelector.rejoinElection();
            /* NO NEED TO RESET FOR NOW
            heartBeatTimer.stop();
            peerManager.clear();
            distributedObjectManager.clearRemoteObjects();
            leaderSelector.reset();
            peerAnnounceTimer.schedule();
            LockHelper.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY);
            peerAnnounceTimer.stop();
            leaderSelector.rejoinElection();
            */
        } else {
            LOGGER.debug("no, it is not");
        }
    }

    // 1)no need to sync because we don't add to a list more than once, we only
    // set a leader if coming peer is master.
    // peerManager has a set.
    // only we may have more than one heartbeatstatuslistener
    void incomingPeer(final Peer peer) throws GridException {
        if (peerManager.getServer(peer.getId()) == null) {
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
    // for (DistributedObject co : distributedObjectManager.getDistributedObjects()) {
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
            for (Peer remotePeer : peers) {
                try {
                    LOGGER.debug("Operation is in progress. " + operation + " on peer " + remotePeer);
                    mr.addSuccessfulResponse(remotePeer, unicastTCP(remotePeer, operation, options));// TODO
                                                                                                     // run
                                                                                                     // tcp
                                                                                                     // requests
                                                                                                     // in
                                                                                                     // threads
                } catch (GridException e) {
                    // mr.addErroneusResponse(remotePeer, e);
                    // LOGGER.error("Error occured while processing operation" + operation + "on peer" + remotePeer
                    //         + e.toString());

                    mr.addErroneusResponse(remotePeer, e);
                    LOGGER.error("Error occured while processing operation" + operation + "on peer " + remotePeer
                            + e.toString());

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
        unicastTCP(peerManager.getServer(leaderSelector.getLeaderId()), new Operation(null, "writeLock", lockName),
                new RequestOptions());
    }

    @Override
    public void writeLock(DistributedObject co) throws GridException {
        unicastTCP(peerManager.getServer(co.getOwnerId()), new Operation(null, "writeLock", co.getPublicName()),
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
        unicastTCP(peerManager.getServer(leaderSelector.getLeaderId()),
                new Operation(null, "unlockWriteLock", lockName), new RequestOptions());
    }

    void unlockWriteLock(Peer peer, String lockName) throws GridException {
        unicastTCP(peer, new Operation(null, "unlockWriteLock", lockName), new RequestOptions());
    }

    @Override
    public void unlockWriteLock(DistributedObject co) throws GridException {
        unicastTCP(peerManager.getServer(co.getOwnerId()), new Operation(null, "unlockWriteLock", co.getPublicName()),
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
