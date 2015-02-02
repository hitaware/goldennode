package com.goldennode.api.test;

import java.util.Iterator;
import java.util.List;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Logger;
import com.goldennode.api.core.Request;
import com.goldennode.api.core.Response;

public class TestGoldenNodeServer {
	public static void main(String[] args) {
		System.setProperty("goldennodeserver.receiveselfmulticast", "true");
		new TestGoldenNodeServer();
	}

	public TestGoldenNodeServer() {
		testBlockingMulticast();
		// testUnicastUDP();
		// testMulticast();
		// testTcp();

	}

	private void testMulticast() {
		Logger.debug("****************testMulticast****************");
		try {
			TestProxy proxy = new TestProxy();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();
			Request r = server.prepareRequest("_getSumError", new Integer(3),
					new Integer(4));
			server.multicast(r);
			// server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void testBlockingMulticast() {
		Logger.debug("****************testBlockingMulticast****************");
		try {
			TestProxy proxy = new TestProxy();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();
			Request r = server.prepareRequest("_getSumError", new Integer(3),
					new Integer(4));
			List<Response> lr = server.blockingMulticast(r, 2000l);
			Iterator<Response> iter = lr.iterator();
			while (iter.hasNext()) {
				Response resp = iter.next();
				printResponse(resp);
			}
			// server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void testUnicastUDP() {
		Logger.debug("****************testUnicast****************");
		try {
			TestProxy proxy = new TestProxy();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();
			Request r = server.prepareRequest("_getSumError", new Integer(3),
					new Integer(4));
			Response resp = server.unicastUDP(server, r);
			printResponse(resp);
			// r = server.prepareRequest("_echo", "Hello ozgen");
			// server.unicastUDP(server, r);
			// server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void testTcp() {
		Logger.debug("****************testUnicast****************");
		try {
			TestProxy proxy = new TestProxy();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();
			// Thread.sleep(1000);
			Request r = server.prepareRequest("_getSumError", new Integer(3),
					new Integer(4));
			Response resp = server.unicastTCP(server, r);
			printResponse(resp);
			/*
			 * r = server.prepareRequest("echo", "Hello ozgen");
			 * 
			 * resp = server.tcpCall(server, r); printResponse(resp);
			 */
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printResponse(Response resp) {
		Logger.debug("Response:" + resp.getReturnValue() + " "
				+ resp.getServerFrom());
	}
}
