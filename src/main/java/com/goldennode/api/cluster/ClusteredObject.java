package com.goldennode.api.cluster;

import java.io.Serializable;
import java.util.Stack;

import com.goldennode.api.core.ReflectionUtils;

public abstract class ClusteredObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private String ownerId;
	private String publicName;
	private transient Cluster cluster;
	private transient Stack<Operation> history;
	private int version = 1;

	protected ClusteredObject() {
		history = new Stack<Operation>();
	}

	/*
	 * public void beginTransaction() { history.clear(); }
	 * 
	 * public void commitTransaction() { history.clear(); }
	 * 
	 * public void rollbackTransaction() throws ObjectOperationException { while
	 * (!history.isEmpty()) { undo(); } }
	 */

	protected void createUndoRecord(Operation operation) {
		version++;
		history.push(operation);
	}

	public Integer _getVersion() {
		return version;
	}

	public Stack<Operation> getHistory() {
		return history;
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
				version--;
			} catch (Exception e) {
				throw new OperationException(e);
			}
		}
	}

	public ClusteredObject(String publicName, String ownerId)
			throws ClusterException {
		this.publicName = publicName;
		this.ownerId = ownerId;
	}

	public ClusteredObject(String publicName, String ownerId, Cluster cluster)
			throws ClusterException {
		this.publicName = publicName;
		this.ownerId = ownerId;
		this.cluster = cluster;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setPublicName(String publicName) {
		this.publicName = publicName;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getPublicName() {
		return publicName;
	}

	public Cluster getCluster() {
		return cluster;
	}

	@Override
	public String toString() {
		return "ClusteredObject [ownerId=" + ownerId + ", publicName="
				+ publicName + ", cluster=" + cluster + "]";
	}

	@Override
	public int hashCode() {
		return publicName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ClusteredObject other = (ClusteredObject) obj;
		if (publicName == null) {
			if (other.publicName != null) {
				return false;
			}
		} else if (!publicName.equals(other.publicName)) {
			return false;
		}
		return true;
	}

}
