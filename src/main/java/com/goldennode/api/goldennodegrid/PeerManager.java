package com.goldennode.api.goldennodegrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Peer;

public class PeerManager {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);
    private ConcurrentSkipListSet<Peer> peers = new ConcurrentSkipListSet<Peer>();
    private ConcurrentSkipListSet<Peer> all = new ConcurrentSkipListSet<Peer>();
    private Peer owner;

    public PeerManager(Peer owner) {
        this.owner = owner;
        all.add(owner);
    }

    public void removePeer(Peer peer) {
        LOGGER.debug("Peer removed from the grid: " + peer);
        peers.remove(peer);
        all.remove(peer);
    }

    public void addPeer(Peer peer) {
        peers.add(peer);
        all.add(peer);
    }

    public void clear() {
        peers.clear();
        ConcurrentSkipListSet<Peer> allPeersTmp = new ConcurrentSkipListSet<Peer>();
        allPeersTmp.add(owner);
        ConcurrentSkipListSet<Peer> allPeersTmp2 = all;
        all = allPeersTmp;
        allPeersTmp2.clear();
    }

    public Collection<Peer> getPeers() {
        return peers;
    }

   
    public Peer getCandidatePeer() {
        StringBuffer sb = new StringBuffer();
        for (Peer s : all.clone()) {
            sb.append(s.getId() + ", ");
        }
        LOGGER.debug("candidate is " + all.last().getId() + " out of " + all.size() + " peers > "
                + sb.toString());
        return all.last();
    }

    public Collection<Peer> getAllPeers() {
        List<Peer> allPeers = new ArrayList<Peer>();
        allPeers.add(getOwner());
        allPeers.addAll(getPeers());
        return allPeers;
    }

    public Peer getOwner() {
        return owner;
    }

    public Peer getPeer(String id) {
        for (Peer peer : getAllPeers()) {
            if (peer.getId().equals(id)) {
                return peer;
            }
        }
        return null;
    }
}
