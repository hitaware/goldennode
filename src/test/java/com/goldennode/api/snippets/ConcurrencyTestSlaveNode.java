package com.goldennode.api.snippets;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;

public class ConcurrencyTestSlaveNode {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConcurrencyTestSlaveNode.class);

	public static void main(String[] args) {

		try {
			final Cluster c = ClusterFactory.getCluster();

			/*
			 * new Timer().schedule(new TimerTask() {
			 * 
			 * @Override public void run() { ClusteredObject co =
			 * c.getClusteredObject("list1"); if (co != null) {
			 * LOGGER.debug(((ErroneousClusteredList) co).size()); } else {
			 * LOGGER.debug("not init"); }
			 * 
			 * } }, 1000, 1000);
			 */
			// clusteredList.remove(0);
			// c.detachObject(clusteredList);
			// clusteredList.remove(0);
			// sleep(1000);
			// c.stop();
		} catch (ClusterException e) {
			LOGGER.error("Error occured", e);
		}
	}

}
