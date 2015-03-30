package com.goldennode.api.cluster;

public interface LeaderSelectionListener {

	public void leaderChanged(String newLeaderId);
}
