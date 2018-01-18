package com.goldennode.api.goldennodegrid;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.grid.GridFactory;
import com.goldennode.api.grid.ReplicatedMemoryCounter;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class ReplicatedMemoryCounterTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCounterTest.class);
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
    public void testCounter() throws GridException, InterruptedException {
        final Grid c1 = GridFactory.getGrid();
        c1.start();
        final Grid c2 = GridFactory.getGrid();
        c2.start();
        Thread th1 = new Thread(() -> {
            try {
                final ReplicatedMemoryCounter counter = c1.newDistributedObjectInstance("counter",
                        ReplicatedMemoryCounter.class);
                for (int i = 0; i < 20; i++) {
                    counter.inccounter();
                    LOGGER.debug("Counter="+counter.getcounter());
                }
                Thread.sleep(1000);
                counter1 = counter.getcounter();
            } catch (GridException e1) {
                throw new RuntimeException(e1);
            } catch (InterruptedException e2) {
                throw new RuntimeException(e2);
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                final ReplicatedMemoryCounter counter = c2.newDistributedObjectInstance("counter",
                        ReplicatedMemoryCounter.class);
                for (int i = 0; i < 20; i++) {
                    counter.inccounter();
                    LOGGER.debug("Counter="+counter.getcounter());
                }
                Thread.sleep(1000);
                counter2 = counter.getcounter();
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
    public void testCounter2() throws GridException, InterruptedException {
        final Grid c1 = GridFactory.getGrid();
        Mockito.when(((GoldenNodeGridOperationBaseImpl)(((GoldenNodeGrid)c1).getOwner().getOperationBase()))..)
        
        final Grid c2 = GridFactory.getGrid();
        c2.start();
        Thread th1 = new Thread(() -> {
            try {
                final ReplicatedMemoryCounter counter = c1.newDistributedObjectInstance("counter",
                        ReplicatedMemoryCounter.class);
                for (int i = 0; i < 20; i++) {
                    counter.inccounter();
                    LOGGER.debug("Counter="+counter.getcounter());
                }
                Thread.sleep(1000);
                counter1 = counter.getcounter();
            } catch (GridException e1) {
                throw new RuntimeException(e1);
            } catch (InterruptedException e2) {
                throw new RuntimeException(e2);
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                final ReplicatedMemoryCounter counter = c2.newDistributedObjectInstance("counter",
                        ReplicatedMemoryCounter.class);
                for (int i = 0; i < 20; i++) {
                    counter.inccounter();
                    LOGGER.debug("Counter="+counter.getcounter());
                }
                Thread.sleep(1000);
                counter2 = counter.getcounter();
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
}
