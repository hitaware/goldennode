package com.goldennode.api.goldennodecluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Server;

public class ClusteredServerManager {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredServerManager.class);
	private Map<String, Server> clusteredServers = new ConcurrentHashMap<String, Server>();
	private Server owner;

	public ClusteredServerManager(Server owner) {
		this.owner = owner;
	}

	public void removeClusteredServer(Server server) {
		LOGGER.debug("Server removed from the cluster: " + server);
		clusteredServers.remove(server.getId());
	}

	public void addClusteredServer(Server server) {
		LOGGER.debug("Server added to the cluster: " + server);
		clusteredServers.put(server.getId(), server);
	}

	public void clear() {
		clusteredServers.clear();
	}

	public Collection<Server> getServers() {
		return new ArrayList<Server>(clusteredServers.values());
	}

	public synchronized Server getClusteredServer(String key) {
		return clusteredServers.get(key);
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
		allServers.addAll(getServers());
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
