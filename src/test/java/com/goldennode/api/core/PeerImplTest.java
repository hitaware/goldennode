package com.goldennode.api.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.ExceptionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;
import com.goldennode.testutils.ThreadUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PeerImplTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PeerImplTest.class);
    static final int BLOCKING_MULTICAST_TIMEOUT = 500;
    static final int MULTICAST_DELAY = 500;

    @Test(timeout = BLOCKING_MULTICAST_TIMEOUT * 3 + 500)
    @RepeatTest(times = 1)
    public void testBlockingMulticast() throws PeerException, InterruptedException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        System.setProperty("com.goldennode.api.core.GoldenNodePeer.receiveSelfMulticast", "true");
        try {
            PeerImpl[] peer = new PeerImpl[5];
            for (int i = 0; i < 4; i++) {
                peer[i] = new PeerImpl("srv1");
                OperationBaseImpl op = new OperationBaseImpl();
                peer[i].addPeerStateListener(op);
                peer[i].setOperationBase(op);
                peer[i].start();
            }
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Request r = peer[0].prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            r.setTimeout(BLOCKING_MULTICAST_TIMEOUT);
            List<Response> l = peer[0].blockingMulticast(r);
            Assert.assertEquals(4, l.size());
            Assert.assertEquals(7, ((Integer) l.get(0).getReturnValue()).intValue());
            r = peer[0].prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            r.setTimeout(BLOCKING_MULTICAST_TIMEOUT);
            l = peer[0].blockingMulticast(r);
            Assert.assertEquals(4, l.size());
            Assert.assertNull(l.get(0).getReturnValue());
            r = peer[0].prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            r.setTimeout(BLOCKING_MULTICAST_TIMEOUT);
            l = peer[0].blockingMulticast(r);
            Assert.assertEquals(4, l.size());
            Assert.assertTrue(l.get(0).getReturnValue() instanceof InvocationTargetException);
            Assert.assertTrue(
                    ((InvocationTargetException) l.get(0).getReturnValue()).getCause() instanceof RuntimeException);
            for (int i = 0; i < 4; i++) {
                peer[i].stop();
            }
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        } catch (PeerException e) {
            throw e;
        } finally {
            System.clearProperty("com.goldennode.api.core.GoldenNodePeer.receiveSelfMulticast");
        }
    }

    @Test(timeout = MULTICAST_DELAY * 2 + 500)
    @RepeatTest(times = 1)
    public void testMulticastSelfReceiveActive() throws PeerException {
        System.setProperty("com.goldennode.api.core.GoldenNodePeer.receiveSelfMulticast", "true");
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        PeerImpl peer = null;
        PeerImpl peer2 = null;
        try {
            OperationBase proxy1 = new OperationBaseImpl();
            OperationBase proxy2 = new OperationBaseImpl();
            peer = new PeerImpl("srv1");
            peer.addPeerStateListener((PeerStateListener) proxy1);
            peer.setOperationBase(proxy1);
            peer.start();
            peer2 = new PeerImpl("srv2");
            peer2.addPeerStateListener((PeerStateListener) proxy2);
            peer2.setOperationBase(proxy2);
            peer2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Request r = peer.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            peer.multicast(r);
            r = peer.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            peer.multicast(r);
            r = peer.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            peer.multicast(r);
            peer.stop(MULTICAST_DELAY);
            peer2.stop(MULTICAST_DELAY);
            Assert.assertEquals(1, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        } finally {
            System.clearProperty("com.goldennode.api.core.GoldenNodePeer.receiveSelfMulticast");
        }
    }

    @Test(timeout = MULTICAST_DELAY * 2 + 500)
    @RepeatTest(times = 1)
    public void testMulticastNoSelfReceive() throws PeerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        PeerImpl peer = null;
        PeerImpl peer2 = null;
        try {
            OperationBase proxy1 = new OperationBaseImpl();
            OperationBase proxy2 = new OperationBaseImpl();
            peer = new PeerImpl("srv1");
            peer.addPeerStateListener((PeerStateListener) proxy1);
            peer.setOperationBase(proxy1);
            peer.start();
            peer2 = new PeerImpl("srv2");
            peer2.addPeerStateListener((PeerStateListener) proxy2);
            peer2.setOperationBase(proxy2);
            peer2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Request r = peer.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            peer.multicast(r);
            peer.multicast(r);
            r = peer.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            peer.multicast(r);
            peer.multicast(r);
            r = peer.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            peer.multicast(r);
            peer.multicast(r);
            peer.stop(MULTICAST_DELAY);
            peer2.stop(MULTICAST_DELAY);
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

    @Test(timeout = 1500)
    @RepeatTest(times = 1)
    public void testUnicastTCP() throws PeerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        PeerImpl peer = null;
        PeerImpl peer2 = null;
        OperationBase proxy1 = new OperationBaseImpl();
        OperationBase proxy2 = new OperationBaseImpl();
        try {
            peer = new PeerImpl("srv1");
            peer.addPeerStateListener((PeerStateListener) proxy1);
            peer.setOperationBase(proxy1);
            peer.start();
            peer2 = new PeerImpl("srv2");
            peer2.addPeerStateListener((PeerStateListener) proxy2);
            peer2.setOperationBase(proxy2);
            peer2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Request r = peer.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            Response resp = null;
            resp = peer.unicastTCP(peer2, r);
            resp = peer.unicastTCP(peer2, r);
            Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
            r = peer.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            resp = peer.unicastTCP(peer2, r);
            resp = peer.unicastTCP(peer2, r);
            Assert.assertNull(resp.getReturnValue());
            //Class should be public and not inner class for this to work.
            r = peer.prepareRequest("_nullParamTest", new RequestOptions(), (Object) null);
            resp = peer.unicastTCP(peer2, r);
            resp = peer.unicastTCP(peer2, r);
            Assert.assertEquals("x", resp.getReturnValue());
            r = peer.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            resp = peer.unicastTCP(peer2, r);
            resp = peer.unicastTCP(peer2, r);
        } catch (PeerException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
            throw e;
        } finally {
            peer.stop();
            peer2.stop();
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        }
    }

    @Test(expected = PeerException.class, timeout = 1000)
    @RepeatTest(times = 1)
    public void testUnicastUDP() throws PeerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        PeerImpl peer = null;
        PeerImpl peer2 = null;
        OperationBase proxy1 = new OperationBaseImpl();
        OperationBase proxy2 = new OperationBaseImpl();
        try {
            peer = new PeerImpl("srv1");
            peer.addPeerStateListener((PeerStateListener) proxy1);
            peer.setOperationBase(proxy1);
            peer.start();
            peer2 = new PeerImpl("srv2");
            peer2.addPeerStateListener((PeerStateListener) proxy2);
            peer2.setOperationBase(proxy2);
            peer2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Request r = peer.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
            Response resp = null;
            resp = peer.unicastUDP(peer2, r);
            resp = peer.unicastUDP(peer2, r);
            Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
            r = peer.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
            resp = peer.unicastUDP(peer2, r);
            resp = peer.unicastUDP(peer2, r);
            Assert.assertNull(resp.getReturnValue());
            //Class should be public and not inner class for this to work.
            r = peer.prepareRequest("_nullParamTest", new RequestOptions(), (Object) null);
            resp = peer.unicastUDP(peer2, r);
            resp = peer.unicastUDP(peer2, r);
            Assert.assertEquals("x", resp.getReturnValue());
            r = peer.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
            resp = peer.unicastUDP(peer2, r);
            resp = peer.unicastUDP(peer2, r);
            Assert.fail("_getSumException call should have failed");
        } catch (PeerException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
            throw e;
        } finally {
            peer.stop();
            peer2.stop();
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getEchoCalled());
            Assert.assertEquals(0, ((OperationBaseImpl) proxy1).getGetSumExceptionCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getGetSumCalled());
            Assert.assertEquals(2, ((OperationBaseImpl) proxy2).getEchoCalled());
            Assert.assertEquals(1, ((OperationBaseImpl) proxy2).getGetSumExceptionCalled());
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        }
    }

    @Test(expected = PeerException.class)
    @RepeatTest(times = 1)
    public void testUnicastUDPBigData() throws PeerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        PeerImpl peer = null;
        PeerImpl peer2 = null;
        OperationBase proxy1 = new OperationBaseImpl();
        OperationBase proxy2 = new OperationBaseImpl();
        try {
            peer = new PeerImpl("srv1");
            peer.addPeerStateListener((PeerStateListener) proxy1);
            peer.setOperationBase(proxy1);
            peer.start();
            peer2 = new PeerImpl("srv2");
            peer2.addPeerStateListener((PeerStateListener) proxy2);
            peer2.setOperationBase(proxy2);
            peer2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Map map = new HashMap<>();
            for (int i = 0; i < 100000; i++) {
                map.put(i, i);
            }
            Request r = peer.prepareRequest("_putMap", new RequestOptions(), map);
            Response resp = peer.unicastUDP(peer2, r);
            Assert.assertEquals(100000, ((Integer) resp.getReturnValue()).intValue());
        } catch (PeerException e) {
            Assert.assertTrue(ExceptionUtils.hasCause(e, IOException.class));
            throw e;
        } finally {
            peer.stop();
            peer2.stop();
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        }
    }
    
    @Test()
    @RepeatTest(times = 1)
    public void testUnicastTCPBigData() throws PeerException {
        Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        PeerImpl peer = null;
        PeerImpl peer2 = null;
        OperationBase proxy1 = new OperationBaseImpl();
        OperationBase proxy2 = new OperationBaseImpl();
        try {
            peer = new PeerImpl("srv1");
            peer.addPeerStateListener((PeerStateListener) proxy1);
            peer.setOperationBase(proxy1);
            peer.start();
            peer2 = new PeerImpl("srv2");
            peer2.addPeerStateListener((PeerStateListener) proxy2);
            peer2.setOperationBase(proxy2);
            peer2.start();
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv1"));
            Assert.assertTrue(ThreadUtils.hasThreadNamedLike("srv2"));
            Map map = new HashMap<>();
            for (int i = 0; i < 100000; i++) {
                map.put(i, i);
            }
            Request r = peer.prepareRequest("_putMap", new RequestOptions(), map);
            Response resp = peer.unicastTCP(peer2, r);
            Assert.assertEquals(100000, ((Integer) resp.getReturnValue()).intValue());
        } catch (PeerException e) {
            throw e;
        } finally {
            peer.stop();
            peer2.stop();
            Assert.assertFalse(ThreadUtils.hasThreadNamedLike("srv"));
        }
    }
}
