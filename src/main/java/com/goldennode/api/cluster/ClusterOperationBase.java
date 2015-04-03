package com.goldennode.api.cluster;

import java.util.concurrent.TimeUnit;

import com.goldennode.api.core.OperationBase;
import com.goldennode.api.helper.ReflectionUtils;

public abstract class ClusterOperationBase implements OperationBase {
	public Object _op_(Operation operation) throws OperationException {
		if (operation.getObjectPublicName() != null) {
			ClusteredObject co = getCluster().getClusteredObject(operation.getObjectPublicName());
			if (co != null) {
				try {
					return ReflectionUtils.callMethod(co, operation.getObjectMethod(), operation.getParams());
				} catch (Exception e) {
					throw new OperationException(e);
				}
			} else {
				throw new OperationException("ClusteredObject not found:" + operation.getObjectPublicName());
			}
		} else {
			try {
				return ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
			} catch (Exception e) {
				throw new OperationException(e);
			}
		}
	}

	public abstract Cluster getCluster();

	public String _ping(String str) {
		return "pong " + getCluster().getOwner().getId();
	}

	public void _lock(String publicName, long timeout) {
		getCluster().serverLock(publicName, timeout);
	}

	public void _unlock(String publicName) {
		getCluster().serverUnlock(publicName);
	}

	public int _newCondition(String lockName) {
		return getCluster().serverNewCondition(lockName);
	}

	public void _await(int conditionId) throws InterruptedException {
		getCluster().serverAwait(conditionId);
	}

	public void _signal(int conditionId) {
		getCluster().serverSignal(conditionId);
	}

	public void _signalAll(int conditionId) {
		getCluster().serverSignalAll(conditionId);
	}

	public void _lockInterruptibly(String lockName, long timeout) throws InterruptedException {
		getCluster().serverLockInterruptibly(lockName, timeout);
	}

	public boolean _tryLock(String lockName, long timeout) {
		return getCluster().serverTryLock(lockName, timeout);
	}

	public boolean _tryLock(String lockName, long timeout, TimeUnit unit, long lockTimeout) throws InterruptedException {
		return getCluster().serverTryLock(lockName, timeout, unit, lockTimeout);
	}

	public boolean _acquireProvisionalLeadership(String id) {
		return getCluster().leaderSelector._acquireProvisionalLeadership(id);
	}

	public boolean _acquireLeadership(String id) {
		return getCluster().leaderSelector._acquireLeadership(id);
	}

	public boolean _releaseProvisionalLeadership(String id) {
		return getCluster().leaderSelector._releaseProvisionalLeadership(id);
	}

	public boolean _releaseLeadership(String id) {
		return getCluster().leaderSelector._releaseLeadership(id);
	}
}
