package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.ClusteredObject;

public class ClusteredObjectManager {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredObjectManager.class);
	private Map<String, ClusteredObject> clusteredObjects = new HashMap<String, ClusteredObject>();

	public synchronized Collection<ClusteredObject> getClusteredObjects() {
		return Collections.unmodifiableCollection(clusteredObjects.values());
	}

	public synchronized ClusteredObject getClusteredObject(String publicName) {
		return clusteredObjects.get(publicName);
	}

	public synchronized void clear() {
		clusteredObjects.clear();
	}

	public synchronized boolean contains(ClusteredObject co) {
		return clusteredObjects.containsValue(co);
	}

	public synchronized boolean contains(String publicName) {
		return clusteredObjects.containsKey(publicName);
	}

	public synchronized void addClusteredObject(ClusteredObject co) {
		clusteredObjects.put(co.getPublicName(), co);
	}
}
