package com.goldennode.api.cluster;

import java.util.Stack;

import com.goldennode.api.core.Proxy;
import com.goldennode.api.core.ReflectionUtils;

public abstract class ClusterProxy implements Proxy {

	public Stack<Operation> history = new Stack<Operation>();
	private int version = 1;

	public void createUndoRecord(Operation operation) {
		version++;
		history.push(operation);
	}

	public Integer _getVersion() {
		return version;
	}

	public void undoLatest(int lastestVersion) {

		if (version != lastestVersion) {
			throw new OperationException(lastestVersion
					+ " is not the latest version");
		}
		if (version == 1) {
			throw new OperationException("Object in initial state");
		}
		Operation operation = history.pop();
		if (operation != null) {
			try {
				ReflectionUtils.callMethod(this, operation.getObjectMethod(),
						operation.getParams());
			} catch (Exception e) {
				throw new OperationException(e);
			}
		}

	}

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

}
