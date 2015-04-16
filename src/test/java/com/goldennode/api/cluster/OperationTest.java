package com.goldennode.api.cluster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OperationTest {
	private Operation oper1;
	private Operation oper2;
	private Operation oper3;
	private Operation oper4;

	@Before
	public void init() {
		Object[] obj = new Object[1];
		obj[0] = null;
		oper1 = new Operation("publicName1", "doJob");
		oper2 = new Operation("publicName1", "doJob", 1);
		oper3 = new Operation("publicName1", "doJob", obj);
		oper4 = new Operation(null, "doJob", obj);
	}

	@Test
	public void test1() {
		Assert.assertEquals("_op_", oper1.getMethod());
		Assert.assertEquals("_doJob", oper1.getObjectMethod());
		Assert.assertEquals("publicName1", oper1.getObjectPublicName());
		Assert.assertEquals(0, oper1.getParams().length);
	}

	@Test
	public void test2() {
		Assert.assertEquals("_op_", oper2.getMethod());
		Assert.assertEquals("_doJob", oper2.getObjectMethod());
		Assert.assertEquals("publicName1", oper2.getObjectPublicName());
		Assert.assertEquals(1, oper2.getParams().length);
		Assert.assertEquals(1, oper2.get(0));
	}

	@Test
	public void test3() {
		Assert.assertEquals("_op_", oper3.getMethod());
		Assert.assertEquals("_doJob", oper3.getObjectMethod());
		Assert.assertEquals("publicName1", oper3.getObjectPublicName());
		Assert.assertEquals(1, oper3.getParams().length);
		Assert.assertEquals(null, oper3.get(0));
	}

	@Test
	public void test4() {
		Assert.assertEquals("_op_", oper4.getMethod());
		Assert.assertEquals("_doJob", oper4.getObjectMethod());
		Assert.assertEquals(null, oper4.getObjectPublicName());
		Assert.assertEquals(1, oper4.getParams().length);
		Assert.assertEquals(null, oper4.get(0));
	}
}
