package com.goldennode.api.grid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.goldennodegrid.DistributedObject;
import com.goldennode.api.goldennodegrid.HeartbeatTimer;
import com.goldennode.api.goldennodegrid.MultiResponse;
import com.goldennode.api.goldennodegrid.Operation;
import com.goldennode.api.core.Peer;
import com.goldennode.api.helper.LockHelper;

public abstract class Grid {
    public abstract Peer getOwner();

    public abstract Peer getCandidatePeer();

    public abstract <T extends DistributedObject> DistributedObject attach(T t, boolean eagerPropagation)
            throws GridException;

    public abstract <T extends DistributedObject> T attach(T t) throws GridException;

    public abstract <T extends DistributedObject> T newDistributedObjectInstance(String publicName, Class<T> claz,
            boolean eagerPropagation) throws GridException;

    public abstract <T extends DistributedObject> T newDistributedObjectInstance(String publicName, Class<T> claz)
            throws GridException;

    public abstract <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName, boolean eagerPropagation)
            throws GridException;

    public abstract <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName) throws GridException;

    public abstract <E> List<E> newReplicatedMemoryListInstance(String publicName, boolean eagerPropagation)
            throws GridException;

    public abstract <E> List<E> newReplicatedMemoryListInstance(String publicName) throws GridException;

    public abstract <E> Set<E> newReplicatedMemorySetInstance(String publicName, boolean eagerPropagation)
            throws GridException;

    public abstract <E> Set<E> newReplicatedMemorySetInstance(String publicName) throws GridException;

    public abstract <K, V> Map<K, V> newReplicatedMemoryMapInstance(boolean eagerPropagation) throws GridException;

    public abstract <K, V> Map<K, V> newReplicatedMemoryMapInstance() throws GridException;

    public abstract <T extends DistributedObject> T newDistributedObjectInstance(Class<T> claz,
            boolean eagerPropagation) throws GridException;

    public abstract <T extends DistributedObject> T newDistributedObjectInstance(Class<T> claz) throws GridException;

    public abstract <E> Set<E> newReplicatedMemorySetInstance(boolean eagerPropagation) throws GridException;

    public abstract <E> Set<E> newReplicatedMemorySetInstance() throws GridException;

    public abstract <E> List<E> newReplicatedMemoryListInstance(boolean eagerPropagation) throws GridException;

    public abstract <E> List<E> newReplicatedMemoryListInstance() throws GridException;

    public abstract void multicast(Operation operation, RequestOptions options) throws GridException;

    public abstract Object safeMulticast(Operation o) throws GridException;

    public abstract MultiResponse tcpMulticast(Collection<Peer> peers, Operation operation, RequestOptions options);

    public abstract Response unicastTCP(Peer remotePeer, Operation operation, RequestOptions options)
            throws GridException;

    public abstract Response unicastUDP(Peer remotePeer, Operation operation, RequestOptions options)
            throws GridException;

    public abstract void start() throws GridException;

    public abstract void stop() throws GridException;

    public abstract Collection<Peer> getPeers();

    public abstract void readLock(DistributedObject co) throws GridException;

    public abstract void writeLock(DistributedObject co) throws GridException;

    public abstract void unlockReadLock(DistributedObject co) throws GridException;

    public abstract void unlockWriteLock(DistributedObject co) throws GridException;

    @Override
    public String toString() {
        return " > Grid [owner=" + getOwner() + "] ";
    }

    public void reboot() {
        try {
            stop();
            LockHelper.sleep(HeartbeatTimer.TASK_PERIOD * 2);
            start();
        } catch (Exception e) {// NOPMD
            // Nothing to do
        }
    }
}
