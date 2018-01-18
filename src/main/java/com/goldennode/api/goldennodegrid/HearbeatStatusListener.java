package com.goldennode.api.goldennodegrid;

import com.goldennode.api.core.Peer;

public interface HearbeatStatusListener {
    void peerUnreachable(Peer peer);
}
