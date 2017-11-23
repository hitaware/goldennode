package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.goldennode.api.core.MockGoldenNodeServer;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;

public class MockCluster extends Cluster {
    public MockCluster(MockGoldenNodeServer mockGoldenNodeServer) throws ClusterException {
        super();
    }

    @Override
    public Server getOwner() {
        return null;
    }

    @Override
    public Collection<Server> getPeers() {
        return null;
    }

    @Override
    public <T extends ClusteredObject> T newClusteredObjectInstance(String publicName, Class<T> claz)
            throws ClusterException {
        return null;
    }

    @Override
    public <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName) throws ClusterException {
        return null;
    }

    @Override
    public <E> List<E> newReplicatedMemoryListInstance(String publicName) throws ClusterException {
        return null;
    }

    @Override
    public <E> Set<E> newReplicatedMemorySetInstance(String publicName) throws ClusterException {
        return null;
    }

    @Override
    public void multicast(Operation operation, RequestOptions options) throws ClusterException {
    }

    @Override
    public Object safeMulticast(Operation o) throws ClusterException {
        return null;
    }

    @Override
    public MultiResponse tcpMulticast(Collection<Server> peers, Operation operation, RequestOptions options) {
        return null;
    }

    @Override
    public Response unicastTCP(Server server, Operation operation, RequestOptions options) throws ClusterException {
        return null;
    }

    @Override
    public Response unicastUDP(Server remoteServer, Operation operation, RequestOptions options)
            throws ClusterException {
        return null;
    }

    @Override
    public void start() throws ClusterException {
    }

    @Override
    public void stop() throws ClusterException {
    }

    @Override
    public void lock(ClusteredObject co) throws ClusterException {
    }

    @Override
    public void unlock(ClusteredObject co) throws ClusterException {
    }

    @Override
    public void lockInterruptibly(ClusteredObject co) throws ClusterException {
    }

    @Override
    public boolean tryLock(ClusteredObject co) throws ClusterException {
        return false;
    }

    @Override
    public boolean tryLock(ClusteredObject co, long timeout, TimeUnit unit) throws ClusterException {
        return false;
    }

    @Override
    public <T extends ClusteredObject> T attach(T t) throws ClusterException {
        return null;
    }

    @Override
    public Server getCandidateServer() {
        return null;
    }
}
