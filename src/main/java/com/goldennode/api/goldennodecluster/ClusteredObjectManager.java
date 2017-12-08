package com.goldennode.api.goldennodecluster;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusteredObject;

public class ClusteredObjectManager {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredObjectManager.class);
    private Map<String, ClusteredObject> clusteredObjects = new ConcurrentHashMap<String, ClusteredObject>();
    private Cluster cluster;

    public ClusteredObjectManager(Cluster cluster) {
        this.cluster = cluster;
    }

    public Collection<ClusteredObject> getClusteredObjects() {
        return Collections.unmodifiableCollection(clusteredObjects.values());
    }

    public ClusteredObject getClusteredObject(String publicName) {
        return clusteredObjects.get(publicName);
    }

    public void clearAll() {
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

    public void clearRemoteObjects() {
        for (ClusteredObject co : clusteredObjects.values()) {
            if (!co.getOwnerId().equals(cluster.getOwner().getId())) {
                clusteredObjects.remove(co.getPublicName());
            }
        }

    }
}
