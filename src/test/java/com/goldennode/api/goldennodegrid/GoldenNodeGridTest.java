package com.goldennode.api.goldennodegrid;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.core.PeerAlreadyStartedException;
import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.grid.GridFactory;
import com.goldennode.api.helper.ExceptionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.PortUtils;
import com.goldennode.testutils.RepeatTest;

public class GoldenNodeGridTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeGridTest.class);

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 1 + 100)
    @RepeatTest(times = 1)
    public void TestIllegalStartWithoutStopping() throws Throwable {
        Grid c = null;
        try {
            c = GridFactory.getGrid("0");
            c.start();
            validatePortsOpen();
            c.start();
            validatePortsOpen();
        } catch (GridException e) {
            ExceptionUtils.throwCauseIfThereIs(e, PeerAlreadyStartedException.class);
        } finally {
            c.stop();
            validateAllPortsClosed();
        }
    }

    @Test
    @RepeatTest(times = 1)
    public void TestStopWithoutStarting() throws Exception {
        Grid c = null;
        try {
            c = GridFactory.getGrid("1");
        } finally {
            c.stop();
            validateAllPortsClosed();
        }
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 1 + 100)
    @RepeatTest(times = 1)
    public void TestStop() throws Throwable {
        Grid c = null;
        try {
            c = GridFactory.getGrid("2");
            c.start();
            validatePortsOpen();
        } finally {
            c.stop();
            validateAllPortsClosed();
        }
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 1 + 100)
    @RepeatTest(times = 1)
    public void TestStart() throws Throwable {
        Grid c = null;
        try {
            c = GridFactory.getGrid("3");
            c.start();
            validatePortsOpen();
        } finally {
            c.stop();
        }
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 3 + 100)
    @RepeatTest(times = 1)
    public void TestStopStart() throws Throwable {
        Grid c = null;
        try {
            c = GridFactory.getGrid("4");
            c.start();
            c.stop();
            c.start();
            c.stop();
            c.start();
            validatePortsOpen();
        } finally {
            c.stop();
        }
    }

    private void validateAllPortsClosed() {
        LOGGER.trace("validateAllPortsClosed");
        for (int i = 25002; i < 25010; i++) {
            Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
        }
        for (int i = 26002; i < 26010; i++) {
            Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
        }
    }

    private void validatePortsOpen() {
        LOGGER.trace("validatePortsOpen");
        Assert.assertTrue("25002 is closed", !PortUtils.portClosed(25002));
        for (int i = 25003; i < 25010; i++) {
            Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
        }
        Assert.assertTrue("26002 is closed", !PortUtils.portClosed(26002));
        for (int i = 26003; i < 26010; i++) {
            Assert.assertTrue(i + " is open", PortUtils.portClosed(i));
        }
    }
}
