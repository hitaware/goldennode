package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public class ClusteredObjectManager {
	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(ClusteredObjectManager.class);

	private Map<String, ClusteredObject> clusteredObjects = new HashMap<String, ClusteredObject>();

	public Collection<ClusteredObject> getClusteredObjects() {

		return Collections.unmodifiableCollection(clusteredObjects.values());

	}

	public ClusteredObject getClusteredObject(String publicName) {

		return clusteredObjects.get(publicName);

	}

	public void clear() {

		clusteredObjects.clear();

	}

	public boolean contains(ClusteredObject co) {

		return clusteredObjects.containsValue(co);

	}

	public boolean contains(String publicName) {

		return clusteredObjects.containsKey(publicName);

	}

	public void addClusteredObject(ClusteredObject co) {

		clusteredObjects.put(co.getPublicName(), co);
	}
}
