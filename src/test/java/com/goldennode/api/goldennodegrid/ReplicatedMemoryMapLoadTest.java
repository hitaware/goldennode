
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

		int items = 5000000;
		final Grid c1 = GridFactory.getGrid();
		c1.start();
		ReplicatedMemoryMap<Integer, Integer> map = new ReplicatedMemoryMap<>();
		for (int i = 0; i < items; i++) {
			map.put(i, i);
		}
		c1.attach(map);
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
		Map<Integer, Integer> map2 = c2.newReplicatedMemoryMapInstance();
		LOGGER.debug("map.size()        =" + map2.size());
		LOGGER.debug("usedMemory       ="
				+ (Runtime.getRuntime().totalMemory() / 1000000 - Runtime.getRuntime().freeMemory() / 1000000));
		Assert.assertEquals(items, map.size());
		Assert.assertEquals(items, map2.size());
	}

	@Test()
	public void testLoad2() throws GridException, InterruptedException {
		final Grid c1 = GridFactory.getGrid();
		c1.start();
		Map<Integer, Integer> map = c1.newReplicatedMemoryMapInstance("map1");
		for (int i = 0; i < 5000000; i++) {
			((ReplicatedMemoryMap) map).put(i, i);
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
