package com.goldennode.api.goldennodegrid;

import java.util.Map;
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

public class ReplicatedMemoryMapTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMapTest.class);
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
                final Map<String, String> map = c2.newReplicatedMemoryMapInstance("map1");
                for (int i = 0; i < 20; i++) {
                    map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
                }
                Thread.sleep(1000);
                counter1 = map.size();
            } catch (GridException e1) {
                throw new RuntimeException(e1);
            } catch (InterruptedException e2) {
                throw new RuntimeException(e2);
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                final Map<String, String> map = c2.newReplicatedMemoryMapInstance("map1");
                for (int i = 0; i < 20; i++) {
                    map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
                }
                Thread.sleep(1000);
                counter2 = map.size();
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

    @Test(timeout = GoldenNodeGrid.DEFAULT_PEER_ANNOUNCING_DELAY * 2 + 20000)
    @RepeatTest(times = 1)
    public void testOperations() throws GridException, InterruptedException {
        final Grid c1 = GridFactory.getGrid();
        c1.start();
        final Grid c2 = GridFactory.getGrid();
        c2.start();
        final Map<String, String> map = c1.newReplicatedMemoryMapInstance("map1");
        final Map<String, String> map2 = c2.newReplicatedMemoryMapInstance("map1");
        Assert.assertEquals(map, map2);
        Assert.assertNotSame(map, map2);
        map.put("1", "1V");
        Assert.assertTrue(CollectionUtils.verifyMapContents(map, "1"));
        Assert.assertTrue(map.equals(map2));
        map.put("t1", "t1V");
        Assert.assertTrue(CollectionUtils.verifyMapContents(map, "1", "t1"));
        Assert.assertTrue(map.equals(map2));
        map.remove("1");
        Assert.assertTrue(CollectionUtils.verifyMapContents(map, "t1"));
        Assert.assertTrue(map.equals(map2));
        map.clear();
        Assert.assertTrue(CollectionUtils.verifyMapContents(map));
        Assert.assertTrue(map.equals(map2));
        c1.stop();
        c2.stop();
    }
}
