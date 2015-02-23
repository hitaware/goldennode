package com.goldennode.api.snippets;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.ClusteredObject;

public class ListOperations2 {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ListOperations2.class);

	public static void main(String[] args) {

		try {
			final Cluster c = ClusterFactory.getCluster();

			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					ClusteredObject co = c.getClusteredObject("list1");
					if (co != null) {
						LOGGER.debug(new Integer(((ClusteredList) co).size()).toString());
					} else {
						LOGGER.debug("not init");
					}

				}
			}, 1000, 1000);

			// sleep(1000);
			// c.stop();
		} catch (ClusterException e) {
			LOGGER.error("Error occured", e);
		}
	}
}
