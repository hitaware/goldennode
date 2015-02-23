package com.goldennode.api.snippets;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Proxy;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.core.ServerStateListener;

public class StartGoldenNodeServer {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StartGoldenNodeServer.class);

	public static void main(String[] args) {
		try {

			ProxyClass proxy = new StartGoldenNodeServer().new ProxyClass();
			GoldenNodeServer server = new GoldenNodeServer();
			server.addServerStateListener(proxy);
			server.setProxy(proxy);
			server.start();

		} catch (ServerException e) {
			LOGGER.error("Error occured", e);
		}

	}

	class ProxyClass implements Proxy, ServerStateListener {
		public Integer _getSum(Integer param1, Integer param2) {
			LOGGER.debug("getSum(" + param1 + "," + param2 + ")");
			return new Integer(param1.intValue() + param2.intValue());
		}

		public Integer _getSumError(Integer param1, Integer param2) throws Exception {
			throw new Exception("sum error");

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
