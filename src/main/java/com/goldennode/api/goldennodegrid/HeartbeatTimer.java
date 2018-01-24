package com.goldennode.api.goldennodegrid;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.core.Peer;
import com.goldennode.api.helper.StringUtils;
import com.goldennode.api.helper.SystemUtils;

public class HeartbeatTimer {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HeartbeatTimer.class);
    private Timer timer;
    private Grid grid;
    private static final int TASK_DELAY = Integer
            .parseInt(SystemUtils.getSystemProperty("1000", "com.goldennode.api.grid.HeartbeatTimer.taskDelay"));
    public static final int TASK_PERIOD = Integer
            .parseInt(SystemUtils.getSystemProperty("2000", "com.goldennode.api.grid.HeartbeatTimer.taskPeriod"));
    private static final int TASK_RETRY = Integer
            .parseInt(SystemUtils.getSystemProperty("3", "com.goldennode.api.grid.HeartbeatTimer.retry"));
    private HashMap<String, Integer> errorCountByPeer;
    private HashMap<String, TimerTask> tasks;

    public HeartbeatTimer(Grid grid) {
        this.grid = grid;
    }

    public void start() {
        timer = new Timer(StringUtils.shortId(grid.getOwner().getId()) + " Heartbeat Timer");
        errorCountByPeer = new HashMap<>();
        tasks = new HashMap<>();
    }

    public void stop() {
        timer.cancel();
        tasks.clear();
        errorCountByPeer.clear();
    }

    public void cancelTaskForPeer(final Peer peer) {
        TimerTask task = tasks.get(peer.getId());
        if (task != null) {
            task.cancel();
            tasks.remove(peer.getId());
        } else {
            LOGGER.warn("Task already cancelled for peer " + peer);
        }
    }

    public void schedule(final Peer peer, final HearbeatStatusListener listener) {
        errorCountByPeer.put(peer.getId(), 0);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    grid.unicastTCP(peer, new Operation(null, "ping", grid.getOwner().getId()),
                            new RequestOptions());
                } catch (GridException e) {
                    Integer count = null;
                    count = errorCountByPeer.get(peer.getId());
                    if (count == null || count > HeartbeatTimer.TASK_RETRY) {
                        cancel();
                        tasks.remove(peer.getId());
                        listener.peerUnreachable(peer);
                    } else {
                        LOGGER.error("Can't ping peer. Will retry. Peer: " + peer + " " + e.toString());
                        errorCountByPeer.put(peer.getId(), ++count);
                    }
                }
            }
        };
        tasks.put(peer.getId(), task);
        timer.schedule(task, HeartbeatTimer.TASK_DELAY, HeartbeatTimer.TASK_PERIOD);
    }
}
