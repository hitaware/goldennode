package com.goldennode.api.cluster;

import java.util.Collection;

import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;

public interface Cluster {

	public void start() throws ClusterException;

	public void stop() throws ClusterException;

	public Server getOwner();

	public ClusterProxy getProxy();

	public Response unicastUDP(Server peer, Operation operation)
			throws ClusterException;

	public Response unicastTCP(Server peer, Operation operation)
			throws ClusterException;

	public void multicast(Operation operation) throws ClusterException;

	public Response safeMulticast(Operation operation);

	public void attachObject(ClusteredObject obj) throws ClusterException;

	public void detachObject(ClusteredObject obj) throws ClusterException;

	public ClusteredObject getClusteredObject(String publicName);

	public Collection<ClusteredObject> getClusteredObjects();

	public Collection<Server> getPeers();

}
