package com.goldennode.api.test;

import com.goldennode.api.cluster.Operation;
import com.goldennode.api.cluster.OperationException;
import com.goldennode.api.core.Logger;
import com.goldennode.api.core.Proxy;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerStateListener;

public class TestProxy implements Proxy, ServerStateListener {
	public Integer _getSum(Integer param1, Integer param2) {
		Logger.debug("getSum(" + param1 + "," + param2 + ")");
		return new Integer(param1.intValue() + param2.intValue());
	}

	public Integer _getSumError(Integer param1, Integer param2)
			throws Exception {
		// Thread.sleep(2000);
		throw new Exception("sum error");
		// return new Integer(param1.intValue() + param2.intValue());

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

	public void createUndoRecord(Operation operation) {
		// TODO Auto-generated method stub

	}

	public void undoLatest(int latestVersion) throws OperationException {
		// TODO Auto-generated method stub

	}

	public Integer _getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

}
