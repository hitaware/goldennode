package com.goldennode.api.goldennodecluster;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.RepeatRule;

public class ReplicatedMemoryMapTest {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMapTest.class);
	@Rule
	public RepeatRule repeatRule = new RepeatRule();
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
		Thread th1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final Map<String, String> map = c2.newReplicatedMemoryMapInstance("map1");
					for (int i = 0; i < 20; i++) {
						map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
					}
					Thread.sleep(5000);
					counter1 = map.size();
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
					final Map<String, String> map = c2.newReplicatedMemoryMapInstance("map1");
					for (int i = 0; i < 20; i++) {
						map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
					}
					Thread.sleep(5000);
					counter2 = map.size();
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

	@Test
	public void testOperations() throws ClusterException, InterruptedException {
		final Cluster c1 = ClusterFactory.getCluster();
		c1.start();
		final Cluster c2 = ClusterFactory.getCluster();
		c2.start();
		final Map<String, String> map = c1.newReplicatedMemoryMapInstance("map1");
		final Map<String, String> map2 = c2.newReplicatedMemoryMapInstance("map1");
		Assert.assertEquals(map, map2);
		Assert.assertNotSame(map, map2);
		map.put("1", "1V");
		Assert.assertTrue(CollectionUtils.verifyMapContents(map, "1"));
		Assert.assertTrue(map.equals(map2));
		Map<String, String> m = new HashMap<String, String>();
		m.put("t1", "t1V");
		map.putAll(m);
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
