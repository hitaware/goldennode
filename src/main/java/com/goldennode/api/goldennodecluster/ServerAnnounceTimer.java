package com.goldennode.api.goldennodecluster;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.helper.SystemUtils;

public class ServerAnnounceTimer {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServerAnnounceTimer.class);
    private Timer timer;
    private Cluster cluster;
    private static final int TASK_PERIOD = Integer.parseInt(
            SystemUtils.getSystemProperty("2000", "com.goldennode.api.cluster.ServerAnnounceTimer.taskPeriod"));

    public ServerAnnounceTimer(Cluster cluster) {
        this.cluster = cluster;
    }

    public void stop() {
        timer.cancel();
    }

    public void schedule() {
        timer = new Timer(cluster.getOwner().getShortId() + " ServerAnnounce Timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    cluster.multicast(new Operation(null, "announceServerJoining", cluster.getOwner()),
                            new RequestOptions());
                } catch (ClusterException e) {
                    LOGGER.error("Can't announce server joining: " + cluster.getOwner());
                    // This shouldn't never happen.
                }
            }
        }, 0, ServerAnnounceTimer.TASK_PERIOD);
    }
}
