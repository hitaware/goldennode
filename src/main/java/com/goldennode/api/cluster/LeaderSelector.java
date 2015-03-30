package com.goldennode.api.cluster;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.RequestOptions;
import com.goldennode.api.core.Server;

public class LeaderSelector {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LeaderSelector.class);

	private Cluster cluster;
	private volatile String provisionLeaderId;
	private volatile String leaderId;
	private static final int WAIT_TIMEOUT = 1000;
	private static final int REQUESTS_TIMEOUT = 10000;
	private Object waitObject = new Object();

	private LeaderSelectionListener listener;

	public LeaderSelector(Cluster cluster, LeaderSelectionListener listener) {
		this.cluster = cluster;
		this.listener = listener;
	}

	public synchronized void IamCandidate() throws ClusterException {

		while (true) {
			if (leaderId == null && amICandidate()) {
				LOGGER.debug("I am candidate for leadership");
				if (getLeadership(true)) {
					if (getLeadership(false)) {
						LOGGER.debug("Got leadership.");
						break;
					}
					LOGGER.debug("Couldnt get lead. Will retry");
				} else {
					LOGGER.debug("Couldnt get provisional lead. Will retry");
				}
				synchronized (waitObject) {

					try {
						wait(WAIT_TIMEOUT);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new ClusterException();
					}

				}
			} else {
				LOGGER.debug("There is already a leader.");
				break;
			}
		}
	}

	private boolean amICandidate() {
		LOGGER.debug("Am I candidate for leadership?");
		String myId = cluster.getOwner().getId();
		for (Server server : cluster.getPeers()) {
			if (server.getId().compareTo(myId) > 0) {
				LOGGER.debug("No, I am not!");
				return false;
			}
		}
		LOGGER.debug("Yes, I am!");
		return true;
	}

	private boolean getLeadership(boolean provisonal) {
		try {
			LOGGER.trace("begin getLeadership. isProvisional? " + provisonal);
			LOGGER.debug("trying to get leadership");
			String methodName = null;
			boolean selfResponse = false;
			if (provisonal) {
				selfResponse = _acquireProvisionalLeadership(cluster.getOwner().getId());
				methodName = "acquireProvisionalLeadership";
			} else {
				selfResponse = _acquireLeadership(cluster.getOwner().getId());
				methodName = "acquireLeadership";
			}
			if (!selfResponse) {
				LOGGER.debug("cant acquire lead on own. can't  get leadership");
				return false;
			}

			MultiResponse responses = cluster.tcpMulticast(cluster.getPeers(), new Operation(null, methodName, cluster
					.getOwner().getId()), new RequestOptions(REQUESTS_TIMEOUT));
			if (responses.size() > 0 && !responses.isSuccessfulCall(true)) {
				LOGGER.debug("rollback acquire");
				if (provisonal) {
					releaseLeadership(responses.getSuccessfulServers(true), true);
				}
				return false;
			}
			LOGGER.debug("got leadership successfully");
			return true;
		} finally {
			LOGGER.trace("end getLeadership" + provisonal);
		}

	}

	public boolean _acquireProvisionalLeadership(String id) {
		try {

			LOGGER.trace("begin _acquireProvisionalLeadership");
			LOGGER.debug("trying to acquire provisional lead with id > " + id);
			if (provisionLeaderId != null) {
				LOGGER.debug("provisonal lead has already been acquired by > " + provisionLeaderId);
				return false;
			}
			if (leaderId != null) {
				LOGGER.debug("lead has already been acquired by > " + leaderId);
				return false;
			}
			synchronized (this) {
				if (provisionLeaderId != null) {
					LOGGER.debug("provisonal lead has already been acquired by > " + provisionLeaderId);
					return false;
				}
				if (leaderId != null) {
					LOGGER.debug("lead has already been acquired by > " + leaderId);
					return false;
				}
				LOGGER.debug("acquired provisional lead with id > " + id);
				provisionLeaderId = id;
				return true;
			}
		} finally {
			LOGGER.trace("end _acquireProvisionalLeadership");
		}
	}

	public boolean _acquireLeadership(String id) {
		try {

			LOGGER.trace("begin _acquireLeadership");
			LOGGER.debug("trying to acquire lead with id > " + id);
			if (provisionLeaderId == null || !provisionLeaderId.equals(id)) {
				LOGGER.debug("lead has already been acquired by > " + provisionLeaderId);
				return false;
			}
			if (leaderId != null) {
				LOGGER.debug("lead has already been acquired by > " + leaderId);
				return false;
			}

			synchronized (this) {

				if (provisionLeaderId == null || !provisionLeaderId.equals(id)) {
					LOGGER.debug("lead has already been acquired by > " + provisionLeaderId);
					return false;
				}
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

	public synchronized boolean _releaseProvisionalLeadership(String id) {
		try {
			LOGGER.trace("begin _releaseProvisionalLeadership");
			LOGGER.debug("trying to release provisonal lead with id > " + id);
			if (provisionLeaderId != null && provisionLeaderId.equals(id)) {
				provisionLeaderId = null;
				LOGGER.debug("released lead with id > " + id);
				return true;
			}
			LOGGER.debug("cant release provisional lead. provisonal lead has already been acquired by > "
					+ provisionLeaderId);
			return false;
		} finally {
			LOGGER.trace("end _releaseProvisionalLeadership");
		}

	}

	public synchronized boolean _releaseLeadership(String id) {
		try {
			LOGGER.trace("begin _releaseLeadership");
			LOGGER.debug("trying to release lead with id > " + id);
			if (leaderId != null && leaderId.equals(id)) {
				leaderId = null;
				provisionLeaderId = null;
				LOGGER.debug("released lead with id > " + id);
				return true;
			}
			LOGGER.debug("cant release lead. lead has already been acquired by > " + leaderId);
			return false;
		} finally {
			LOGGER.trace("end _releaseLeadership");
		}

	}

	private boolean releaseLeadership(Collection<Server> servers, boolean provisonal) {
		try {
			LOGGER.trace("begin releaseLeadership" + provisonal);
			LOGGER.debug("trying to release leadership");

			String methodName = null;
			boolean selfResponse = false;
			if (provisonal) {
				selfResponse = _releaseProvisionalLeadership(cluster.getOwner().getId());
				methodName = "releaseProvisionalLeadership";
			} else {
				selfResponse = _releaseLeadership(cluster.getOwner().getId());
				methodName = "releaseLeadership";
			}

			MultiResponse responses = cluster.tcpMulticast(servers, new Operation(null, methodName, cluster.getOwner()
					.getId()), new RequestOptions(REQUESTS_TIMEOUT));

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
			LOGGER.trace("end releaseLeadership" + provisonal);
		}

	}

	public synchronized void setLeaderId(String newLeaderId) {
		if (leaderId == null) {
			LOGGER.debug("setting leader to > " + newLeaderId);
			leaderId = newLeaderId;
			listener.leaderChanged(newLeaderId);
		} else {
			// This shouldn't happen!!! throw exception
			LOGGER.debug("cant set leader. aldready set to > " + leaderId);
		}

	}

	public synchronized void rejoinElection() throws ClusterException {
		provisionLeaderId = null;
		leaderId = null;
		joinElection();

	}

	public synchronized void joinElection() throws ClusterException {
		if (leaderId == null && amICandidate()) {
			IamCandidate();
		}
	}
}
