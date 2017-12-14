package com.goldennode.api.goldennodecluster;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;

public class ClusterJoinTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusterJoinTest.class);
    private static int THREAD_COUNT;
    private ClusterRunner[] th;

    @Test
    public void testJoining1() throws Exception {
        LOGGER.debug("testJoining1 start");
        THREAD_COUNT = 5;
        th = new ClusterRunner[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            th[i] = new ClusterRunner(new Integer(i).toString());
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            LOGGER.debug("Starting serverId= " + th[i].getServerId());
            th[i].start();
        }
        Thread.sleep(10000);
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set),
                set.contains(new Integer(THREAD_COUNT - 1).toString()));
        for (int i = 0; i < THREAD_COUNT; i++) {
            LOGGER.debug("Stopping serverId= " + th[i].getServerId());
            th[i].stopCluster();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining1 end");
    }

    @Test
    public void testJoining2() throws Exception {
        LOGGER.debug("testJoining2 start");
        THREAD_COUNT = 10;
        th = new ClusterRunner[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            th[i] = new ClusterRunner(new Integer(i).toString());
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            if (i == 3) {
                Thread.sleep(10000);
            }
            if (i == 6) {
                Thread.sleep(5000);
            }
            if (i == 7) {
                Thread.sleep(2000);
            }
            if (i == 8) {
                Thread.sleep(1000);
            }
            LOGGER.debug("Starting serverId= " + th[i].getServerId());
            th[i].start();
        }
        Thread.sleep(10000);
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            set.add(th[i].getLeaderId() == null ? "N/A for server" + i : th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("2"));
        LOGGER.debug("testJoining2 end");
        for (int i = 0; i < THREAD_COUNT; i++) {
            if (i == 2) {
                continue;
            }
            LOGGER.debug("Stopping serverId= " + th[i].getServerId());
            th[i].stopCluster();
        }
        th[2].stopCluster();
        for (int i = 0; i < THREAD_COUNT; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining2 end.");
    }

    @Test
    public void testJoining3() throws Exception {
        LOGGER.debug("testJoining3 start");
        THREAD_COUNT = 10;
        th = new ClusterRunner[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            th[i] = new ClusterRunner(new Integer(i).toString());
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            if (i == 3) {
                Thread.sleep(10000);
            }
            if (i == 6) {
                Thread.sleep(5000);
            }
            if (i == 7) {
                Thread.sleep(2000);
            }
            if (i == 8) {
                Thread.sleep(1000);
            }
            LOGGER.debug("Starting serverId= " + th[i].getServerId());
            th[i].start();
        }
        Thread.sleep(10000);
        Set<String> set = new HashSet<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("2"));
        LOGGER.debug("Stopping serverId= " + th[2].getServerId());
        th[2].stopCluster();
        Thread.sleep(20000);
        set = new HashSet<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            if (i == 2) {
                continue;
            }
            set.add(th[i].getLeaderId());
        }
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.size() == 1);
        Assert.assertTrue("Leader info: " + CollectionUtils.getContents(set), set.contains("9"));
        for (int i = 0; i < THREAD_COUNT; i++) {
            if (i == 2) {
                continue;
            }
            LOGGER.debug("Stopping serverId= " + th[i].getServerId());
            th[i].stopCluster();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            th[i].join();
        }
        LOGGER.debug("testJoining3 end");
    }
}
