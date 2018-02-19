package com.goldennode.api.goldennodegrid;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.core.Peer;
import com.goldennode.api.core.PeerStateListener;
import com.goldennode.api.helper.LockHelper;
import com.goldennode.api.helper.StringUtils;

public class GoldenNodeGridPeerStateListenerImpl implements PeerStateListener {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeGridPeerStateListenerImpl.class);
    /**
     * A reference to grid in case it is needed in this class
     */
    private GoldenNodeGrid grid;

    GoldenNodeGridPeerStateListenerImpl(GoldenNodeGrid grid) {
        this.grid = grid;
    }

    /**
     * This method is called when local peer is started. addPeerToGrid is
     * called within grid
     */
    @Override
    public void peerStarted(Peer peer) {
        LOGGER.debug("***peer started... id : " + StringUtils.shortId(peer.getId()));
        grid.peerAnnounceTimer.schedule();
        LockHelper.sleep(GoldenNodeGrid.PEER_ANNOUNCING_DELAY);
        grid.peerAnnounceTimer.stop();
        grid.leaderSelector.candidateDecisionLogic(false);
    }

    @Override
    public void peerStopping(Peer peer) {
        LOGGER.debug("***peer stopping... id : " + StringUtils.shortId(peer.getId()));
        try {
            grid.multicast(new Operation(null, "announcePeerLeaving", grid.getOwner()), new RequestOptions());
        } catch (GridException e) {
            LOGGER.error("Can't announce peer leaving: " + grid.getOwner());
            // This shouldn't never happen.
        }
    }

    @Override
    public void peerStopped(Peer peer) {
        LOGGER.debug("***peer peerStopped... id : " + StringUtils.shortId(peer.getId()));
        grid.heartBeatTimer.stop();
        grid.peerManager.clear();
        grid.distributedObjectManager.clearAll();
        grid.leaderSelector.reset();
    }

    @Override
    public void peerStarting(Peer peer) {
        LOGGER.debug("***peer starting... id : " + StringUtils.shortId(peer.getId()));
        grid.heartBeatTimer.start();
    }
}
