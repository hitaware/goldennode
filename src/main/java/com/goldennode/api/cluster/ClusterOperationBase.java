package com.goldennode.api.cluster;

import com.goldennode.api.core.OperationBase;
import com.goldennode.api.helper.ReflectionUtils;

public abstract class ClusterOperationBase implements OperationBase {

	public Object _op_(Operation operation) throws OperationException {

		if (operation.getObjectPublicName() != null) {
			ClusteredObject co = getCluster().getClusteredObject(
					operation.getObjectPublicName());
			if (co != null) {
				try {
					return ReflectionUtils.callMethod(co,
							operation.getObjectMethod(), operation.getParams());
				} catch (Exception e) {
					throw new OperationException(e);
				}

			} else {
				throw new OperationException("ClusteredObject not found:"
						+ operation.getObjectPublicName());
			}
		} else {
			try {
				return ReflectionUtils.callMethod(this,
						operation.getObjectMethod(), operation.getParams());
			} catch (Exception e) {
				throw new OperationException(e);
			}

		}
	}

	public abstract Cluster getCluster();

	public String _ping(String str) {
		return "pong " + getCluster().getOwner().getId();
	}

	public void _acquireLock(String publicName, String processId) {
		getCluster().acquireLock(publicName, processId);
	}

	public void _releaseLock(String publicName, String processId) {
		getCluster().releaseLock(publicName, processId);
	}

	public void _acquireLock(String processId) {
		getCluster().acquireLock(processId);
	}

	public void _releaseLock(String processId) {
		getCluster().releaseLock(processId);
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
