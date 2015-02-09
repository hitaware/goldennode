package com.goldennode.api.test;

import org.junit.Assert;
import org.junit.Test;

import com.goldennode.api.core.ReflectionUtils;

public class TestReflection {

	public String method1(int index) {
		return "1";
	}

	public String method1(Integer index) {
		return "2";
	}

	public String method1(Object index) {
		return "3";
	}

	public String method2(Object o) {
		return "4";
	}

	public String method2(Integer index) {
		return "5";
	}

	public String method3(int index) {
		return "6";
	}

	public String method3(Object index) {
		return "7";
	}

	@Test
	public void test() throws Exception {

		TestReflection rt = new TestReflection();

		Assert.assertEquals(ReflectionUtils.callMethod(rt, "method1",
				new Object[] { new Integer(1) }), "2");
		Assert.assertEquals(ReflectionUtils.callMethod(rt, "method2",
				new Object[] { new Integer(1) }), "5");
		Assert.assertEquals(ReflectionUtils.callMethod(rt, "method3",
				new Object[] { new Integer(1) }), "6");

	}
}