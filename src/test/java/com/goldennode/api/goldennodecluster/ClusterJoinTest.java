package com.goldennode.api.goldennodecluster;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class ClusterJoinTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusterJoinTest.class);
    private ClusterRunner[] th;

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY * 1 + 2000)
    @RepeatTest(times = 1)
    public void testJoining1() throws Exception {
        LOGGER.debug("testJoining1 start");
        int clusterCount = 5;
        th = new ClusterRunner[clusterCount];
        for (int i = 0; i < clusterCount; i++) {
            th[i] = new ClusterRunner(new Integer(i).toString());
        }
        for (int i = 0; i < clusterCount; i++) {
            LOGGER.debug("Starting serverId= " + th[i].getServerId());
            th[i].start();
        }
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < clusterCount; i++) {
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set),
                set.contains(new Integer(clusterCount - 1).toString()));
        for (int i = 0; i < clusterCount; i++) {
            LOGGER.debug("Stopping serverId= " + th[i].getServerId());
            th[i].stopCluster();
        }
        for (int i = 0; i < clusterCount; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining1 end");
    }

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY * 4 + 5000)
    @RepeatTest(times = 1)
    public void testJoining2() throws Exception {
        LOGGER.debug("testJoining2 start");
        int clusterCount = 10;
        th = new ClusterRunner[clusterCount];
        for (int i = 0; i < clusterCount; i++) {
            th[i] = new ClusterRunner(new Integer(i).toString());
        }
        for (int i = 0; i < clusterCount; i++) {
            if (i == 3) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 6) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 7) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY / 2);
            }
            if (i == 8) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY / 4);
            }
            LOGGER.debug("Starting serverId= " + th[i].getServerId());
            th[i].start();
        }
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < clusterCount; i++) {
            set.add(th[i].getLeaderId() == null ? "N/A for server" + i : th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("2"));
        LOGGER.debug("testJoining2 end");
        for (int i = 0; i < clusterCount; i++) {
            if (i == 2) {
                continue;
            }
            LOGGER.debug("Stopping serverId= " + th[i].getServerId());
            th[i].stopCluster();
        }
        th[2].stopCluster();
        for (int i = 0; i < clusterCount; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining2 end.");
    }

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY * 5 + 10000)
    @RepeatTest(times = 1)
    public void testJoining3() throws Exception {
        LOGGER.debug("testJoining3 start");
        int clusterCount = 10;
        th = new ClusterRunner[clusterCount];
        for (int i = 0; i < clusterCount; i++) {
            th[i] = new ClusterRunner(new Integer(i).toString());
        }
        for (int i = 0; i < clusterCount; i++) {
            if (i == 3) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 6) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
            }
            if (i == 7) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY / 2);
            }
            if (i == 8) {
                Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY / 4);
            }
            LOGGER.debug("Starting serverId= " + th[i].getServerId());
            th[i].start();
        }
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        Set<String> set = new HashSet<>();
        for (int i = 0; i < clusterCount; i++) {
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("2"));
        LOGGER.debug("Stopping serverId= " + th[2].getServerId());
        th[2].stopCluster();
        Thread.sleep(GoldenNodeCluster.SERVER_ANNOUNCING_DELAY + 1000);
        set = new HashSet<>();
        for (int i = 0; i < clusterCount; i++) {
            if (i == 2) {
                continue;
            }
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("9"));
        for (int i = 0; i < clusterCount; i++) {
            if (i == 2) {
                continue;
            }
            LOGGER.debug("Stopping serverId= " + th[i].getServerId());
            th[i].stopCluster();
        }
        for (int i = 0; i < clusterCount; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining3 end");
    }
}
