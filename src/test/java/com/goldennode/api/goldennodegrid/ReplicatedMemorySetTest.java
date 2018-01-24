package com.goldennode.api.goldennodegrid;

import java.util.Set;
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

public class ReplicatedMemorySetTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemorySetTest.class);
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
        Grid c1 = GridFactory.getGrid();
        Grid c2 = GridFactory.getGrid();
        try {
            c1.start();
            c2.start();
            final Grid c1t = c1;
            final Grid c2t = c2;
            Thread th1 = new Thread(() -> {
                try {
                    final Set<String> Set = c1t.newReplicatedMemorySetInstance("Set1");
                    for (int i = 0; i < 20; i++) {
                        Set.add(UUID.randomUUID().toString());
                    }
                    Thread.sleep(1000);
                    counter1 = Set.size();
                } catch (GridException e1) {
                    throw new RuntimeException(e1);
                } catch (InterruptedException e2) {
                    throw new RuntimeException(e2);
                }
            });
            Thread th2 = new Thread(() -> {
                try {
                    final Set<String> Set = c2t.newReplicatedMemorySetInstance("Set1");
                    for (int i = 0; i < 20; i++) {
                        Set.add(UUID.randomUUID().toString());
                    }
                    Thread.sleep(1000);
                    counter2 = Set.size();
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
        } finally {
            if (c1 != null) {
                c1.stop();
            }
            if (c2 != null) {
                c2.stop();
            }
            Assert.assertEquals(40, counter1);
            Assert.assertEquals(40, counter2);
        }
    }

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 2 + 12000)
    @RepeatTest(times = 1)
    public void testOperations() throws GridException, InterruptedException {
        Grid c1 = null;
        Grid c2 = null;
        try {
            c1 = GridFactory.getGrid();
            c2 = GridFactory.getGrid();
            c1.start();
            c2.start();
            final Set<Integer> set = c1.newReplicatedMemorySetInstance("Set1");
            final Set<Integer> set2 = c2.newReplicatedMemorySetInstance("Set1");
            Assert.assertEquals(set, set2);
            Assert.assertNotSame(set, set2);
            Assert.assertTrue(CollectionUtils.verifySetContents(set));
            Assert.assertTrue(set.equals(set2));
            set.add(1);
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 1));
            Assert.assertTrue(set.equals(set2));
            set.clear();
            Assert.assertTrue(CollectionUtils.verifySetContents(set));
            Assert.assertTrue(set.equals(set2));
            set.add(1);
            set.add(2);
            set.add(3);
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 1, 2, 3));
            Assert.assertTrue(set.equals(set2));
            set.add(10);
            set.add(11);
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 1, 2, 3, 10, 11));
            Assert.assertTrue(set.equals(set2));
            set.remove(1);
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 2, 3, 10, 11));
            Assert.assertTrue(set.equals(set2));
            set.remove(10);
            set.remove(11);
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 2, 3));
            Assert.assertTrue(set.equals(set2));
            set.add(10);
            set.add(11);
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 2, 3, 10, 11));
            Assert.assertTrue(set.equals(set2));
            Assert.assertTrue(set.add(12));
            Assert.assertFalse(set.add(10));
            Assert.assertTrue(CollectionUtils.verifySetContents(set, 2, 3, 10, 11, 12));
            Assert.assertTrue(set.equals(set2));
        } finally {
            if (c1 != null) {
                c1.stop();
            }
            if (c2 != null) {
                c2.stop();
            }
        }
    }
}
