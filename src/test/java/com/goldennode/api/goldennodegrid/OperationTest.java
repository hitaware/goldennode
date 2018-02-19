package com.goldennode.api.goldennodegrid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.goldennode.api.goldennodegrid.Operation;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class OperationTest extends GoldenNodeJunitRunner {
    private Operation oper1;
    private Operation oper2;
    private Operation oper3;
    private Operation oper4;
    private Operation oper5;

    @Before
    public void init() {
        oper1 = new Operation("publicName1", "doJob");
        oper2 = new Operation("publicName1", "doJob", 1);
        oper3 = new Operation("publicName1", "doJob", "String");
        oper4 = new Operation(null, "doJob", new Object[] { null });
        oper5 = new Operation("publicName1", "doJob", new Object[] { null });
    }

    @Test
    @RepeatTest(times = 1)
    public void testNoParamOnDistributedObject() {
        Assert.assertEquals("_op_", oper1.getMethod());
        Assert.assertEquals("_doJob", oper1.getObjectMethod());
        Assert.assertEquals("publicName1", oper1.getObjectPublicName());
        Assert.assertEquals(0, oper1.getParams().length);
    }

    @Test
    @RepeatTest(times = 1)
    public void testPrimitiveParamOnDistributedObject() {
        Assert.assertEquals("_op_", oper2.getMethod());
        Assert.assertEquals("_doJob", oper2.getObjectMethod());
        Assert.assertEquals("publicName1", oper2.getObjectPublicName());
        Assert.assertEquals(1, oper2.getParams().length);
        Assert.assertEquals(1, oper2.get(0));
    }

    @Test
    @RepeatTest(times = 1)
    public void testObjectParamOnDistributedObjectt() {
        Assert.assertEquals("_op_", oper3.getMethod());
        Assert.assertEquals("_doJob", oper3.getObjectMethod());
        Assert.assertEquals("publicName1", oper3.getObjectPublicName());
        Assert.assertEquals(1, oper3.getParams().length);
        Assert.assertEquals("String", oper3.get(0));
    }

    @Test
    @RepeatTest(times = 1)
    public void testNullParamOnOperationBase() {
        Assert.assertEquals("_op_", oper4.getMethod());
        Assert.assertEquals("_doJob", oper4.getObjectMethod());
        Assert.assertEquals(null, oper4.getObjectPublicName());
        Assert.assertEquals(1, oper4.getParams().length);
        Assert.assertEquals(null, oper4.get(0));
    }

    @Test
    @RepeatTest(times = 1)
    public void testNullParamOnDistributedObject() {
        Assert.assertEquals("_op_", oper5.getMethod());
        Assert.assertEquals("_doJob", oper5.getObjectMethod());
        Assert.assertEquals("publicName1", oper5.getObjectPublicName());
        Assert.assertEquals(1, oper5.getParams().length);
        Assert.assertEquals(null, oper5.get(0));
    }
}
