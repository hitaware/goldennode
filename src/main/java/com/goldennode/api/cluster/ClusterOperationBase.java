package com.goldennode.api.cluster;

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

}
