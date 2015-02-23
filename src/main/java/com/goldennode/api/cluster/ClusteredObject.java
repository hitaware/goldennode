package com.goldennode.api.cluster;

public abstract class ClusteredObject {

	private String ownerId;
	private String publicName;
	private transient Cluster cluster;

	protected ClusteredObject() {
		//
	}

	public ClusteredObject(String publicName, String ownerId) throws ClusterException {
		this.publicName = publicName;
		this.ownerId = ownerId;

	}

	public ClusteredObject(String publicName, String ownerId, Cluster cluster) throws ClusterException {
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

	@Override
	public String toString() {
		return "ClusteredObject [ownerId=" + ownerId + ", publicName=" + publicName + ", cluster=" + cluster + "]";
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
