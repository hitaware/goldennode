package com.goldennode.api.test;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.core.Logger;

public class ConcurrencyTestMaster {

	public static void main(String[] args) {

		try {
			Cluster c = ClusterFactory.getCluster();
			final List<String> clusteredList = new ErroneousClusteredList<String>();

			((ErroneousClusteredList<String>) clusteredList).setOwnerId(c
					.getOwner().getId());
			((ErroneousClusteredList<String>) clusteredList)
					.setPublicName("list1");
			c.attachObject((ClusteredObject) clusteredList);

			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					for (int i = 0; i < 1; i++) {
						clusteredList.add(new Integer(i).toString());
					}

				}
			}, 3000);

			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					System.out.println(clusteredList.size());

				}
			}, 0, 1000);
			clusteredList.remove(0);
			// c.detachObject((ClusteredObject) clusteredList);
			// clusteredList.remove(0);
			// c.stop();
		} catch (ClusterException e) {
			Logger.error(e);
		}
	}
}
