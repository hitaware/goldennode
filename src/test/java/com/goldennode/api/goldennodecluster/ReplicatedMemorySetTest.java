package com.goldennode.api.goldennodecluster;

import java.util.Set;
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
import com.goldennode.api.cluster.ReplicatedMemorySet;
import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;

public class ReplicatedMemorySetTest  extends GoldenNodeJunitRunner{
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemorySetTest.class);

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
		Cluster c1 = ClusterFactory.getCluster();
		Cluster c2 = ClusterFactory.getCluster();
		try {

			c1.start();
			c2.start();
			final Cluster c1t = c1;
			final Cluster c2t = c2;
			Thread th1 = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						final Set<String> Set = c1t.newReplicatedMemorySetInstance("Set1");
						for (int i = 0; i < 20; i++) {
							Set.add(UUID.randomUUID().toString());
						}
						Thread.sleep(5000);
						counter1 = Set.size();
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
						final Set<String> Set = c2t.newReplicatedMemorySetInstance("Set1");
						for (int i = 0; i < 20; i++) {
							Set.add(UUID.randomUUID().toString());
						}
						Thread.sleep(5000);
						counter2 = Set.size();
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

	@Test
	public void testOperations() throws ClusterException, InterruptedException {
		Cluster c1 = null;
		Cluster c2 = null;

		try {
			c1 = ClusterFactory.getCluster();
			c2 = ClusterFactory.getCluster();
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

	@Test
	public void testUndo() throws ClusterException, InterruptedException {
		Set<String> s = new ReplicatedMemorySet<String>();
		Assert.assertEquals(1, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s));
		s.add("1");
		Assert.assertEquals(2, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s, "1"));
		s.clear();
		Assert.assertEquals(3, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s));
		s.add("2");
		s.add("3");
		Assert.assertEquals(5, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s, "2", "3"));
		s.remove("2");
		Assert.assertEquals(6, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s, "3"));
		((ClusteredObject) s).undo(6);
		Assert.assertEquals(5, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s, "2", "3"));
		((ClusteredObject) s).undo(5);
		((ClusteredObject) s).undo(4);
		Assert.assertEquals(3, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s));
		((ClusteredObject) s).undo(3);
		Assert.assertEquals(2, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s, "1"));
		((ClusteredObject) s).undo(2);
		Assert.assertEquals(1, ((ClusteredObject) s).getVersion());
		Assert.assertTrue(CollectionUtils.verifySetContents(s));

	}
}
