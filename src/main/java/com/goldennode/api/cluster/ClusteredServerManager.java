package com.goldennode.api.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Server;

public class ClusteredServerManager {
	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(ClusteredServerManager.class);

	private Map<String, Server> clusteredServers = new HashMap<String, Server>();

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

	public Collection<Server> getClusteredServers() {
		return new ArrayList<Server>(clusteredServers.values());
	}

	public Server getClusteredServer(String key) {
		return clusteredServers.get(key);
	}

}
