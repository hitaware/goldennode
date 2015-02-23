package com.goldennode.api.bigmemorycluster;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterProxy;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;

public class BigMemoryCluster implements Cluster {

	public BigMemoryCluster(Server server) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start() throws ClusterException {

	}

	@Override
	public void stop() throws ClusterException {

	}

	@Override
	public Server getOwner() {

		return null;
	}

	@Override
	public ClusterProxy getProxy() {

		return null;
	}

	@Override
	public Response unicastUDP(Server peer, Operation operation) throws ClusterException {

		return null;
	}

	@Override
	public Response unicastTCP(Server peer, Operation operation) throws ClusterException {

		return null;
	}

	@Override
	public void multicast(Operation operation) throws ClusterException {

	}

	@Override
	public List<Response> safeMulticast(Operation operation) {

		return null;
	}

	@Override
	public ClusteredObject getClusteredObject(String publicName) {

		return null;
	}

	@Override
	public Collection<ClusteredObject> getClusteredObjects() {

		return null;
	}

	@Override
	public Collection<Server> getPeers() {

		return null;
	}

	@Override
	public Lock createLock(ClusteredObject obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attachObject(ClusteredObject obj) throws ClusterException {
		// TODO Auto-generated method stub

	}

}
