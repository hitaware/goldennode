package com.goldennode.api.test;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Logger;
import com.goldennode.api.core.Proxy;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.core.ServerStateListener;

public class StartGoldenNodeServer {

	public static void main(String[] args) {
		try {

			ProxyClass proxy = new StartGoldenNodeServer().new ProxyClass();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();

		} catch (ServerException e) {
			Logger.error(e);
		}

	}

	class ProxyClass implements Proxy, ServerStateListener {
		public Integer _getSum(Integer param1, Integer param2) {
			Logger.debug("getSum(" + param1 + "," + param2 + ")");
			return new Integer(param1.intValue() + param2.intValue());
		}

		public Integer _getSumError(Integer param1, Integer param2)
				throws Exception {
			throw new Exception("sum error");

		}

		public void _echo(String param) {
			Logger.debug("echo " + param);

		}

		@Override
		public void serverStarted(Server server) {
			Logger.debug("Server started." + server.toString());
		}

		@Override
		public void serverStopping(Server server) {
			Logger.debug("Server stopped." + server.toString());
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
