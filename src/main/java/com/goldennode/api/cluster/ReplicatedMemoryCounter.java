package com.goldennode.api.cluster;

import java.util.UUID;

import org.slf4j.LoggerFactory;

public class ReplicatedMemoryCounter extends ReplicatedMemoryObject {
    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCounter.class);
    private Counter counter = new Counter();

    class Counter {
        private int counter = 0;

        public synchronized int getCounter() {
            return counter;
        }

        public synchronized void setCounter(int counter) {
            this.counter = counter;
        }

        public synchronized int incrementAndGet() {
            return ++counter;
        }
    }

    public ReplicatedMemoryCounter() {
        super(UUID.randomUUID().toString());
    }

    public ReplicatedMemoryCounter(String publicName) {
        super(publicName);
    }

    public int getcounter() throws ClusterException {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return counter.getCounter();
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    public int inccounter() {
        return (int) safeOperate(new Operation(getPublicName(), "inccounter"));
    }

    public int _inccounter() {
        return _base_inccounter();
    }

    public int _base_inccounter() {
        return counter.incrementAndGet();
    }
}
