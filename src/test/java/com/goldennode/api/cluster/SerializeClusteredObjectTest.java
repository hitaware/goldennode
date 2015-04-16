package com.goldennode.api.cluster;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.goldennode.testutils.SerializationUtils;
import com.goldennode.testutils.ThreadUtils;

public class SerializeClusteredObjectTest {
	@Before
	public void init() {
		Thread.interrupted();
	}

	@Test
	public void testGetOwnerId1() {
		ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		ThreadUtils.threadInterrupter(Thread.currentThread(), 500);
		Assert.assertNull(cl.getOwnerId());
	}

	@Test
	public void testGetOwnerId2() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		ThreadUtils.run(new Runnable() {
			@Override
			public void run() {
				cl.setOwnerId("1");
			}
		}, 500);
		Assert.assertEquals("1", cl.getOwnerId());
	}

	@Test
	public void testPublicName1() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		Assert.assertTrue(cl.getPublicName().contains(".ReplicatedMemoryList_"));
	}

	@Test
	public void testPublicName2() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		cl.setPublicName("1");
		Assert.assertEquals("1", cl.getPublicName());
	}

	@Test
	public void testPublicName3() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>("1");
		Assert.assertEquals("1", cl.getPublicName());
	}

	@Test
	public void testPublicNameAfterSerialize1() throws IOException, ClassNotFoundException {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>("1");
		cl.innerList.add("1");
		Assert.assertEquals("1", cl.getPublicName());
		Assert.assertEquals(1, cl.size());
		ReplicatedMemoryList<String> clCreated = SerializationUtils.serializeDeserialize(cl);
		Assert.assertEquals("1", clCreated.getPublicName());
		Assert.assertEquals(1, clCreated.size());
		Assert.assertNotSame(cl, clCreated);
		Assert.assertEquals(cl, clCreated);
	}

	@Test
	public void testPublicNameAfterSerialize2() throws IOException, ClassNotFoundException {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		cl.innerList.add("1");
		Assert.assertTrue(cl.getPublicName().contains(".ReplicatedMemoryList_"));
		Assert.assertEquals(1, cl.size());
		ReplicatedMemoryList<String> clCreated = SerializationUtils.serializeDeserialize(cl);
		Assert.assertEquals(cl.getPublicName(), clCreated.getPublicName());
		Assert.assertEquals(1, clCreated.size());
		Assert.assertNotSame(cl, clCreated);
		Assert.assertEquals(cl, clCreated);
	}
}
