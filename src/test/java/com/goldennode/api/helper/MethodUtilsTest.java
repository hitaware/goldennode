package com.goldennode.api.helper;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class MethodUtilsTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MethodUtilsTest.class);

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

    public boolean method4() {
        return true;
    }

    public Boolean method5() {
        return true;
    }

    public Boolean method6(String obj) {
        return true;
    }

    @Test
    @RepeatTest(times = 1)
    public void test() throws Exception {
        MethodUtilsTest rt = new MethodUtilsTest();
        Assert.assertEquals("2", ReflectionUtils.callMethod(rt, "method1", new Object[] { new Integer(1) }));
        Assert.assertEquals("5", ReflectionUtils.callMethod(rt, "method2", new Object[] { new Integer(1) }));
        Assert.assertEquals("6", ReflectionUtils.callMethod(rt, "method3", new Object[] { new Integer(1) }));
        Assert.assertEquals(true, ReflectionUtils.callMethod(rt, "method4", new Object[] {}));
        Assert.assertEquals(true, ReflectionUtils.callMethod(rt, "method5", new Object[] {}));
        Assert.assertEquals(true, ReflectionUtils.callMethod(rt, "method6", new Object[] { null }));
    }

    @Test(expected = NoSuchMethodException.class)
    @RepeatTest(times = 1)
    public void test2() throws Exception {
        MethodUtilsTest rt = new MethodUtilsTest();
        ReflectionUtils.callMethod(rt, "method6", new Object[] { null, null });
    }
}