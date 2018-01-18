package com.goldennode.api.core;

import java.util.List;

import org.slf4j.LoggerFactory;

public class MockPeerImpl extends Peer {
    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MockPeerImpl.class);

    public MockPeerImpl(String id) throws PeerException {
        super(id);
    }

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
    public void start(int delay) throws PeerException {
        LOGGER.debug("peer started");
    }

    @Override
    public void stop(int delay) throws PeerException {
        LOGGER.debug("peer stopped");
    }

    @Override
    public Response unicastTCP(Peer remotePeer, Request request) throws PeerException {
        LOGGER.debug("unicastTCP sent");
        return null;
    }

    @Override
    public Response unicastUDP(Peer remotePeer, Request request) throws PeerException {
        LOGGER.debug("unicastUDP sent");
        return null;
    }

    @Override
    public void multicast(Request request) throws PeerException {
        LOGGER.debug("multicast sent");
    }

    @Override
    public List<Response> blockingMulticast(Request request) throws PeerException {
        LOGGER.debug("blockingMulticast sent");
        return null;
    }

    @Override
    public Request prepareRequest(String method, RequestOptions options, Object... params) {
        LOGGER.debug("request prepared");
        return null;
    }

    @Override
    public void start() throws PeerException {
        start(0);
    }

    @Override
    public void stop() throws PeerException {
        stop(0);
    }
}
