package com.goldennode.api.goldennodegrid;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.grid.GridFactory;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class ReplicatedMemoryListTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryListTest.class);
    private int counter1;
    private int counter2;

    @Before
    public void init() throws GridException {
    }

    @After
    public void teardown() throws GridException {
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 2 + 30000)
    @RepeatTest(times = 1)
    public void testReplication1() throws GridException, InterruptedException {
        final Grid c1 = GridFactory.getGrid();
        c1.start();
        final Grid c2 = GridFactory.getGrid();
        c2.start();
        Thread th1 = new Thread(() -> {
            try {
                final List<String> list = c2.newReplicatedMemoryListInstance("list1");
                for (int i = 0; i < 20; i++) {
                    list.add(UUID.randomUUID().toString());
                }
                Thread.sleep(1000);
                counter1 = list.size();
            } catch (GridException e1) {
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
            } catch (GridException e1) {
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

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 2 + 30000)
    @RepeatTest(times = 1)
    public void testOperations() throws GridException, InterruptedException {
        final Grid c1 = GridFactory.getGrid();
        c1.start();
        final Grid c2 = GridFactory.getGrid();
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
