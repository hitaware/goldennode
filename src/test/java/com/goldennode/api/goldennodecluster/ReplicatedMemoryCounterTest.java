package com.goldennode.api.goldennodecluster;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ReplicatedMemoryCounter;
import com.goldennode.testutils.GoldenNodeJunitRunner;

public class ReplicatedMemoryCounterTest  extends GoldenNodeJunitRunner{
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCounterTest.class);

    private int counter1;
    private int counter2;

    @Before
    public void init() throws ClusterException {
    }

    @After
    public void teardown() throws ClusterException {
    }

    @Test
    public void test1() throws ClusterException, InterruptedException {
        final Cluster c1 = ClusterFactory.getCluster();
        c1.start();
        final Cluster c2 = ClusterFactory.getCluster();
        c2.start();
        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ReplicatedMemoryCounter counter = c1.newClusteredObjectInstance("counter",
                            ReplicatedMemoryCounter.class);
                    for (int i = 0; i < 20; i++) {
                        counter.inccounter();
                    }
                    Thread.sleep(5000);
                    counter1 = counter.getcounter();
                } catch (ClusterException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ReplicatedMemoryCounter counter = c2.newClusteredObjectInstance("counter",
                            ReplicatedMemoryCounter.class);
                    for (int i = 0; i < 20; i++) {
                        counter.inccounter();
                    }
                    Thread.sleep(5000);
                    counter2 = counter.getcounter();
                } catch (ClusterException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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
