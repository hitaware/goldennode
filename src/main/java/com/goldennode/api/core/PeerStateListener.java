package com.goldennode.api.core;

public interface PeerStateListener {
    void peerStarted(Peer peer);

    void peerStopping(Peer peer);

    void peerStopped(Peer peer);

    void peerStarting(Peer peer);
}
