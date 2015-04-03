package com.goldennode.api.core;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.LockHelper;

public class TestGoldenNodeServer {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestGoldenNodeServer.class);

	@BeforeClass
	public static void init() {
		System.setProperty("com.goldennode.api.core.GoldenNodeServer.receiveSelfMulticast", "true");
	}

	@Test
	public void testBlockingMulticast() throws ServerException {
		GoldenNodeServer server = null;
		try {
			OperationBase proxy = new OperationBaseImpl();
			LockService lockService = new LockServiceImpl();
			server = new GoldenNodeServer(lockService);
			server.addServerStateListener((ServerStateListener) proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
			r.setTimeout(1);
			List<Response> l = server.blockingMulticast(r);
			Assert.assertEquals(l.size(), 1);
			Assert.assertEquals(7, ((Integer) l.get(0).getReturnValue()).intValue());
			r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
			r.setTimeout(1);
			l = server.blockingMulticast(r);
			Assert.assertEquals(l.size(), 1);
			Assert.assertNull(l.get(0).getReturnValue());
			r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
			r.setTimeout(1);
			l = server.blockingMulticast(r);
			Assert.assertEquals(l.size(), 1);
			Assert.assertTrue(l.get(0).getReturnValue() instanceof InvocationTargetException);
			Assert.assertTrue(((InvocationTargetException) l.get(0).getReturnValue()).getCause() instanceof RuntimeException);
		} catch (ServerException e) {
			throw e;
		} finally {
			try {
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}
	}

	@Test
	public void testMulticast() throws ServerException {
		GoldenNodeServer server = null;
		try {
			OperationBase proxy = new OperationBaseImpl();
			LockService lockService = new LockServiceImpl();
			server = new GoldenNodeServer(lockService);
			server.addServerStateListener((ServerStateListener) proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
			server.multicast(r);
			r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
			server.multicast(r);
			r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
			server.multicast(r);
		} finally {
			try {
				LockHelper.sleep(1000);
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}
	}

	@Test(expected = ServerException.class)
	public void testUnicastUDP() throws ServerException {
		GoldenNodeServer server = null;
		try {
			OperationBase proxy = new OperationBaseImpl();
			LockService lockService = new LockServiceImpl();
			server = new GoldenNodeServer(lockService);
			server.addServerStateListener((ServerStateListener) proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
			Response resp = server.unicastUDP(server, r);
			Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
			r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
			resp = server.unicastUDP(server, r);
			Assert.assertNull(resp.getReturnValue());
			r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
			resp = server.unicastUDP(server, r);
			Assert.fail("_getSumException call should have failed");
		} catch (ServerException e) {
			Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
			throw e;
		} finally {
			try {
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}
	}

	@Test(expected = ServerException.class)
	public void testUnicastUDP2() throws ServerException {
		GoldenNodeServer server = null;
		try {
			OperationBase proxy = new OperationBaseImpl();
			LockService lockService = new LockServiceImpl();
			server = new GoldenNodeServer(lockService);
			server.addServerStateListener((ServerStateListener) proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
			server.unicastUDP(server, r);
		} catch (ServerException e) {
			Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
			throw e;
		} finally {
			try {
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}
	}

	@Test(expected = ServerException.class)
	public void testUnicastTCP() throws ServerException {
		GoldenNodeServer server = null;
		try {
			OperationBase proxy = new OperationBaseImpl();
			LockService lockService = new LockServiceImpl();
			server = new GoldenNodeServer(lockService);
			server.addServerStateListener((ServerStateListener) proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new RequestOptions(), new Integer(3), new Integer(4));
			Response resp = server.unicastTCP(server, r);
			Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
			r = server.prepareRequest("_echo", new RequestOptions(), "Hello ozgen");
			resp = server.unicastTCP(server, r);
			Assert.assertNull(resp.getReturnValue());
			// TODO Class should be public for this to work. Fix it, it should
			// work even if the class is an inner class
			r = server.prepareRequest("_nullParamTest", new RequestOptions(), (Object) null);
			resp = server.unicastTCP(server, r);
			Assert.assertEquals("x", resp.getReturnValue());
			r = server.prepareRequest("_getSumException", new RequestOptions(), new Integer(3), new Integer(4));
			resp = server.unicastTCP(server, r);
			Assert.fail("_getSumException call should have failed");
		} catch (ServerException e) {
			Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
			throw e;
		} finally {
			try {
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}
	}
}
