package com.goldennode.api.goldennodegrid;

import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.grid.GridFactory;

public class GridRunner extends Thread {
    private String peerId;
    private Grid c;

    public GridRunner(String peerId) {
        this.peerId = peerId;
    }

    public void stopGrid() throws GridException {
        c.stop();
        synchronized (this) {
            notify();
        }
    }

    public Grid getGrid() {
        return c;
    }

    public String getPeerId() throws GridException {
        return peerId;
    }

    public String getLeaderId() throws GridException {
        return ((GoldenNodeGrid) c).leaderSelector.getLeaderId();
    }

    private void doJob() {
        try {
            c = GridFactory.getGrid(peerId);
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
        setName(peerId);
        doJob();
    }
}
