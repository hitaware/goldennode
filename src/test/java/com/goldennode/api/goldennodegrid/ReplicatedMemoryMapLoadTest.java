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

public class ReplicatedMemoryMapLoadTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMapLoadTest.class);

    @Test()
    public void testLoad() throws GridException, InterruptedException {
        final Grid c1 = GridFactory.getGrid();
        c1.start();
        Map<Integer, Integer> map = c1.newReplicatedMemoryMapInstance("map1");
        for (int i = 0; i < 5000000; i++) {
            ((ReplicatedMemoryMap) map).putLocal(i, i);
        }
        LOGGER.debug("map.size()=" + map.size());
        System.gc();
        LOGGER.debug("usedMemory       ="
                + (Runtime.getRuntime().totalMemory() / 1000000 - Runtime.getRuntime().freeMemory() / 1000000));
        final Grid c2 = GridFactory.getGrid();
        c2.start();
        System.gc();
        LOGGER.debug("usedMemory       ="
                + (Runtime.getRuntime().totalMemory() / 1000000 - Runtime.getRuntime().freeMemory() / 1000000));
        System.gc();
        LOGGER.debug("map.size()        =" + c2.newReplicatedMemoryMapInstance("map1").size());
        LOGGER.debug("usedMemory       ="
                + (Runtime.getRuntime().totalMemory() / 1000000 - Runtime.getRuntime().freeMemory() / 1000000));
        
    }
}
