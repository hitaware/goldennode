package com.goldennode.api.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.core.Logger;

public class StartStopCluster {

	public static void main(String[] args) {
		try {

			Cluster c = ClusterFactory.getCluster();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			boolean flag = true;
			while (true) {
				try {
					try {
						System.out.println("Enter a key");
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
			Logger.error(e);
		}

	}
}
