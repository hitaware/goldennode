package com.goldennode.api.snippets;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredList;

public class ListOperations {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ListOperations.class);

	public static void main(String[] args) {

		try {
			Cluster c = ClusterFactory.getCluster();
			final List<String> clusteredList = new ClusteredList<String>();

			((ClusteredList<String>) clusteredList).setOwnerId(c.getOwner().getId());
			((ClusteredList<String>) clusteredList).setPublicName("list1");
			c.attachObject((ClusteredList<String>) clusteredList);
			for (int i = 0; i < 10; i++) {
				clusteredList.add(new Integer(i).toString());
			}

			/*
			 * new Timer().schedule(new TimerTask() {
			 * 
			 * @Override public void run() { LOGGER.debug(clusteredList.size());
			 * 
			 * } }, 0, 1000);
			 */
			clusteredList.remove(0);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			LOGGER.debug(new Integer(clusteredList.size()).toString());

			// c.stop();
		} catch (Exception e) {
			LOGGER.error("Error occured", e);
		}

	}
}
