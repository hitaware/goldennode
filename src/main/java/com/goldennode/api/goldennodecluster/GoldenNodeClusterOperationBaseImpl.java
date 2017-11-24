package com.goldennode.api.goldennodecluster;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterOperationBase;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.OperationException;
import com.goldennode.api.cluster.ClusteredObjectNotAvailableException;
import com.goldennode.api.core.Server;
import com.goldennode.api.helper.ReflectionUtils;

public class GoldenNodeClusterOperationBaseImpl extends ClusterOperationBase {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeClusterOperationBaseImpl.class);
	GoldenNodeCluster cluster;

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

	public void _sendOwnServerIdentity(Server s) {
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

	public boolean _acquireLeadership(String id) {
		return cluster.leaderSelector.acquireLeadership(id);
	}

	public boolean _releaseLeadership(String id) {
		return cluster.leaderSelector.releaseLeadership(id);
	}

	public void _lock(String publicName) {
		cluster.lockService.lock(publicName, Server.processId.get());
	}

	public void _unlock(String publicName) {
		cluster.lockService.unlock(publicName, Server.processId.get());
	}

	public void _lockInterruptibly(String lockName) throws InterruptedException {
		cluster.lockService.lockInterruptibly(lockName, Server.processId.get());
	}

	public boolean _tryLock(String lockName, long timeout) {
		return cluster.lockService.tryLock(lockName, Server.processId.get(), timeout);
	}

	public boolean _tryLock(String lockName, long timeout, TimeUnit unit) throws InterruptedException {
		return cluster.lockService.tryLock(lockName, Server.processId.get(), timeout, unit);
	}
}
