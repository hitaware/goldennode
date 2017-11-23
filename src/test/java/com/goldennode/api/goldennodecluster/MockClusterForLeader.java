package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.core.ServerStateListener;
import com.goldennode.api.helper.LockHelper;
import com.goldennode.api.helper.SystemUtils;

public class MockClusterForLeader extends Cluster implements RequestListener {

    private Server server;
    private LeaderSelector leaderSelector;
    private ClusteredServerManager clusteredServerManager;
    private LeaderSelectorTest lst;

    private static final int HANDSHAKING_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("5000",
            "com.goldennode.api.goldennodecluster.GoldenNodeCluster.handshakingDelay"));
    private static final int WAITFORMASTER_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("10000",
            "com.goldennode.api.goldennodecluster.GoldenNodeCluster.waitForMasterDelay"));
    private static final int LOCK_TIMEOUT = Integer.parseInt(SystemUtils.getSystemProperty("60000",
            "com.goldennode.api.goldennodecluster.GoldenNodeCluster.lockTimeout"));
    private static final int START_RETRY_COUNT = Integer.parseInt(SystemUtils.getSystemProperty("3",
            "com.goldennode.api.goldennodecluster.GoldenNodeCluster.startRetryCount"));
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MockClusterForLeader.class);

    public MockClusterForLeader(Server server, LeaderSelectorTest lst) {
        this.server = server;
        clusteredServerManager = new ClusteredServerManager(server, this);
        server.addServerStateListener(new ServerStateListener() {

            @Override
            public void serverStopping(Server server) {
                // TODO Auto-generated method stub

            }

            @Override
            public void serverStopped(Server server) {
                // TODO Auto-generated method stub

            }

            @Override
            public void serverStarting(Server server) {
                // TODO Auto-generated method stub

            }

            @Override
            public void serverStarted(Server server) {
                try {
                    LOGGER.debug("***server started... id : " + server.getShortId());
                    multicast(new Operation(null, "announceServerJoining", server), new RequestOptions());
                } catch (ClusterException e) {
                    LOGGER.error("Error Occured", e);
                }

            }
        });

        leaderSelector = new LeaderSelector(this, new LeaderSelectionListener() {
            @Override
            public void leaderChanged(String newLeaderId) {
                //clusteredServerManager.setMasterServer(newLeaderId);
            }
        });
        this.lst = lst;
    }

    @Override
    public Server getOwner() {
        return server;
    }

    void sendOwnServerIdentiy(Server fromServer, Server toServer) throws ClusterException {
        lst.sendOwnServerIdentiy(fromServer, toServer);

    }

    void incomingServer(final Server server) {
        clusteredServerManager.addClusteredServer(server);
        if (server.isMaster()) {
            LOGGER.debug("new incoming master server" + server);
            leaderSelector.setLeaderId(server.getId());
        } else {
            LOGGER.debug("new incoming non-master server" + server);
        }
    }

    public void _announceServerJoining(Server s) throws ClusterException {
        incomingServer(s);
        sendOwnServerIdentiy(server, s);
    }

    public void _sendOwnServerIdentity(Server s) {
        incomingServer(s);

    }

    public boolean _acquireProvisionalLeadership(String id) {
        return leaderSelector.acquireProvisionalLeadership(id);
    }

    public boolean _acquireLeadership(String id) {
        return leaderSelector.acquireLeadership(id);
    }

    public boolean _releaseProvisionalLeadership(String id) {
        return leaderSelector.releaseProvisionalLeadership(id);
    }

    public boolean _releaseLeadership(String id) {
        return leaderSelector.releaseLeadership(id);
    }

    @Override
    public void start() throws ClusterException {
        int retry = 0;
        for (;;) {
            retry++;
            if (retry > START_RETRY_COUNT) {
                throw new ClusterException("Can not start");
            }
            try {
                getOwner().start();
            } catch (ServerException e) {
                throw new ClusterException(e);
            }
            // Wait for handshaking of peers
            LockHelper.sleep(HANDSHAKING_DELAY);
            leaderSelector.candidateDecisionLogic();
            if (clusteredServerManager.getMasterServer() == null) {
                LOGGER.debug("REBOOTING... (" + retry + ")");
                stop();
            } else {
                break;
            }
        }

    }

    @Override
    public void stop() throws ClusterException {
        clusteredServerManager.clear();
        leaderSelector.reset();

    }

    @Override
    public Collection<Server> getPeers() {
        return clusteredServerManager.getPeers();
    }

    @Override
    public <T extends ClusteredObject> T attach(T t) throws ClusterException {

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
        lst.multicast(server, operation, options);
    }

    @Override
    public Object safeMulticast(Operation o) throws ClusterException {

        return null;
    }

    @Override
    public MultiResponse tcpMulticast(Collection<Server> peers, Operation operation, RequestOptions options) {

        return lst.tcpMulticast(getOwner(), peers, operation);

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
    protected void lock(ClusteredObject co) throws ClusterException {

    }

    @Override
    protected void unlock(ClusteredObject co) throws ClusterException {

    }

    @Override
    protected void lockInterruptibly(ClusteredObject co) throws ClusterException {

    }

    @Override
    protected boolean tryLock(ClusteredObject co) throws ClusterException {

        return false;
    }

    @Override
    protected boolean tryLock(ClusteredObject co, long timeout, TimeUnit unit) throws ClusterException {

        return false;
    }

    @Override
    public Server getCandidateServer() {
        return clusteredServerManager.getCandidateServer();
    }

}
