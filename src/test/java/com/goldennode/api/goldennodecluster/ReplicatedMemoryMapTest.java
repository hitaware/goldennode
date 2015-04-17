package com.goldennode.api.goldennodecluster;

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
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.ReplicatedMemoryMap;
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
		System.out.println(((ReplicatedMemoryMap<String, String>) map).getVersion());
	}

	@Test
	public void testUndo() throws ClusterException, InterruptedException {
		Map<String, String> m = new ReplicatedMemoryMap<String, String>();
		Assert.assertEquals(1, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m));
		m.put("1", "1v");
		Assert.assertEquals(2, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "1"));
		m.put("2", "2v");
		Assert.assertEquals(3, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "1", "2"));
		m.remove("1");
		Assert.assertEquals(4, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "2"));
		m.clear();
		Assert.assertEquals(5, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m));
		m.put("3", "3v");
		Assert.assertEquals(6, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "3"));
		((ClusteredObject) m).undo(6);
		Assert.assertEquals(5, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m));
		((ClusteredObject) m).undo(5);
		Assert.assertEquals(4, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "2"));
		((ClusteredObject) m).undo(4);
		Assert.assertEquals(3, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "1", "2"));
		((ClusteredObject) m).undo(3);
		Assert.assertEquals(2, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m, "1"));
		((ClusteredObject) m).undo(2);
		Assert.assertEquals(1, ((ClusteredObject) m).getVersion());
		Assert.assertTrue(CollectionUtils.verifyMapContents(m));
	}
}
