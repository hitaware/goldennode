package com.goldennode.api.test;

import java.util.List;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.core.Logger;

public class ListOperations {

	public static void main(String[] args) {

		try {
			Cluster c = ClusterFactory.getCluster();
			final List<String> clusteredList = new ClusteredList<String>();

			((ClusteredList<String>) clusteredList).setOwnerId(c.getOwner()
					.getId());
			((ClusteredList<String>) clusteredList).setPublicName("list1");
			c.attachObject((ClusteredObject) clusteredList);
			for (int i = 0; i < 10; i++) {
				clusteredList.add(new Integer(i).toString());
			}
			// c.attachObject((ClusteredObject) clusteredList);

			/*
			 * new Timer().schedule(new TimerTask() {
			 * 
			 * @Override public void run() {
			 * System.out.println(clusteredList.size());
			 * 
			 * } }, 0, 1000);
			 */
			clusteredList.remove(0);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			System.out.println(clusteredList.size());

			// c.detachObject((ClusteredObject) clusteredList);
			// clusteredList.remove(0);
			// c.stop();
		} catch (ClusterException e) {
			Logger.error(e);
		}

	}

}
