package com.goldennode.api.goldennodegrid;

import java.util.List;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Peer;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.helper.ReflectionUtils;

public class GoldenNodeGridOperationBaseImpl extends GridOperationBase {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeGridOperationBaseImpl.class);
    GoldenNodeGrid grid;
    protected List<Operation> uncommitted = new Vector<>();

    public boolean addToUncommited(Operation operation) {
        uncommitted.add(operation);
        return true;
    }

    public void _adoptOprphanObject(String publicName, Peer peer) {
        grid.distributedObjectManager.adoptOrphanObject(publicName, peer);
    }

    public Object _commit() {
        LOGGER.debug("Committing");
        if (uncommitted.size() > 1) {
            throw new OperationException("..More.." + uncommitted.size());
        }
        Operation operation = uncommitted.remove(0);
        if (operation != null) {
            try {
                return ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
            } catch (Exception e) {
                throw new OperationException(e);
            }
        } else {
            throw new OperationException("No operation to commit");
        }
    }

    public void _rollback() {
        LOGGER.debug("Rollback");
        uncommitted.clear();
    }

    GoldenNodeGridOperationBaseImpl(GoldenNodeGrid grid) {
        this.grid = grid;
    }

    public void _announcePeerJoining(Peer s) throws GridException {
        if (grid.peerManager.getPeer(s.getId()) == null) {
            LOGGER.debug("Peer announced that it is joining. Peer: " + s);
            grid.incomingPeer(s);
            grid.sendOwnPeerIdentiy(s);
        }
    }

    public DistributedObject _receiveDistributedObject(String publicName) throws GridException {
        return grid.distributedObjectManager.getDistributedObject(publicName);
    }

    public void _announcePeerLeaving(Peer s) throws GridException {
        LOGGER.debug("Peer announced that it is leaving. Peer: " + s);
        grid.heartBeatTimer.cancelTaskForPeer(s);
        grid.peerIsDeadOperation(s);
    }

    public void _sendOwnPeerIdentity(Peer s) throws GridException {
        if (grid.peerManager.getPeer(s.getId()) == null) {
            LOGGER.debug("Peer sent its identity: " + s);
            grid.incomingPeer(s);
        }
    }

    public void _addDistributedObject(DistributedObject obj) throws GridException {
        grid.addDistributedObject(obj);
    }

    public boolean _amIOwnerOf(String publicName) {
        return grid.amIOwnerOf(publicName);
    }

    @Override
    public Object _op_(Operation operation) throws OperationException {
        if (operation.getObjectPublicName() != null) {
            if (!grid.isDistributedObjectOperationEnabled())
                throw new OperationException("Operation on distributed objects disabled");
            DistributedObject co = grid.distributedObjectManager.getDistributedObject(operation.getObjectPublicName());
            if (co != null) {
                try {
                    if (operation.isSafe())
                        return co.addToUncommited(operation);
                    else
                        return ReflectionUtils.callMethod(co, operation.getObjectMethod(), operation.getParams());
                } catch (Exception e) {
                    LOGGER.error(operation.toString(), e);
                    throw new OperationException(e);
                }
            } else {
                LOGGER.debug("peer not ready, distributedObject not found:" + operation.getObjectPublicName());
                throw new DistriburedObjectNotAvailableException();
            }
        } else {
            try {
                if (operation.isSafe()) {
                    LOGGER.debug("Adding to uncommitted" + operation);
                    return addToUncommited(operation);
                } else
                    return ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
            } catch (Exception e) {
                LOGGER.error(operation.toString(), e);
                throw new OperationException(e);
            }
        }
    }

    public String _ping(String str) {
        return "pong " + grid.getOwner().getId();
    }

    public boolean _acquireLeadershipCommit(String id) {
        return grid.leaderSelector.acquireLeadershipCommit(id, false);
    }

    public boolean _acquireLeadershipPrepare(String id) {
        return grid.leaderSelector.acquireLeadershipPrepare(id, false);
    }

    public void _readLock(String publicName) {
        grid.lockService.readLock(publicName, Peer.processId.get());
    }

    public void _writeLock(String publicName) {
        grid.lockService.writeLock(publicName, Peer.processId.get());
    }

    public void _unlockReadLock(String publicName) {
        grid.lockService.unlockReadLock(publicName, Peer.processId.get());
    }

    public void _unlockWriteLock(String publicName) {
        grid.lockService.unlockWriteLock(publicName, Peer.processId.get());
    }
}
