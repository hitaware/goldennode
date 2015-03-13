package com.goldennode.api.snippets;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.OperationBase;
import com.goldennode.api.core.Request;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.core.ServerStateListener;

public class TestGoldenNodeServer {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestGoldenNodeServer.class);

	@Before
	public void init() {
		System.setProperty("goldennodeserver.receiveselfmulticast", "true");

	}

	@Test
	public void testBlockingMulticast() throws ServerException {
		GoldenNodeServer server = null;
		try {
			ProxyClass proxy = new TestGoldenNodeServer().new ProxyClass();
			server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new Integer(3), new Integer(4));
			List<Response> l = server.blockingMulticast(r, 1000l);
			Assert.assertEquals(l.size(), 1);
			Assert.assertEquals(7, ((Integer) l.get(0).getReturnValue()).intValue());
			r = server.prepareRequest("_echo", "Hello ozgen");
			l = server.blockingMulticast(r, 1000l);
			Assert.assertEquals(l.size(), 1);
			Assert.assertNull(l.get(0).getReturnValue());
			r = server.prepareRequest("_getSumException", new Integer(3), new Integer(4));
			l = server.blockingMulticast(r, 1000l);
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
			ProxyClass proxy = new TestGoldenNodeServer().new ProxyClass();
			server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new Integer(3), new Integer(4));
			server.multicast(r);
			r = server.prepareRequest("_echo", "Hello ozgen");
			server.multicast(r);
			r = server.prepareRequest("_getSumException", new Integer(3), new Integer(4));
			server.multicast(r);
		} catch (ServerException e) {
			throw e;
		} finally {
			try {
				sleep(1000);
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}

	}

	@Test
	public void testUnicastUDP() throws ServerException {
		GoldenNodeServer server = null;
		try {
			ProxyClass proxy = new TestGoldenNodeServer().new ProxyClass();
			server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new Integer(3), new Integer(4));
			Response resp = server.unicastUDP(server, r);
			Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
			r = server.prepareRequest("_echo", "Hello ozgen");
			resp = server.unicastUDP(server, r);
			Assert.assertNull(resp.getReturnValue());
			r = server.prepareRequest("_getSumException", new Integer(3), new Integer(4));
			resp = server.unicastUDP(server, r);
			Assert.fail("_getSumException call should have failed");
		} catch (ServerException e) {
			Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
		} finally {
			try {
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}
	}

	@Test
	public void testUnicastTCP() throws ServerException {
		GoldenNodeServer server = null;
		try {
			ProxyClass proxy = new TestGoldenNodeServer().new ProxyClass();
			server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setOperationBase(proxy);
			server.start();
			Request r = server.prepareRequest("_getSum", new Integer(3), new Integer(4));
			Response resp = server.unicastTCP(server, r);
			Assert.assertEquals(7, ((Integer) resp.getReturnValue()).intValue());
			r = server.prepareRequest("_echo", "Hello ozgen");
			resp = server.unicastTCP(server, r);
			Assert.assertNull(resp.getReturnValue());
			r = server.prepareRequest("_getSumException", new Integer(3), new Integer(4));
			resp = server.unicastTCP(server, r);
			Assert.fail("_getSumException call should have failed");
		} catch (ServerException e) {
			Assert.assertTrue(e.getCause().getCause() instanceof RuntimeException);
		} finally {
			try {
				server.stop();
			} catch (ServerException e) {
				throw e;
			}
		}

	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			//
		}
	}

	class ProxyClass implements OperationBase, ServerStateListener {
		public Integer _getSum(Integer param1, Integer param2) {
			LOGGER.debug("getSum(" + param1 + "," + param2 + ")");
			return new Integer(param1.intValue() + param2.intValue());
		}

		public Integer _getSumException(Integer param1, Integer param2) throws Exception {
			throw new RuntimeException("sum exception");

		}

		public void _echo(String param) {
			LOGGER.debug("echo " + param);

		}

		@Override
		public void serverStarted(Server server) {
			LOGGER.debug("Server started." + server.toString());
		}

		@Override
		public void serverStopping(Server server) {
			LOGGER.debug("Server stopped." + server.toString());
		}

		@Override
		public void serverStopped(Server server) {
			//

		}

		@Override
		public void serverStarting(Server server) {
			//

		}

	}

}
