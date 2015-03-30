package com.goldennode.api.cluster;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.LockService;
import com.goldennode.api.core.Request;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.replicatedmemorycluster.TestListOperations;

public class MockGoldenNodeServer extends Server {

	public MockGoldenNodeServer(LockService lockService, String id) throws ServerException {
		super(lockService);
		super.setId(id);
	}

	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestListOperations.class);

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

	@Override
	public void acquireLock(String locker, String processId) {
		LOGGER.debug("acquired lock");

	}

	@Override
	public void releaseLock(String locker, String processId) {
		LOGGER.debug("released lock");

	}

	@Override
	public void createLock(String lockName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteLock(String lockName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void releaseLock(String processId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acquireLock(String processId) {
		// TODO Auto-generated method stub

	}

}
