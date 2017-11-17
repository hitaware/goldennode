package com.goldennode.api.core;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.goldennodecluster.LockService;

public class MockGoldenNodeServer extends Server {
	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MockGoldenNodeServer.class);

	public MockGoldenNodeServer(String id) throws ServerException {
		super(id);
	}

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
		LOGGER.debug("server started");
	}

	@Override
	public void stop() throws ServerException {
		LOGGER.debug("server stopped");
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
	public List<Response> blockingMulticast(Request request) throws ServerException {
		LOGGER.debug("blockingMulticast sent");
		return null;
	}

	@Override
	public Request prepareRequest(String method, RequestOptions options, Object... params) {
		LOGGER.debug("request prepared");
		return null;
	}

}
