package com.goldennode.api.goldennodegrid;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.helper.StringUtils;

public class LeaderSelector {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LeaderSelector.class);
    private Grid grid;
    private volatile String candidateLeaderId;
    private volatile String leaderId;
    private static final int REQUESTS_TIMEOUT = 5000;
    private LeaderSelectionListener listener;
    private Object lock = new Object();

    public LeaderSelector(Grid grid, LeaderSelectionListener listener) {
        this.grid = grid;
        this.listener = listener;
    }

    public void candidateDecisionLogic(boolean rejoined) {
        if (leaderId == null) {
            if (grid.getCandidatePeer().equals(grid.getOwner())) {
                LOGGER.debug("I am candidate for leadership");
                getLeadership(rejoined);
            } else {
                LOGGER.debug("I am not candidate for leadership. Waiting for master to contact me.");
            }
        } else {
            waitForMaster();
        }
    }

    private void waitForMaster() {
        try {
            for (int i = 0; i < GoldenNodeGrid.WAITFORMASTER_DELAY / 1000; i++) {
                if (leaderId != null) {
                    return;
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            //
        }
        if (getLeaderId() == null) {
            LOGGER.debug("***REBOOTING*** Couldn't get master");
            grid.reboot();
        }
    }

    private void getLeadership(boolean rejoined) {
        try {
            LOGGER.trace("begin getLeadership.");
            LOGGER.debug("trying to get leadership");
            if (acquireLeadershipPrepare(grid.getOwner().getId(), true)) {
                MultiResponse responses = grid.tcpMulticast(grid.getPeers(),
                        new Operation(null, "acquireLeadershipPrepare", StringUtils.shortId(grid.getOwner().getId())),
                        new RequestOptions(LeaderSelector.REQUESTS_TIMEOUT));
                if (responses.isSuccessfulCall(true)) {
                    setCandidateLeaderId(grid.getOwner().getId());
                    if (acquireLeadershipCommit(grid.getOwner().getId(), true)) {
                        responses = grid.tcpMulticast(grid.getPeers(),
                                new Operation(null, "acquireLeadershipCommit",  StringUtils.shortId(grid.getOwner().getId())),
                                new RequestOptions(LeaderSelector.REQUESTS_TIMEOUT));
                        {
                            if (responses.isSuccessfulCall(true)) {
                                setLeaderId(grid.getOwner().getId());
                                listener.iAmSelectedAsLead(rejoined);
                                LOGGER.debug("Got leadership.");
                                return;
                            }
                        }
                    }
                }
            }
            LOGGER.debug("***REBOOTING***");
            grid.reboot();
        } finally {
            LOGGER.trace("end getLeadership.");
        }
    }

    public boolean acquireLeadershipCommit(String id, boolean local) {
        try {
            LOGGER.trace("begin _acquireLeadershipCommit");
            LOGGER.debug("trying to acquire lead with id > " +  StringUtils.shortId(id) + " Thread Name:" + Thread.currentThread().getName());
            if (leaderId != null) {
                LOGGER.error("lead has already been acquired by > " +  StringUtils.shortId(leaderId));
                return false;
            }
            if (candidateLeaderId != null && !candidateLeaderId.equals(id)) {
                LOGGER.error("candidate lead mismatch > " +  StringUtils.shortId(candidateLeaderId));
                return false;
            }
            synchronized (lock) {
                if (leaderId != null) {
                    LOGGER.error("lead has already been acquired by > " +  StringUtils.shortId(leaderId));
                    return false;
                }
                if (candidateLeaderId != null && !candidateLeaderId.equals(id)) {
                    LOGGER.error("candidate lead mismatch > " +  StringUtils.shortId(candidateLeaderId));
                    return false;
                }
                LOGGER.debug("acquired lead with id > " +  StringUtils.shortId(id));
                if (!local) {
                    setLeaderId(id);
                }
                return true;
            }
        } finally {
            LOGGER.trace("end _acquireLeadershipCommit");
        }
    }

    public boolean acquireLeadershipPrepare(String id, boolean local) {
        try {
            LOGGER.trace("begin acquireLeadershipPrepare");
            LOGGER.debug("trying to acquire candidate lead with id > " + id + " Thread Name:"
                    + Thread.currentThread().getName());
            if (leaderId != null) {
                LOGGER.error("lead has already been acquired by > " +  StringUtils.shortId(leaderId));
                return false;
            }
            if (candidateLeaderId != null) {
                LOGGER.error("candidate lead has already been acquired by > " +  StringUtils.shortId(candidateLeaderId));
                return false;
            }
            synchronized (lock) {
                if (leaderId != null) {
                    LOGGER.error("lead has already been acquired by > " +  StringUtils.shortId(leaderId));
                    return false;
                }
                if (candidateLeaderId != null) {
                    LOGGER.error("candidate lead has already been acquired by > " +  StringUtils.shortId(candidateLeaderId));
                    return false;
                }
                LOGGER.debug("acquired candidate lead with id > " +  StringUtils.shortId(id));
                if (!local) {
                    setCandidateLeaderId(id);
                }
                return true;
            }
        } finally {
            LOGGER.trace("end acquireLeadershipPrepare");
        }
    }

    private void setCandidateLeaderId(String candidateLeaderId) {
        synchronized (lock) {
            if (candidateLeaderId == null) {
                LOGGER.debug("setting candidate leader to > " +  StringUtils.shortId(candidateLeaderId));
                this.candidateLeaderId = candidateLeaderId;
            }
        }
    }

    public void reset() {
        candidateLeaderId = null;
        leaderId = null;
    }

    public void rejoinElection() {
        reset();
        candidateDecisionLogic(true);
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        synchronized (lock) {
            if (this.leaderId == null) {
                LOGGER.debug("setting leader to > " + StringUtils.shortId(leaderId));
                this.leaderId = leaderId;
            }
        }
    }
}
