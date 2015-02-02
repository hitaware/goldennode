package com.goldennode.api.test;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.core.Logger;

public class ConcurrencyTestSlaveNode {

	public static void main(String[] args) {

		try {
			final Cluster c = ClusterFactory.getCluster();

			/*
			 * new Timer().schedule(new TimerTask() {
			 * 
			 * @Override public void run() { ClusteredObject co =
			 * c.getClusteredObject("list1"); if (co != null) {
			 * System.out.println(((ErroneousClusteredList) co).size()); } else
			 * { System.out.println("not init"); }
			 * 
			 * } }, 1000, 1000);
			 */
			// clusteredList.remove(0);
			// c.detachObject(clusteredList);
			// clusteredList.remove(0);
			// sleep(1000);
			// c.stop();
		} catch (ClusterException e) {
			Logger.error(e);
		}
	}

}
