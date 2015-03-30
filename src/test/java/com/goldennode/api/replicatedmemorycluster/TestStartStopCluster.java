package com.goldennode.api.replicatedmemorycluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;

public class TestStartStopCluster {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestStartStopCluster.class);

	public static void main(String[] args) {
		try {

			Cluster c = ClusterFactory.getCluster();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			boolean flag = true;
			while (true) {
				try {
					try {
						LOGGER.debug("Enter a key");
						br.readLine();

					} catch (IOException e) {

					}
					if (flag) {
						c.stop();
						flag = false;
					} else {
						c.start();
						flag = true;
					}
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (ClusterException e) {
			LOGGER.error("Error occured", e);
		}

	}
}
