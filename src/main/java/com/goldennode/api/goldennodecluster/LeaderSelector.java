package com.goldennode.api.goldennodecluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;

public class LeaderSelector {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LeaderSelector.class);
	private Cluster cluster;
	private volatile String leaderId;
	private static final int REQUESTS_TIMEOUT = 5000;
	private LeaderSelectionListener listener;

	public LeaderSelector(Cluster cluster, LeaderSelectionListener listener) {
		this.cluster = cluster;
		this.listener = listener;
	}

	public void candidateDecisionLogic() {

		synchronized (this) {
			if (leaderId == null) {
				if (cluster.getCandidateServer().equals(cluster.getOwner())) {
					LOGGER.debug("I am candidate for leadership");
					getLeadership();
				} else {
					LOGGER.debug("I am not candidate for leadership. Waiting for master to contact me.");
				}
			}
		}

		waitForMaster();

	}

	private void waitForMaster() {
		if (((GoldenNodeCluster) cluster).clusteredServerManager
				.getMasterServer(GoldenNodeCluster.WAITFORMASTER_DELAY) == null) {
			LOGGER.debug("***REBOOTING*** Couldn't get master");
			cluster.reboot();
		}
	}

	private void getLeadership() {
		try {
			LOGGER.trace("begin getLeadership.");
			LOGGER.debug("trying to get leadership");
			boolean selfResponse = false;

			selfResponse = acquireLeadership(cluster.getOwner().getId());

			if (selfResponse) {
				MultiResponse responses = cluster.tcpMulticast(cluster.getPeers(),
						new Operation(null, "acquireLeadership", cluster.getOwner().getId()),
						new RequestOptions(REQUESTS_TIMEOUT));
				if (responses.size() > 0 && !responses.isSuccessfulCall(true)) {
					LOGGER.debug("***REBOOTING***Partially/Full Error Response");
					cluster.reboot();
				}
			} else {
				cluster.reboot();
			}

			LOGGER.debug("Got leadership.");

		} finally {
			LOGGER.trace("end getLeadership.");
		}
	}

	public boolean acquireLeadership(String id) {
		try {

			LOGGER.trace("begin _acquireLeadership");
			LOGGER.debug("trying to acquire lead with id > " + id + " Thread Name:" + Thread.currentThread().getName());

			if (leaderId != null) {
				LOGGER.debug("lead has already been acquired by > " + leaderId);
				return false;
			}
			synchronized (this) {

				if (leaderId != null) {
					LOGGER.debug("lead has already been acquired by > " + leaderId);
					return false;
				}
				LOGGER.debug("acquired lead with id > " + id);
				setLeaderId(id);
				return true;
			}
		} finally {
			LOGGER.trace("end _acquireLeadership");
		}
	}

	public synchronized void setLeaderId(String newLeaderId) {
		if (leaderId == null) {
			LOGGER.debug("setting leader to > " + newLeaderId);
			leaderId = newLeaderId;
			listener.leaderChanged(newLeaderId);
		}
	}

	public void reset() {

		leaderId = null;
	}

	public synchronized void rejoinElection() {
		reset();
		candidateDecisionLogic();
	}

	public synchronized String getLeaderId() {
		return leaderId;
	}
}
