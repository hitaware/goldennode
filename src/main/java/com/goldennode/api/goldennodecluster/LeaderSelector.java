package com.goldennode.api.goldennodecluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;

public class LeaderSelector {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LeaderSelector.class);
    private Cluster cluster;
    private volatile String candidateLeaderId;
    private volatile String leaderId;
    private static final int REQUESTS_TIMEOUT = 5000;
    private LeaderSelectionListener listener;
    private Object lock = new Object();

    public LeaderSelector(Cluster cluster, LeaderSelectionListener listener) {
        this.cluster = cluster;
        this.listener = listener;
    }

    public void candidateDecisionLogic() {
        if (leaderId == null) {
            if (cluster.getCandidateServer().equals(cluster.getOwner())) {
                LOGGER.debug("I am candidate for leadership");
                getLeadership();
            } else {
                LOGGER.debug("I am not candidate for leadership. Waiting for master to contact me.");
            }
        } else {
            waitForMaster();
        }
    }

    private void waitForMaster() {
        try {
            for (int i = 0; i < GoldenNodeCluster.WAITFORMASTER_DELAY / 1000; i++) {
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
            cluster.reboot();
        }
    }

    private void getLeadership() {
        try {
            LOGGER.trace("begin getLeadership.");
            LOGGER.debug("trying to get leadership");
            if (acquireLeadershipPrepare(cluster.getOwner().getId(), true)) {
                MultiResponse responses = cluster.tcpMulticast(cluster.getPeers(),
                        new Operation(null, "acquireLeadershipPrepare", cluster.getOwner().getId()),
                        new RequestOptions(REQUESTS_TIMEOUT));
                if (responses.isSuccessfulCall(true)) {
                    setCandidateLeaderId(cluster.getOwner().getId());
                    if (acquireLeadershipCommit(cluster.getOwner().getId(), true)) {
                        responses = cluster.tcpMulticast(cluster.getPeers(),
                                new Operation(null, "acquireLeadershipCommit", cluster.getOwner().getId()),
                                new RequestOptions(REQUESTS_TIMEOUT));
                        {
                            if (responses.isSuccessfulCall(true)) {
                                setLeaderId(cluster.getOwner().getId());
                                listener.iAmSelectedAsLead();
                                LOGGER.debug("Got leadership.");
                                return;
                            }
                        }
                    }
                }
            }
            LOGGER.debug("***REBOOTING***");
            cluster.reboot();
        } finally {
            LOGGER.trace("end getLeadership.");
        }
    }

    public boolean acquireLeadershipCommit(String id, boolean local) {
        try {
            LOGGER.trace("begin _acquireLeadershipCommit");
            LOGGER.debug("trying to acquire lead with id > " + id + " Thread Name:" + Thread.currentThread().getName());
            if (leaderId != null) {
                LOGGER.error("lead has already been acquired by > " + leaderId);
                return false;
            }
            if (candidateLeaderId != null && !candidateLeaderId.equals(id)) {
                LOGGER.error("candidate lead mismatch > " + candidateLeaderId);
                return false;
            }
            synchronized (lock) {
                if (leaderId != null) {
                    LOGGER.error("lead has already been acquired by > " + leaderId);
                    return false;
                }
                if (candidateLeaderId != null && !candidateLeaderId.equals(id)) {
                    LOGGER.error("candidate lead mismatch > " + candidateLeaderId);
                    return false;
                }
                LOGGER.debug("acquired lead with id > " + id);
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
                LOGGER.error("lead has already been acquired by > " + candidateLeaderId);
                return false;
            }
            if (candidateLeaderId != null) {
                LOGGER.error("candidate lead has already been acquired by > " + candidateLeaderId);
                return false;
            }
            synchronized (lock) {
                if (leaderId != null) {
                    LOGGER.error("lead has already been acquired by > " + candidateLeaderId);
                    return false;
                }
                if (candidateLeaderId != null) {
                    LOGGER.error("candidate lead has already been acquired by > " + candidateLeaderId);
                    return false;
                }
                LOGGER.debug("acquired candidate lead with id > " + id);
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
                LOGGER.debug("setting candidate leader to > " + candidateLeaderId);
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
        candidateDecisionLogic();
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        synchronized (lock) {
            if (this.leaderId == null) {
                LOGGER.debug("setting leader to > " + leaderId);
                this.leaderId = leaderId;
            }
        }
    }
}
