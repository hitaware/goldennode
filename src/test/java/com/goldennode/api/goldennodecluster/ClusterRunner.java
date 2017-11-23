package com.goldennode.api.goldennodecluster;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.core.Server;

public class ClusterRunner extends Thread {
	private String clusterId;
	private Cluster c;

	public ClusterRunner(String clusterId) {
		this.clusterId = clusterId;
	}

	public void stopCluster() throws ClusterException {
		c.stop();
		interrupt();
	}

	public String getLeaderId() throws ClusterException {
		Server s = ((GoldenNodeCluster) c).clusteredServerManager.getMasterServer();
		if (s != null) {
			return s.getId();
		} else {
			return null;
		}
	}

	private void doJob() {
		try {
			c = ClusterFactory.getCluster(clusterId);
			c.start();
			synchronized (this) {
				wait();
			}
		} catch (InterruptedException e) {
			// leave thread
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		doJob();
	}
}
