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

    @Test
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
                Thread.sleep(5000);
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
                Thread.sleep(5000);
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

    @Test
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

    @Test
    public void testUndo() throws ClusterException, InterruptedException {
        List<String> l = new ReplicatedMemoryList<String>();
        Assert.assertEquals(1, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l));
        l.add("1");
        Assert.assertEquals(2, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "1"));
        l.add(0, "2");
        Assert.assertEquals(3, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "2", "1"));
        l.remove(1);
        Assert.assertEquals(4, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "2"));
        l.clear();
        Assert.assertEquals(5, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l));
        l.add("3");
        Assert.assertEquals(6, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "3"));
        l.add("4");
        Assert.assertEquals(7, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "3", "4"));
        l.remove("4");
        Assert.assertEquals(8, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "3"));
        l.set(0, "5");
        Assert.assertEquals(9, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "5"));
        ((ClusteredObject) l).undo(9);
        Assert.assertEquals(8, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "3"));
        ((ClusteredObject) l).undo(8);
        Assert.assertEquals(7, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "3", "4"));
        ((ClusteredObject) l).undo(7);
        Assert.assertEquals(6, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "3"));
        ((ClusteredObject) l).undo(6);
        Assert.assertEquals(5, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l));
        ((ClusteredObject) l).undo(5);
        Assert.assertEquals(4, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "2"));
        ((ClusteredObject) l).undo(4);
        Assert.assertEquals(3, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "2", "1"));
        ((ClusteredObject) l).undo(3);
        Assert.assertEquals(2, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l, "1"));
        ((ClusteredObject) l).undo(2);
        Assert.assertEquals(1, ((ClusteredObject) l).getVersion());
        Assert.assertTrue(CollectionUtils.verifyListContents(l));
    }
}
