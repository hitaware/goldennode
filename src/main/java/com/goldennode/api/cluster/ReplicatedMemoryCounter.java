package com.goldennode.api.cluster;

import java.util.UUID;

import org.slf4j.LoggerFactory;

public class ReplicatedMemoryCounter extends ClusteredObject {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCounter.class);
    private int counter = 0;

    public ReplicatedMemoryCounter() {
        super(UUID.randomUUID().toString());
    }

    public ReplicatedMemoryCounter(String publicName) {
        super(publicName);
    }

    public synchronized int getcounter() {
        return counter;
    }

    public int inccounter() {
        return (int) safeOperate(new Operation(getPublicName(), "inccounter"));
    }

    public int _inccounter() {
        return _base_inccounter();
    }

    public synchronized int _base_inccounter() {
        counter = counter + 1;
        return counter;
    }
}
