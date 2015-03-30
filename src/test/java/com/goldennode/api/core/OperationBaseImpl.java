package com.goldennode.api.core;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.OperationBase;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerStateListener;

public class OperationBaseImpl implements OperationBase, ServerStateListener {
	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(OperationBaseImpl.class);

	public Integer _getSum(Integer param1, Integer param2) {
		LOGGER.debug("getSum(" + param1 + "," + param2 + ")");
		return new Integer(param1.intValue() + param2.intValue());
	}

	public Integer _getSumException(Integer param1, Integer param2)
			throws Exception {
		throw new RuntimeException("sum exception");

	}

	public void _echo(String param) {
		LOGGER.debug("echo " + param);

	}

	public String _nullParamTest(String param) {
		LOGGER.debug("Param is null? param = " + null);
		return "x";

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