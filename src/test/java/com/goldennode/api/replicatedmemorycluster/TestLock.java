package com.goldennode.api.replicatedmemorycluster;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredLock;

public class TestLock {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestLock.class);

	public static void main(String[] args) throws Exception {

		Cluster c = ClusterFactory.getCluster();
		ClusteredLock lock1 = c.getLock("lock1");
		for (int i = 0; i < 20; i++) {

			// Thread.sleep(1000);
			lock1.lock();

			LOGGER.debug("**************************begin job****************************");
			LOGGER.debug("counter=" + i);
			// Thread.sleep(100);
			LOGGER.debug("**************************end job****************************");
			lock1.unlock();

		}

	}

}
