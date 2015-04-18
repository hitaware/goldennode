package com.goldennode.api.goldennodecluster;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterOperationBase;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.OperationException;
import com.goldennode.api.cluster.PeerNotReadyException;
import com.goldennode.api.core.Server;
import com.goldennode.api.helper.ReflectionUtils;

public class GoldenNodeClusterOperationBaseImpl extends ClusterOperationBase {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeClusterOperationBaseImpl.class);
	GoldenNodeCluster cluster;

	GoldenNodeClusterOperationBaseImpl(GoldenNodeCluster cluster) {
		this.cluster = cluster;
	}

	public void _announceServerJoining(Server s) throws ClusterException {
		cluster.incomingServer(s);
		cluster.sendOwnServerIdentiy(s);
	}

	public ClusteredObject _receiveClusteredObject(String publicName) throws ClusterException {
		return cluster.clusteredObjectManager.getClusteredObject(publicName);
	}

	public void _announceServerLeaving(Server s) throws ClusterException {
		LOGGER.debug("server is leaving" + s);
	}

	public void _sendOwnServerIdentity(Server s) {
		cluster.incomingServer(s);
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
				throw new PeerNotReadyException();
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
		return "pong " + cluster.getOwner().getId();
	}

	public boolean _acquireProvisionalLeadership(String id) {
		return cluster.leaderSelector.acquireProvisionalLeadership(id);
	}

	public boolean _acquireLeadership(String id) {
		return cluster.leaderSelector.acquireLeadership(id);
	}

	public boolean _releaseProvisionalLeadership(String id) {
		return cluster.leaderSelector.releaseProvisionalLeadership(id);
	}

	public boolean _releaseLeadership(String id) {
		return cluster.leaderSelector.releaseLeadership(id);
	}

	public void _lock(String publicName, long timeout) {
		cluster.serverLock(publicName, timeout);
	}

	public void _unlock(String publicName) {
		cluster.serverUnlock(publicName);
	}

	public int _newCondition(String lockName) {
		return cluster.serverNewCondition(lockName);
	}

	public void _await(int conditionId) throws InterruptedException {
		cluster.serverAwait(conditionId);
	}

	public void _signal(int conditionId) {
		cluster.serverSignal(conditionId);
	}

	public void _signalAll(int conditionId) {
		cluster.serverSignalAll(conditionId);
	}

	public void _lockInterruptibly(String lockName, long timeout) throws InterruptedException {
		cluster.serverLockInterruptibly(lockName, timeout);
	}

	public boolean _tryLock(String lockName, long timeout) {
		return cluster.serverTryLock(lockName, timeout);
	}

	public boolean _tryLock(String lockName, long timeout, TimeUnit unit, long lockTimeout) throws InterruptedException {
		return cluster.serverTryLock(lockName, timeout, unit, lockTimeout);
	}
}
