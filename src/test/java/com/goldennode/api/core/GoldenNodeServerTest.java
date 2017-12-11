package com.goldennode.api.core;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;

import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;
import com.goldennode.testutils.RepeatedTestRule;
import com.goldennode.testutils.ThreadUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GoldenNodeServerTest  extends GoldenNodeJunitRunner{
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeServerTest.class);

  
    @Test()
    @RepeatTest(times = 10)
    public void testBlockingMulticast() throws ServerException, InterruptedException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        System.setProperty("com.goldennode.api.core.GoldenNodeServer.receiveSelfMulticast", "true");

        try {
            GoldenNodeServer[] server = new GoldenNodeServer[5];
            for (int i = 0; i < 4; i++) {
                server[i] = new GoldenNodeServer("srv1");
                OperationBaseImpl op = new OperationBaseImpl();
                server[i].addServerStateListener((ServerStateListener) op);
                server[i].setOperationBase(op);
                server[i].start();
            }

            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Request r = server[0].prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            r.setTimeout(1000);
            List<Response> l = server[0].blockingMulticast(r);
            Assert.assertEquals(4, l.size());
            Assert.assertEquals(7, ((Integer) l.get(0).getReturnValue()).intValue());
            r = server[0].prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            r.setTimeout(1000);
            l = server[0].blockingMulticast(r);
            Assert.assertEquals(4, l.size());
            Assert.assertNull(l.get(0).getReturnValue());
            r = server[0].prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            r.setTimeout(1000);
            l = server[0].blockingMulticast(r);
            Assert.assertEquals(4, l.size());
            Assert.assertTrue(l.get(0).getReturnValue() instanceof InvocationTargetException);
            Assert.assertTrue(
                    ((InvocationTargetException) l.get(0).getReturnValue()).getCause() instanceof RuntimeException);
            for (int i = 0; i < 4; i++) {
                server[i].stop();
            }
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        } catch (ServerException e) {
            throw e;
        } finally {
            System.clearProperty("com.goldennode.api.core.GoldenNodeServer.receiveSelfMulticast");
        }
    }

    @Test()
    @RepeatTest(times = 10)
    public void testMulticastSelfReceiveActive() throws ServerException {
        System.setProperty("com.goldennode.api.core.GoldenNodeServer.receiveSelfMulticast", "true");
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        GoldenNodeServer server = null;
        GoldenNodeServer server2 = null;
        try {
            OperationBase proxy1 = new OperationBaseImpl();
            OperationBase proxy2 = new OperationBaseImpl();
            server = new GoldenNodeServer("srv1");
            server.addServerStateListener((ServerStateListener) proxy1);
            server.setOperationBase(proxy1);
            server.start();
            server2 = new GoldenNodeServer("srv2");
            server2.addServerStateListener((ServerStateListener) proxy2);
            server2.setOperationBase(proxy2);
            server2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            server.multicast(r);
            r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            server.multicast(r);
            r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            server.multicast(r);

            server.stop(1000);
            server2.stop(1000);

            Assert.assertEquals(1, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());

            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        } finally {
            System.clearProperty("com.goldennode.api.core.GoldenNodeServer.receiveSelfMulticast");
        }
    }

    @Test()
    @RepeatTest(times = 10)
    public void testMulticastNoSelfReceive() throws ServerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        GoldenNodeServer server = null;
        GoldenNodeServer server2 = null;
        try {
            OperationBase proxy1 = new OperationBaseImpl();
            OperationBase proxy2 = new OperationBaseImpl();
            server = new GoldenNodeServer("srv1");
            server.addServerStateListener((ServerStateListener) proxy1);
            server.setOperationBase(proxy1);
            server.start();
            server2 = new GoldenNodeServer("srv2");
            server2.addServerStateListener((ServerStateListener) proxy2);
            server2.setOperationBase(proxy2);
            server2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            server.multicast(r);
            server.multicast(r);
            r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            server.multicast(r);
            server.multicast(r);
            r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            server.multicast(r);
            server.multicast(r);

            server.stop(1000);
            server2.stop(1000);
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        } finally {

        }
    }

    @Test(expected = ServerException.class)
    @RepeatTest(times = 10)
    public void testUnicastTCP() throws ServerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        GoldenNodeServer server = null;
        GoldenNodeServer server2 = null;
        OperationBase proxy1 = new OperationBaseImpl();
        OperationBase proxy2 = new OperationBaseImpl();
        try {

            server = new GoldenNodeServer("srv1");
            server.addServerStateListener((ServerStateListener) proxy1);
            server.setOperationBase(proxy1);
            server.start();
            server2 = new GoldenNodeServer("srv2");
            server2.addServerStateListener((ServerStateListener) proxy2);
            server2.setOperationBase(proxy2);
            server2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));

            Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            Response resp = null;
            resp = server.unicastTCP(server2, r);
            resp = server.unicastTCP(server2, r);
            Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
            r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            resp = server.unicastTCP(server2, r);
            resp = server.unicastTCP(server2, r);
            Assert.assertNull(resp.getReturnValue());
            //Class should be public and not inner class for this to work.
            r = server.prepareRequest("_nullParamTest", new RequestOptions(), (Object) null);
            resp = server.unicastTCP(server2, r);
            resp = server.unicastTCP(server2, r);
            Assert.assertEquals("x", resp.getReturnValue());
            r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            resp = server.unicastTCP(server2, r);
            resp = server.unicastTCP(server2, r);
            Assert.fail("_getSumException call should have failed");
        } catch (ServerException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
            throw e;
        } finally {
            server.stop(1000);
            server2.stop(1000);
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        }
    }

    @Test(expected = ServerException.class)
    @RepeatTest(times = 100)
    public void testUnicastUDP() throws ServerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        GoldenNodeServer server = null;
        GoldenNodeServer server2 = null;
        OperationBase proxy1 = new OperationBaseImpl();
        OperationBase proxy2 = new OperationBaseImpl();
        try {

            server = new GoldenNodeServer("srv1");
            server.addServerStateListener((ServerStateListener) proxy1);
            server.setOperationBase(proxy1);
            server.start();
            server2 = new GoldenNodeServer("srv2");
            server2.addServerStateListener((ServerStateListener) proxy2);
            server2.setOperationBase(proxy2);
            server2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));

            Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            Response resp = null;
            resp = server.unicastUDP(server2, r);
            resp = server.unicastUDP(server2, r);
            Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
            r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            resp = server.unicastUDP(server2, r);
            resp = server.unicastUDP(server2, r);
            Assert.assertNull(resp.getReturnValue());
            //Class should be public and not inner class for this to work.
            r = server.prepareRequest("_nullParamTest", new RequestOptions(), (Object) null);
            resp = server.unicastUDP(server2, r);
            resp = server.unicastUDP(server2, r);
            Assert.assertEquals("x", resp.getReturnValue());
            r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            resp = server.unicastUDP(server2, r);
            resp = server.unicastUDP(server2, r);
            Assert.fail("_getSumException call should have failed");
        } catch (ServerException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
            throw e;
        } finally {
            server.stop(1000);
            server2.stop(1000);
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        }
    }

}
