package com.goldennode.api.cluster;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.goldennode.api.helper.ReflectionUtils;

public class ReplicatedMemoryObject extends ClusteredObject {
    public ReplicatedMemoryObject(String publicName) {
        super(publicName);
    }

    public ReplicatedMemoryObject() {
        super();
    }

    protected Queue<Operation> uncommitted = new ArrayBlockingQueue<>(1);

    public boolean addToUncommited(Operation operation) {
        uncommitted.add(operation);
        return true;
    }

    public Object _commit() {
        Operation operation = uncommitted.poll();
        if (operation != null) {
            try {
                return ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
            } catch (Exception e) {
                throw new OperationException(e);
            }
        } else {
            throw new RuntimeException("No operation to commit");
        }
    }

    public void _rollback() {
        uncommitted.clear();
    }

    public Object safeOperate(Operation o) {
        boolean locked = false;
        try {
            getCluster().lock(this);
            locked = true;
            return getCluster().safeMulticast(o);
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }
}
