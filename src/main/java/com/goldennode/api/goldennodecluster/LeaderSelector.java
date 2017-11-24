package com.goldennode.api.goldennodecluster;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.MultiResponse;
import com.goldennode.api.cluster.Operation;
import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Server;

public class LeaderSelector {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LeaderSelector.class);
	private Cluster cluster;
	private volatile String leaderId;
	private static final int RETRY_TIMEOUT = 1000;
	private static final int REQUESTS_TIMEOUT = 5000;
	private Object waitObject = new Object();
	private LeaderSelectionListener listener;

	public LeaderSelector(Cluster cluster, LeaderSelectionListener listener) {
		this.cluster = cluster;
		this.listener = listener;
	}

	public synchronized void candidateDecisionLogic() {
		while (true) {
			if (leaderId == null) {
				if (cluster.getCandidateServer().equals(cluster.getOwner())) {
					LOGGER.debug("I am candidate for leadership");

					if (getLeadership()) {
						LOGGER.debug("Got leadership.");
						break;
					}
					LOGGER.warn("Couldnt get lead. Will retry");

					synchronized (waitObject) {
						try {
							wait(RETRY_TIMEOUT);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				} else {
					LOGGER.debug("I am not candidate for leadership");
					break;
				}
			} else {
				LOGGER.debug("There is already a leader. " + leaderId);
				break;
			}
		}
	}

	private boolean getLeadership() {
		try {
			LOGGER.trace("begin getLeadership.");
			LOGGER.debug("trying to get leadership");
			String methodName = null;
			boolean selfResponse = false;

			selfResponse = acquireLeadership(cluster.getOwner().getId());
			methodName = "acquireLeadership";

			if (!selfResponse) {
				LOGGER.debug("cant acquire lead on own. can't  get leadership");
				return false;
			}
			MultiResponse responses = cluster.tcpMulticast(cluster.getPeers(),
					new Operation(null, methodName, cluster.getOwner().getId()), new RequestOptions(REQUESTS_TIMEOUT));
			if (responses.size() > 0 && !responses.isSuccessfulCall(true)) {
				LOGGER.debug("rollback acquire");
				releaseLeadership(responses.getServersWithNoErrorAndExpectedResult(true));
				return false;
			}
			LOGGER.debug("got leadership successfully.");
			return true;
		} finally {
			LOGGER.trace("end getLeadership.");
		}
	}

	private boolean releaseLeadership(Collection<Server> servers) {
		try {
			LOGGER.trace("begin releaseLeadership");
			LOGGER.debug("trying to release leadership");
			boolean selfResponse = false;

			selfResponse = releaseLeadership(cluster.getOwner().getId());

			MultiResponse responses = cluster.tcpMulticast(servers,
					new Operation(null, "releaseLeadership", cluster.getOwner().getId()),
					new RequestOptions(REQUESTS_TIMEOUT));
			boolean result = responses.size() > 0 && responses.isSuccessfulCall(true);
			if (!result) {
				LOGGER.debug("cant release lead from (a) server(s)");
			}
			if (!selfResponse) {
				LOGGER.debug("cant release lead from own");
			}
			if (result && selfResponse) {
				LOGGER.debug("released leadership successfully");
			}
			return result && selfResponse;
		} finally {
			LOGGER.trace("end releaseLeadership");
		}
	}

	public boolean acquireLeadership(String id) {
		try {
			
			LOGGER.trace("begin _acquireLeadership");
			LOGGER.debug("trying to acquire lead with id > " + id+ " Thread Name:"+ Thread.currentThread().getName());
			

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
				leaderId = id;
				listener.leaderChanged(leaderId);
				return true;
			}
		} finally {
			LOGGER.trace("end _acquireLeadership");
		}
	}

	public synchronized boolean releaseLeadership(String id) {
		try {
			LOGGER.trace("begin _releaseLeadership");
			LOGGER.debug("trying to release lead with id > " + id);
			if (leaderId != null && leaderId.equals(id)) {
				leaderId = null;
				LOGGER.debug("released lead with id > " + id);
				return true;
			}
			LOGGER.debug("cant release lead. lead has already been acquired by > " + leaderId);
			return false;
		} finally {
			LOGGER.trace("end _releaseLeadership");
		}
	}

	// Called when incomingServer
	public synchronized void setLeaderId(String newLeaderId) {
		if (leaderId == null) {// on newly initializing server and other server
							   // are on the cluster already.
			LOGGER.debug("setting leader to > " + newLeaderId);
			leaderId = newLeaderId;
			listener.leaderChanged(newLeaderId);
		} else {
			// newly initializing serve is master??? and other server are on the
			// cluster already.
			// This shouldn't happen!!! reboot
			LOGGER.error("cant set leader. aldready set to > " + leaderId);
			cluster.reboot();
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
