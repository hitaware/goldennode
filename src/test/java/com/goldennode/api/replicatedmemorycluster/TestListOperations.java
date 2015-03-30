package com.goldennode.api.replicatedmemorycluster;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredList;

public class TestListOperations {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestListOperations.class);

	public static void main(String[] args) {

		try {
			Cluster c = ClusterFactory.getCluster();
			final ClusteredList<String> clusteredList = (ClusteredList<String>) c.getList(new String(), "list1");

			for (int i = 0; i < 1000; i++) {
				System.out.println("i=" + i);
				clusteredList.inccounter();

			}

			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					LOGGER.debug("Size=" + clusteredList.getcounter());

				}
			}, 0, 1000);

			// c.stop();
		} catch (Exception e) {
			LOGGER.error("Error occured", e);
		}

	}
}
