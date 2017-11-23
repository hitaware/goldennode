package com.goldennode.api.goldennodecluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.core.Server;

public class ClusteredServerManager {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredServerManager.class);
	private HashSet<Server> clusteredServers = new HashSet<Server>();
	private TreeSet<Server> allServers = new TreeSet<Server>();
	private Server owner;
	private Cluster cluster;

	public ClusteredServerManager(Server owner, Cluster cluster) {
		this.owner = owner;
		this.cluster = cluster;
		allServers.add(owner);
	}

	public synchronized void removeClusteredServer(Server server) {
		LOGGER.debug("Server removed from the cluster: " + server);
		clusteredServers.remove(server);
		allServers.remove(server);
	}

	public synchronized void addClusteredServer(Server server) {
		LOGGER.debug("Server added to the cluster: " + server);
		clusteredServers.add(server);
		allServers.add(server);
	}

	public synchronized void clear() {
		clusteredServers.clear();
		allServers.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection<Server> getPeers() {
		return (HashSet<Server>) clusteredServers.clone();
	}

	public synchronized Server getClusteredServer(String key) {
		for (Server server : clusteredServers) {
			if (key.equals(server.getId()))
				return server;
		}

		return null;
	}

	public synchronized Server getCandidateServer() {
		StringBuffer sb = new StringBuffer();
		for (Server s : getPeers()) {
			sb.append(s.getId() + ", ");
		}
		LOGGER.debug("candidate is " + allServers.first().getId() + " out of " + allServers.size() + " servers > " + sb.toString());
		
		return allServers.first();
	}

	public synchronized Server getMasterServer(int timeout) {
		try {
			Server server;
			int retry = 0;
			while ((server = getMasterServer_()) == null) {
				if (retry++ == 1) {
					return null;
				}
				wait(timeout);
			}
			return server;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private synchronized Server getMasterServer_() {
		for (Server server : getAllServers()) {
			if (server.isMaster()) {
				return server;
			}
		}
		return null;
	}

	public synchronized void setMasterServer(String id) {
		for (Server server : getAllServers()) {
			if (server.getId().equals(id)) {
				LOGGER.debug("setting master server" + server);
				server.setMaster(true);
				notifyAll();
			} else {
				if (server.isMaster()) {
					LOGGER.error("can't set master server back to non-master" + server);
				}
			}
		}
	}

	public synchronized Collection<Server> getAllServers() {
		List<Server> allServers = new ArrayList<Server>();
		allServers.add(getOwner());
		allServers.addAll(getPeers());
		return allServers;
	}

	public synchronized Server getMasterServer() {
		return getMasterServer(0);
	}

	public synchronized Server getOwner() {
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
