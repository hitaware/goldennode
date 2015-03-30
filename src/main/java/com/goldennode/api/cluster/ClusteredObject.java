package com.goldennode.api.cluster;

import java.io.Serializable;

import com.goldennode.api.core.Server;

public abstract class ClusteredObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private String ownerId;
	private String publicName;
	private transient Cluster cluster;

	protected ClusteredObject() {
		//
	}

	public ClusteredObject(String publicName, String ownerId) {
		this.publicName = publicName;
		this.ownerId = ownerId;

	}

	public ClusteredObject(String publicName, String ownerId, Cluster cluster) {
		this.publicName = publicName;
		this.ownerId = ownerId;
		this.cluster = cluster;
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

	public Server getLockServer() {
		return getOwnerId().equals(cluster.getOwner().getId()) ? cluster
				.getOwner() : cluster.getServer(getOwnerId());
	}

	@Override
	public String toString() {
		return " > ClusteredObject [ownerId=" + ownerId + ", publicName="
				+ publicName + "] ";
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
