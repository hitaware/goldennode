package com.goldennode.api.test;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Logger;
import com.goldennode.api.core.ServerException;

public class StartGoldenNodeServer {

	public static void main(String[] args) {
		try {

			TestProxy proxy = new TestProxy();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();

		} catch (ServerException e) {
			Logger.error(e);
		}

	}
}
