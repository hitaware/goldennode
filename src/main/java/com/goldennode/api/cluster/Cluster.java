package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;

public interface Cluster {

	public void start() throws ClusterException;

	public void stop() throws ClusterException;

	public Server getOwner();

	public Response unicastUDP(Server peer, Operation operation) throws ClusterException;

	public Response unicastTCP(Server peer, Operation operation) throws ClusterException;

	public void multicast(Operation operation) throws ClusterException;

	public List<Response> safeMulticast(Operation operation);// TODO why doesnt
	// it
	// throw exception

	public void attachObject(ClusteredObject obj) throws ClusterException;

	public ClusteredObject getClusteredObject(String publicName);

	public Collection<ClusteredObject> getClusteredObjects();

	public Collection<Server> getPeers();

	public Lock createLock(ClusteredObject obj);

}
