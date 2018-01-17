package com.goldennode.api.goldennodecluster;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterOperationBase;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.ClusteredObjectNotAvailableException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.OperationException;
import com.goldennode.api.core.Server;
import com.goldennode.api.helper.ReflectionUtils;

public class GoldenNodeClusterOperationBaseImpl extends ClusterOperationBase {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeClusterOperationBaseImpl.class);
    GoldenNodeCluster cluster;
    protected Queue<Operation> uncommitted = new ArrayBlockingQueue<>(1);

    public boolean addToUncommited(Operation operation) {
        uncommitted.add(operation);
        return true;
    }

    public Object _commit() {
        Operation operation = uncommitted.poll();
        if (operation != null) {
            try {
                return ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
            } catch (Exception e) {
                throw new OperationException(e);
            }
        } else {
            throw new RuntimeException("No operation to commit");
        }
    }

    public void _rollback() {
        uncommitted.clear();
    }

    GoldenNodeClusterOperationBaseImpl(GoldenNodeCluster cluster) {
        this.cluster = cluster;
    }

    public void _announceServerJoining(Server s) throws ClusterException {
        if (cluster.clusteredServerManager.getServer(s.getId()) == null) {
            LOGGER.debug("Server announced that it is joining. Server: " + s);
            cluster.incomingServer(s);
            cluster.sendOwnServerIdentiy(s);
        }
    }

    public ClusteredObject _receiveClusteredObject(String publicName) throws ClusterException {
        return cluster.clusteredObjectManager.getClusteredObject(publicName);
    }

    public void _announceServerLeaving(Server s) throws ClusterException {
        LOGGER.debug("Server announced that it is leaving. Server: " + s);
        cluster.heartBeatTimer.cancelTaskForServer(s);
        cluster.serverIsDeadOperation(s);
    }

    public void _sendOwnServerIdentity(Server s) throws ClusterException {
        if (cluster.clusteredServerManager.getServer(s.getId()) == null) {
            LOGGER.debug("Server sent its identity: " + s);
            cluster.incomingServer(s);
        }
    }

    public void _addClusteredObject(ClusteredObject obj) throws ClusterException {
        cluster.addClusteredObject(obj);
    }

    public boolean _amIOwnerOf(String publicName) {
        return cluster.amIOwnerOf(publicName);
    }

    @Override
    public Object _op_(Operation operation) throws OperationException {
        if (operation.getObjectPublicName() != null) {
            ClusteredObject co = cluster.clusteredObjectManager.getClusteredObject(operation.getObjectPublicName());
            if (co != null) {
                try {
                    return ReflectionUtils.callMethod(co, operation.getObjectMethod(), operation.getParams());
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            } else {
                LOGGER.debug("peer not ready, clusteredObject not found:" + operation.getObjectPublicName());
                throw new ClusteredObjectNotAvailableException();
            }
        } else {
            try {
                return ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
            } catch (Exception e) {
                throw new OperationException(e);
            }
        }
    }

    public String _ping(String str) {
        return "pong " + cluster.getOwner().getShortId();
    }

    public boolean _acquireLeadershipCommit(String id) {
        return cluster.leaderSelector.acquireLeadershipCommit(id, false);
    }

    public boolean _acquireLeadershipPrepare(String id) {
        return cluster.leaderSelector.acquireLeadershipPrepare(id, false);
    }

    public void _readLock(String publicName) {
        cluster.lockService.readLock(publicName, Server.processId.get());
    }

    public void _writeLock(String publicName) {
        cluster.lockService.writeLock(publicName, Server.processId.get());
    }

    public void _unlockReadLock(String publicName) {
        cluster.lockService.unlockReadLock(publicName, Server.processId.get());
    }

    public void _unlockWriteLock(String publicName) {
        cluster.lockService.unlockWriteLock(publicName, Server.processId.get());
    }
}
