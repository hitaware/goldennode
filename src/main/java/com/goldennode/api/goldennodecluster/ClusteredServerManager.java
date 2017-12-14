package com.goldennode.api.goldennodecluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Server;

public class ClusteredServerManager {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredServerManager.class);
    private ConcurrentSkipListSet<Server> clusteredServers = new ConcurrentSkipListSet<Server>();
    private ConcurrentSkipListSet<Server> allServers = new ConcurrentSkipListSet<Server>();
    private Server owner;

    public ClusteredServerManager(Server owner) {
        this.owner = owner;
        allServers.add(owner);
    }

    public void removePeer(Server server) {
        LOGGER.debug("Peer removed from the cluster: " + server);
        clusteredServers.remove(server);
        allServers.remove(server);
    }

    public void addPeer(Server server) {
        clusteredServers.add(server);
        allServers.add(server);
    }

    public void clear() {
        clusteredServers.clear();
        ConcurrentSkipListSet<Server> allServersTmp = new ConcurrentSkipListSet<Server>();
        allServersTmp.add(owner);
        ConcurrentSkipListSet<Server> allServersTmp2 = allServers;
        allServers = allServersTmp;
        allServersTmp2.clear();
    }

    public Collection<Server> getPeers() {
        return clusteredServers;
    }

    public Server getPeer(String key) {
        for (Server server : clusteredServers) {
            if (key.equals(server.getId())) {
                return server;
            }
        }
        return null;
    }

    public Server getCandidateServer() {
        StringBuffer sb = new StringBuffer();
        for (Server s : allServers.clone()) {
            sb.append(s.getId() + ", ");
        }
        LOGGER.debug("candidate is " + allServers.last().getId() + " out of " + allServers.size() + " servers > "
                + sb.toString());
        return allServers.last();
    }

    public Collection<Server> getAllServers() {
        List<Server> allServers = new ArrayList<Server>();
        allServers.add(getOwner());
        allServers.addAll(getPeers());
        return allServers;
    }

    public Server getOwner() {
        return owner;
    }

    public synchronized Server getServer(String id) {
        for (Server server : getAllServers()) {
            if (server.getId().equals(id)) {
                return server;
            }
        }
        return null;
    }
}
