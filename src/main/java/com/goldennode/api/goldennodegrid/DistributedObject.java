package com.goldennode.api.goldennodegrid;

import java.io.Serializable;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.helper.ReflectionUtils;

public abstract class DistributedObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ownerId;
    private String publicName;
    private transient Grid grid;
    private String lockDistributedObject = "";
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DistributedObject.class);

    public DistributedObject() {
        publicName = getClass().getName() + "_" + "default";
    }

    public DistributedObject(String publicName) {
        this.publicName = publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public void setOwnerId(String ownerId) {
        synchronized (lockDistributedObject) {
            if (this.ownerId == null) {
                this.ownerId = ownerId;
                lockDistributedObject.notifyAll();
            } else {
                if (ownerId != null) {
                    LOGGER.error("ownerid is not null");
                    throw new RuntimeException("Illegal operation");
                } else {
                    this.ownerId = null;
                }
            }
        }
    }

    public boolean hasOwner() {
        return ownerId != null;
    }

    public String getOwnerId() {
        try {
            synchronized (lockDistributedObject) {
                while (ownerId == null) {
                    lockDistributedObject.wait();
                }
                return ownerId;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public String getPublicName() {
        return publicName;
    }

    public Grid getGrid() {
        return grid;
    }

    @Override
    public String toString() {
        return " > DistributedObject [ownerId=" + ownerId + ", publicName=" + publicName + "] ";
    }

    @Override
    public int hashCode() {
        return publicName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DistributedObject other = (DistributedObject) obj;
        if (publicName == null) {
            if (other.publicName != null) {
                return false;
            }
        } else if (!publicName.equals(other.publicName)) {
            return false;
        }
        return true;
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
            getGrid().writeLock(this);
            locked = true;
            return getGrid().safeMulticast(o);
        } catch (GridException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getGrid().unlockWriteLock(this);
                } catch (GridException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }
}
