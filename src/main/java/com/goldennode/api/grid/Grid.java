package com.goldennode.api.grid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Response;
import com.goldennode.api.goldennodegrid.HeartbeatTimer;
import com.goldennode.api.core.Peer;
import com.goldennode.api.helper.LockHelper;

public abstract class Grid {
    public abstract Peer getOwner();

    public abstract Peer getCandidatePeer();

    //public abstract <T extends DistributedObject> T attach(T t) throws GridException;

    public abstract <T extends DistributedObject> T newDistributedObjectInstance(String publicName, Class<T> claz)
            throws GridException;

    public abstract <K, V> Map<K, V> newReplicatedMemoryMapInstance(String publicName) throws GridException;

    public abstract <E> List<E> newReplicatedMemoryListInstance(String publicName) throws GridException;

    public abstract <E> Set<E> newReplicatedMemorySetInstance(String publicName) throws GridException;

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

    protected abstract void readLock(DistributedObject co) throws GridException;
    
    protected abstract void writeLock(DistributedObject co) throws GridException;

    protected abstract void unlockReadLock(DistributedObject co) throws GridException;
    
    protected abstract void unlockWriteLock(DistributedObject co) throws GridException;


    @Override
    public String toString() {
        return " > Grid [owner=" + getOwner() + "] ";
    }

    public void reboot() {
        try {
            stop();
            LockHelper.sleep(HeartbeatTimer.TASK_PERIOD * 2);
            start();
        } catch (Exception e) {//NOPMD
            //Nothing to do
        }
    }
}