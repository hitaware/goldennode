package com.goldennode.api.goldennodegrid;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class GridJoinTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GridJoinTest.class);
    private GridRunner[] th;

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 1 + 4000)
    @RepeatTest(times = 1)
    public void testJoining1() throws Exception {
        LOGGER.debug("testJoining1 start");
        int peerCount = 5;
        th = new GridRunner[peerCount];
        for (int i = 0; i < peerCount; i++) {
            th[i] = new GridRunner(new Integer(i).toString());
        }
        for (int i = 0; i < peerCount; i++) {
            LOGGER.debug("Starting peerId= " + th[i].getPeerId());
            th[i].start();
        }
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < peerCount; i++) {
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set),
                set.contains(new Integer(peerCount - 1).toString()));
        for (int i = 0; i < peerCount; i++) {
            LOGGER.debug("Stopping peerId= " + th[i].getPeerId());
            th[i].stopGrid();
        }
        for (int i = 0; i < peerCount; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining1 end");
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 4 + 10000)
    @RepeatTest(times = 1)
    public void testJoining2() throws Exception {
        LOGGER.debug("testJoining2 start");
        int peerCount = 10;
        th = new GridRunner[peerCount];
        for (int i = 0; i < peerCount; i++) {
            th[i] = new GridRunner(new Integer(i).toString());
        }
        for (int i = 0; i < peerCount; i++) {
            if (i == 3) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 6) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 7) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY / 2);
            }
            if (i == 8) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY / 4);
            }
            LOGGER.debug("Starting peerId= " + th[i].getPeerId());
            th[i].start();
        }
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < peerCount; i++) {
            set.add(th[i].getLeaderId() == null ? "N/A for peer" + i : th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("2"));
        LOGGER.debug("testJoining2 end");
        for (int i = 0; i < peerCount; i++) {
            if (i == 2) {
                continue;
            }
            LOGGER.debug("Stopping peerId= " + th[i].getPeerId());
            th[i].stopGrid();
        }
        th[2].stopGrid();
        for (int i = 0; i < peerCount; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining2 end.");
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 5 + 20000)
    @RepeatTest(times = 1)
    public void testJoining3() throws Exception {
        LOGGER.debug("testJoining3 start");
        int peerCount = 10;
        th = new GridRunner[peerCount];
        for (int i = 0; i < peerCount; i++) {
            th[i] = new GridRunner(new Integer(i).toString());
        }
        for (int i = 0; i < peerCount; i++) {
            if (i == 3) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 6) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 7) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY / 2);
            }
            if (i == 8) {
                Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY / 4);
            }
            LOGGER.debug("Starting peerId= " + th[i].getPeerId());
            th[i].start();
        }
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        Set<String> set = new HashSet<>();
        for (int i = 0; i < peerCount; i++) {
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("2"));
        LOGGER.debug("Stopping peerId= " + th[2].getPeerId());
        th[2].stopGrid();
        Thread.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY + 1000);
        set = new HashSet<>();
        for (int i = 0; i < peerCount; i++) {
            if (i == 2) {
                continue;
            }
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("9"));
        for (int i = 0; i < peerCount; i++) {
            if (i == 2) {
                continue;
            }
            LOGGER.debug("Stopping peerId= " + th[i].getPeerId());
            th[i].stopGrid();
        }
        for (int i = 0; i < peerCount; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining3 end");
    }
}
