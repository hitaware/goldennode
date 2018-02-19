package com.goldennode.api.goldennodegrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Peer;
import com.goldennode.api.grid.Grid;

public class DistributedObjectManager {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedObjectManager.class);
    private Map<String, DistributedObject> distributedObjects = new ConcurrentHashMap<String, DistributedObject>();
    private Grid grid;

    public DistributedObjectManager(Grid grid) {
        this.grid = grid;
    }

    public Collection<DistributedObject> getDistributedObjects() {
        return Collections.unmodifiableCollection(distributedObjects.values());
    }

    public DistributedObject getDistributedObject(String publicName) {
        return distributedObjects.get(publicName);
    }

    public void clearAll() {
        distributedObjects.clear();
    }

    public boolean contains(DistributedObject co) {
        return distributedObjects.containsValue(co);
    }

    public boolean contains(String publicName) {
        return distributedObjects.containsKey(publicName);
    }

    public void addDistributedObject(DistributedObject co) {
        distributedObjects.put(co.getPublicName(), co);
    }

    public void clearRemoteObjects() {
        for (DistributedObject co : distributedObjects.values()) {
            if (!co.getOwnerId().equals(grid.getOwner().getId())) {
                distributedObjects.remove(co.getPublicName());
            }
        }
    }

    public void adoptOrphanObject(String publicName, Peer peer) {
        getDistributedObject(publicName).setOwnerId(peer.getId());
    }

    public void makeObjectsOrphanFor(Peer peer) {
        for (DistributedObject co : distributedObjects.values()) {
            if (!co.getOwnerId().equals(peer.getId())) {
                co.setOwnerId(null);
            }
        }
    }

    public List<String> getOrphanObjects() {
        List<String> list = new ArrayList<>();
        for (DistributedObject co : distributedObjects.values()) {
            if (co.getOwnerId() == null) {
                list.add(co.getPublicName());
            }
        }
        return list;
    }
}
