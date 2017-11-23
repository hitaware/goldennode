package com.goldennode.api.goldennodecluster;

import java.util.List;

import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.GoldenNodeServer;
import com.goldennode.api.core.Request;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.api.core.ServerStateListener;

public class MockServerForLeader extends Server {

    private static final long serialVersionUID = 1L;

    public MockServerForLeader(String serverId) throws ServerException {
        super(serverId);
    }

    @Override
    public int getMulticastPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUnicastUDPPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUnicastTCPPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void start() throws ServerException {
        for (ServerStateListener listener : getServerStateListeners()) {
            listener.serverStarted(MockServerForLeader.this);
        }

    }

    @Override
    public void stop() throws ServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public Request prepareRequest(String method, RequestOptions options, Object... params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response unicastTCP(Server remoteServer, Request request) throws ServerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response unicastUDP(Server remoteServer, Request request) throws ServerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void multicast(Request request) throws ServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Response> blockingMulticast(Request request) throws ServerException {
        // TODO Auto-generated method stub
        return null;
    }

}
