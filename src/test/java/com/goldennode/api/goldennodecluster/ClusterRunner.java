package com.goldennode.api.goldennodecluster;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;

public class ClusterRunner extends Thread {
    private String serverId;
    private Cluster c;

    public ClusterRunner(String serverId) {
        this.serverId = serverId;
    }

    public void stopCluster() throws ClusterException {
        c.stop();
        synchronized (this) {
            notify();
        }
    }

    public Cluster getCluster() {
        return c;
    }

    public String getServerId() throws ClusterException {
        return serverId;
    }

    public String getLeaderId() throws ClusterException {
        return ((GoldenNodeCluster) c).leaderSelector.getLeaderId();
    }

    private void doJob() {
        try {
            c = ClusterFactory.getCluster(serverId);
            c.start();
            synchronized (this) {
                wait();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        setName(serverId);
        doJob();
    }
}
