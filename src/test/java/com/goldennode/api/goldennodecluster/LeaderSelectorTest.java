package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;

public class LeaderSelectorTest {

    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredLockTest.class);

    private Vector<MockClusterForLeader> clusters = new Vector<>();
    private Vector<MockClusterForLeader> clusters2 = new Vector<>();
    

    @Before
    public void init() throws ClusterException {

    }

    @After
    public void teardown() throws ClusterException {

    }

    @Test
    public void test() throws Exception {
        int CLUSTER_COUNT = 9;
        for (int i = 0; i < CLUSTER_COUNT; i++) {

            MockClusterForLeader c = new MockClusterForLeader(new MockServerForLeader(String.valueOf(i + 1)),
                    LeaderSelectorTest.this);
            clusters2.add(c);
        }

        int j = 0;

        for (Cluster c : clusters2) {
            final Cluster cluster = c;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        clusters.add((MockClusterForLeader) cluster);
                        cluster.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }, String.valueOf(++j)).start();
            // Thread.sleep( (int)(Math.random() * 10000));
            Thread.sleep(2000);
        }

        synchronized (this) {
            wait();
        }

    }

    public void sendOwnServerIdentiy(Server fromServer, Server toServer) {
        try {
            for (MockClusterForLeader c : clusters) {

                if (c.getOwner().getId().equals(toServer.getId())) {
                    c._sendOwnServerIdentity(fromServer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MultiResponse tcpMulticast(Server fromServer, Collection<Server> peers, Operation operation) {

        Response r = new Response();
        r.setReturnValue(Boolean.TRUE);
        MultiResponse mr = new MultiResponse();
        Vector<MockClusterForLeader> al= (Vector)clusters.clone();
        for (MockClusterForLeader c : al) {
            if (!c.getOwner().equals(fromServer)) {
                if (peers.contains(c)) {
                    if (operation.getObjectMethod().equals("_acquireProvisionalLeadership")) {
                        c._acquireProvisionalLeadership(fromServer.getId());
                        mr.addSuccessfulResponse(c.getOwner(), r);
                    }
                    if (operation.getObjectMethod().equals("_acquireLeadership")) {
                        c._acquireLeadership(fromServer.getId());
                        mr.addSuccessfulResponse(c.getOwner(), r);
                    }
                    if (operation.getObjectMethod().equals("_releaseProvisionalLeadership")) {
                        c._releaseProvisionalLeadership(fromServer.getId());
                        mr.addSuccessfulResponse(c.getOwner(), r);
                    }
                    if (operation.getObjectMethod().equals("_releaseLeadership")) {
                        c._releaseLeadership(fromServer.getId());
                        mr.addSuccessfulResponse(c.getOwner(), r);
                    }
                }
            }

        }
        return mr;
    }

    public void multicast(Server fromServer, Operation operation, RequestOptions options) {

        try {

            for (MockClusterForLeader c : clusters) {
                if (operation.getObjectMethod().equals("_announceServerJoining")) {
                    c._announceServerJoining(fromServer);
                }
            }
        } catch (ClusterException e) {
            e.printStackTrace();
        }
    }

}
