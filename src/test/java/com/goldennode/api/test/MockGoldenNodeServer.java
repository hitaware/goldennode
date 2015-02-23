package com.goldennode.api.test;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Request;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.snippets.ListOperations;

public class MockGoldenNodeServer extends Server {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ListOperations.class);

	@Override
	public int getMulticastPort() {
		return 10000;
	}

	@Override
	public int getUnicastUDPPort() {
		return 10001;
	}

	@Override
	public int getUnicastTCPPort() {
		return 10002;
	}

	@Override
	public void start() throws ServerException {
		LOGGER.debug("Server started");

	}

	@Override
	public void stop() throws ServerException {
		LOGGER.debug("Server stopped");

	}

	@Override
	public Request prepareRequest(String method, Object... params) {
		LOGGER.debug("Request prepared");
		return null;
	}

	@Override
	public Response unicastTCP(Server remoteServer, Request request) throws ServerException {
		LOGGER.debug("unicastTCP sent");
		return null;
	}

	@Override
	public Response unicastUDP(Server remoteServer, Request request) throws ServerException {
		LOGGER.debug("unicastUDP sent");
		return null;
	}

	@Override
	public void multicast(Request request) throws ServerException {
		LOGGER.debug("multicast sent");

	}

	@Override
	public List<Response> blockingMulticast(Request request, Long timeout) throws ServerException {
		LOGGER.debug("blockingMulticast sent");
		return null;
	}

}
