package com.goldennode.api.goldennodecluster;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.ReplicatedMemoryList;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class ReplicatedMemoryListTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryListTest.class);
    private int counter1;
    private int counter2;

    @Before
    public void init() throws ClusterException {
    }

    @After
    public void teardown() throws ClusterException {
    }

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY * 2 + 15000)
    @RepeatTest(times = 1)
    public void testReplication1() throws ClusterException, InterruptedException {
        final Cluster c1 = ClusterFactory.getCluster();
        c1.start();
        final Cluster c2 = ClusterFactory.getCluster();
        c2.start();
        Thread th1 = new Thread(() -> {
            try {
                final List<String> list = c2.newReplicatedMemoryListInstance("list1");
                for (int i = 0; i < 20; i++) {
                    list.add(UUID.randomUUID().toString());
                }
                Thread.sleep(1000);
                counter1 = list.size();
            } catch (ClusterException e1) {
                throw new RuntimeException(e1);
            } catch (InterruptedException e2) {
                throw new RuntimeException(e2);
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                final List<String> list = c2.newReplicatedMemoryListInstance("list1");
                for (int i = 0; i < 20; i++) {
                    list.add(UUID.randomUUID().toString());
                }
                Thread.sleep(1000);
                counter2 = list.size();
            } catch (ClusterException e1) {
                throw new RuntimeException(e1);
            } catch (InterruptedException e2) {
                throw new RuntimeException(e2);
            }
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
        c1.stop();
        c2.stop();
        Assert.assertEquals(40, counter1);
        Assert.assertEquals(40, counter2);
    }

    @Test(timeout = GoldenNodeCluster.DEFAULT_SERVER_ANNOUNCING_DELAY * 2 + 6000)
    @RepeatTest(times = 1)
    public void testOperations() throws ClusterException, InterruptedException {
        final Cluster c1 = ClusterFactory.getCluster();
        c1.start();
        final Cluster c2 = ClusterFactory.getCluster();
        c2.start();
        final List<Integer> list = c1.newReplicatedMemoryListInstance("list1");
        final List<Integer> list2 = c2.newReplicatedMemoryListInstance("list1");
        Assert.assertEquals(list, list2);
        Assert.assertNotSame(list, list2);
        Assert.assertTrue(CollectionUtils.verifyListContents(list));
        Assert.assertTrue(list.equals(list2));
        list.add(1);
        Assert.assertTrue(CollectionUtils.verifyListContents(list, 1));
        Assert.assertTrue(list.equals(list2));
        list.add(0, 2);
        Assert.assertTrue(CollectionUtils.verifyListContents(list, 2, 1));
        Assert.assertTrue(list.equals(list2));
        list.clear();
        Assert.assertTrue(CollectionUtils.verifyListContents(list));
        Assert.assertTrue(list.equals(list2));
        list.add(1);
        list.add(0, 2);
        list.add(0, 3);
        Assert.assertTrue(CollectionUtils.verifyListContents(list, 3, 2, 1));
        Assert.assertTrue(list.equals(list2));
        list.remove(0);
        Assert.assertTrue(CollectionUtils.verifyListContents(list, 2, 1));
        Assert.assertTrue(list.equals(list2));
        list.add(0, 10);
        list.add(0, 11);
        Assert.assertTrue(CollectionUtils.verifyListContents(list, 11, 10, 2, 1));
        Assert.assertTrue(list.equals(list2));
        list.set(0, 100);
        Assert.assertTrue(CollectionUtils.verifyListContents(list, 100, 10, 2, 1));
        Assert.assertTrue(list.equals(list2));
        list.clear();
        Assert.assertTrue(CollectionUtils.verifyListContents(list));
        Assert.assertTrue(list.equals(list2));
        c1.stop();
        c2.stop();
    }
}
