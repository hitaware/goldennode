package com.goldennode.api.goldennodecluster;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Server;
import com.goldennode.api.helper.SystemUtils;

public class HeartbeatTimer {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HeartbeatTimer.class);
    private Timer timer;
    private Cluster cluster;
    private static final int TASK_DELAY = Integer
            .parseInt(SystemUtils.getSystemProperty("1000", "com.goldennode.api.cluster.HeartbeatTimer.taskDelay"));
    public static final int TASK_PERIOD = Integer
            .parseInt(SystemUtils.getSystemProperty("2000", "com.goldennode.api.cluster.HeartbeatTimer.taskPeriod"));
    private static final int TASK_RETRY = Integer
            .parseInt(SystemUtils.getSystemProperty("3", "com.goldennode.api.cluster.HeartbeatTimer.retry"));
    private HashMap<String, Integer> errorCountByServer;
    private HashMap<String, TimerTask> tasks;

    public HeartbeatTimer(Cluster cluster) {
        this.cluster = cluster;
    }

    public void start() {
        timer = new Timer(cluster.getOwner().getShortId() + " Heartbeat Timer");
        errorCountByServer = new HashMap<>();
        tasks = new HashMap<>();
    }

    public void stop() {
        timer.cancel();
        tasks.clear();
        errorCountByServer.clear();
    }

    public void cancelTaskForServer(final Server server) {
        TimerTask task = tasks.get(server.getId());
        if (task != null) {
            task.cancel();
            tasks.remove(server.getId());
        } else {
            LOGGER.warn("Task already cancelled for server " + server);
        }
    }

    public void schedule(final Server server, final HearbeatStatusListener listener) {
        errorCountByServer.put(server.getId(), 0);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    cluster.unicastTCP(server, new Operation(null, "ping", cluster.getOwner().getId()),
                            new RequestOptions());
                } catch (ClusterException e) {
                    Integer count = null;
                    count = errorCountByServer.get(server.getId());
                    if (count == null || count > HeartbeatTimer.TASK_RETRY) {
                        cancel();
                        tasks.remove(server.getId());
                        listener.serverUnreachable(server);
                    } else {
                        LOGGER.error("Can't ping peer. Will retry. Server: " + server + " " + e.toString());
                        errorCountByServer.put(server.getId(), ++count);
                    }
                }
            }
        };
        tasks.put(server.getId(), task);
        timer.schedule(task, HeartbeatTimer.TASK_DELAY, HeartbeatTimer.TASK_PERIOD);
    }
}
