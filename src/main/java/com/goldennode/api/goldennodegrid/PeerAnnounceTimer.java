package com.goldennode.api.goldennodegrid;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.helper.StringUtils;
import com.goldennode.api.helper.SystemUtils;

public class PeerAnnounceTimer {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PeerAnnounceTimer.class);
    private Timer timer;
    private Grid grid;
    private static final int TASK_PERIOD = Integer.parseInt(
            SystemUtils.getSystemProperty("2000", "com.goldennode.api.grid.PeerAnnounceTimer.taskPeriod"));

    public PeerAnnounceTimer(Grid grid) {
        this.grid = grid;
    }

    public void stop() {
        timer.cancel();
    }

    public void schedule() {
        timer = new Timer(StringUtils.shortId(grid.getOwner().getId()) + " PeerAnnounce Timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    grid.multicast(new Operation(null, "announcePeerJoining", grid.getOwner()),
                            new RequestOptions());
                } catch (GridException e) {
                    LOGGER.error("Can't announce peer joining: " + grid.getOwner());
                    // This shouldn't never happen.
                }
            }
        }, 0, PeerAnnounceTimer.TASK_PERIOD);
    }
}
