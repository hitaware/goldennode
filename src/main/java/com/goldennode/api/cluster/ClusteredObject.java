package com.goldennode.api.cluster;

import java.io.Serializable;
import java.util.UUID;

import org.slf4j.LoggerFactory;

public abstract class ClusteredObject implements Serializable {
	private static final long serialVersionUID = 1L;
	private String ownerId;
	private String publicName;
	private transient Cluster cluster;
	private String lockClusteredObject = "";
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredObject.class);

	public ClusteredObject() {
		publicName = getClass().getName() + "_" + UUID.randomUUID().toString();
	}

	public ClusteredObject(String publicName) {
		this.publicName = publicName;
	}

	public void setPublicName(String publicName) {
		this.publicName = publicName;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public void setOwnerId(String ownerId) {
		if (ownerId != null) {
			synchronized (lockClusteredObject) {
				if (this.ownerId == null) {
					this.ownerId = ownerId;
					lockClusteredObject.notifyAll();
				} else {
					LOGGER.error("ownerid is not null");
					throw new RuntimeException("Illegal operation");
				}
			}
		}
	}

	public String getOwnerId() {
		try {
			synchronized (lockClusteredObject) {
				while (ownerId == null) {
					lockClusteredObject.wait();
				}
				return ownerId;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	public String getPublicName() {
		return publicName;
	}

	public Cluster getCluster() {
		return cluster;
	}

	@Override
	public String toString() {
		return " > ClusteredObject [ownerId=" + ownerId + ", publicName=" + publicName + "] ";
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

	public Object safeOperate(Operation o) {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			return getCluster().safeMulticast(o);
		} catch (ClusterException e1) {
			throw new RuntimeException(e1);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
	}
}
