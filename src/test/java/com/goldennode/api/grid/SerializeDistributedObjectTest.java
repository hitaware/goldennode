package com.goldennode.api.grid;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.goldennode.api.goldennodegrid.ReplicatedMemoryList;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;
import com.goldennode.testutils.SerializationUtils;
import com.goldennode.testutils.ThreadUtils;

public class SerializeDistributedObjectTest extends GoldenNodeJunitRunner {
	static final int DELAY = 100;

	@Test(timeout = DELAY + 100)
	@RepeatTest(times = 1)
	public void testGetOwnerId1() {
		ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		ThreadUtils.threadInterrupter(Thread.currentThread(), DELAY);
		Assert.assertNull(cl.getOwnerId());
		Thread.interrupted();
	}

	@Test(timeout = DELAY + 100)
	@RepeatTest(times = 1)
	public void testGetOwnerId2() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		ThreadUtils.run(new Runnable() {
			@Override
			public void run() {
				cl.setOwnerId("1");
			}
		}, DELAY);
		Assert.assertEquals("1", cl.getOwnerId());
	}

	@Test
	@RepeatTest(times = 1)
	public void testPublicName1() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		Assert.assertTrue(cl.getPublicName().contains(".ReplicatedMemoryList_"));
	}

	@Test
	@RepeatTest(times = 1)
	public void testPublicName2() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
		cl.setPublicName("1");
		Assert.assertEquals("1", cl.getPublicName());
	}

	@Test
	@RepeatTest(times = 1)
	public void testPublicName3() {
		final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>("1");
		Assert.assertEquals("1", cl.getPublicName());
	}

	@Test
    @RepeatTest(times = 1)
    public void testPublicNameAfterSerialize1() throws IOException, ClassNotFoundException {
        final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>("1");
        cl.add("1");
        Assert.assertEquals("1", cl.getPublicName());
        Assert.assertEquals(1, cl.size());
        ReplicatedMemoryList<String> clCreated = SerializationUtils.serializeDeserialize(cl);
        Assert.assertEquals("1", clCreated.getPublicName());
        Assert.assertEquals(1, clCreated.size());
        Assert.assertNotSame(cl, clCreated);
        Assert.assertEquals(cl, clCreated);
    }

 @Test
    @RepeatTest(times = 1)
    public void testPublicNameAfterSerialize2() throws IOException, ClassNotFoundException {
        final ReplicatedMemoryList<String> cl = new ReplicatedMemoryList<String>();
        cl.add("1");
        Assert.assertTrue(cl.getPublicName().contains(".ReplicatedMemoryList_"));
        Assert.assertEquals(1, cl.size());
        ReplicatedMemoryList<String> clCreated = SerializationUtils.serializeDeserialize(cl);
        Assert.assertEquals(cl.getPublicName(), clCreated.getPublicName());
        Assert.assertEquals(1, clCreated.size());
        Assert.assertNotSame(cl, clCreated);
        Assert.assertEquals(cl, clCreated);
    }
}

