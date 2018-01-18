package com.goldennode.api.grid;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.PeerImpl;
import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.api.core.LockService;
import com.goldennode.api.core.LockServiceImpl;
import com.goldennode.api.core.Peer;
import com.goldennode.api.core.PeerException;

public class GridFactory {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GridFactory.class);

    private GridFactory() {
    }

    public static Grid getGrid(String peerId, int multicastPort, GridType type) throws GridException {
        try {
            LockService lockService = new LockServiceImpl();
            Peer peer = new PeerImpl(peerId, multicastPort);
            if (type == GridType.GOLDENNODEGRID) {
                return new GoldenNodeGrid(peer, lockService);
            }
            return null;
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    public static Grid getGrid(GridType type) throws GridException {
        try {
            LockService lockService = new LockServiceImpl();
            Peer peer = new PeerImpl();
            if (type == GridType.GOLDENNODEGRID) {
                return new GoldenNodeGrid(peer, lockService);
            }
            return null;
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    public static Grid getGrid(String peerId, GridType type) throws GridException {
        try {
            LockService lockService = new LockServiceImpl();
            Peer peer = new PeerImpl(peerId);
            if (type == GridType.GOLDENNODEGRID) {
                return new GoldenNodeGrid(peer, lockService);
            }
            return null;
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    public static Grid getGrid(int multicastPort, GridType type) throws GridException {
        try {
            LockService lockService = new LockServiceImpl();
            Peer peer = new PeerImpl(multicastPort);
            if (type == GridType.GOLDENNODEGRID) {
                return new GoldenNodeGrid(peer, lockService);
            }
            return null;
        } catch (PeerException e) {
            throw new GridException(e);
        }
    }

    public static Grid getGrid(String peerId, int multicastPort) throws GridException {
        return GridFactory.getGrid(peerId, multicastPort, GridType.GOLDENNODEGRID);
    }

    public static Grid getGrid() throws GridException {
        return GridFactory.getGrid(GridType.GOLDENNODEGRID);
    }

    public static Grid getGrid(String peerId) throws GridException {
        return GridFactory.getGrid(peerId, GridType.GOLDENNODEGRID);
    }

    public static Grid getGrid(int multicastPort) throws GridException {
        return GridFactory.getGrid(multicastPort, GridType.GOLDENNODEGRID);
    }
}
