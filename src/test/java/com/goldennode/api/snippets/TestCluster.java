package com.goldennode.api.snippets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.goldennode.api.cluster.Cluster;
import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.cluster.ClusterFactory;
import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.ClusteredObject;
import com.goldennode.api.core.Server;

public class TestCluster {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestCluster.class);

	Cluster c;
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String arg[]) {
		TestCluster t = new TestCluster();
		t.test1();
	}

	public void test1() {
		try {
			c = ClusterFactory.getCluster();

			menu(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void menu(boolean write) {
		try {
			LOGGER.debug("");
			if (write) {
				System.out
						.println("1-create list/2-add to list/3-remove from list/4-clear list/5-print list detail/6-cluster detail/7-stop");
			}
			String i = br.readLine();
			if (i.equals("1")) {
				createList();
				menu(true);
			} else if (i.equals("2")) {
				add();
				menu(true);
			} else if (i.equals("3")) {
				remove();
				menu(true);
			} else if (i.equals("4")) {
				clear();
				menu(true);
			} else if (i.equals("5")) {
				clusteredListDetail();
				menu(true);
			} else if (i.equals("6")) {
				clusterDetail();
				menu(true);
			} else if (i.equals("7")) {
				c.stop();
				br.close();
				menu(true);
			}
			menu(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clear() throws ClusterException, IOException {
		LOGGER.debug("Enter list name");
		String name = br.readLine();
		if (name.equalsIgnoreCase("x")) {
			return;
		}
		ClusteredList cl = (ClusteredList) c.getClusteredObject(name);
		if (cl != null) {

			cl.clear();

		} else {
			LOGGER.debug("No clustered list available");
		}

	}

	public void createList() throws ClusterException, IOException {
		LOGGER.debug("Enter list name");
		String name = br.readLine();
		if (name.equalsIgnoreCase("x")) {
			return;
		}
		List<String> clusteredList = new ClusteredList<String>(name, c.getOwner().getId());
		// c.attachObject((ClusteredList<String>) clusteredList);

	}

	public void add() throws ClusterException, IOException {

		LOGGER.debug("Enter list name");
		String name = br.readLine();
		if (name.equalsIgnoreCase("x")) {
			return;
		}
		ClusteredList<String> cl = (ClusteredList<String>) c.getClusteredObject(name);
		if (cl != null) {
			LOGGER.debug("Enter value");
			String val = br.readLine();
			if (val.equalsIgnoreCase("x")) {
				return;
			}
			cl.add(val);

		} else {
			LOGGER.debug("No clustered list available");
		}

	}

	public void remove() throws ClusterException, IOException {
		LOGGER.debug("Enter list name");
		String name = br.readLine();
		if (name.equalsIgnoreCase("x")) {
			return;
		}
		ClusteredList cl = (ClusteredList) c.getClusteredObject(name);
		if (cl != null) {
			LOGGER.debug("Enter value");
			String val = br.readLine();
			if (val.equalsIgnoreCase("x")) {
				return;
			}
			cl.remove(new Integer(val).intValue());

		} else {
			LOGGER.debug("No clustered list available");
		}

	}

	public void clusteredListDetail() throws IOException {

		LOGGER.debug("Enter list name");
		String name = br.readLine();
		if (name.equalsIgnoreCase("x")) {
			return;
		}
		ClusteredList cl = (ClusteredList) c.getClusteredObject(name);
		if (cl != null) {
			LOGGER.debug("Clustered List Detail");
			for (int i = 0; i < cl.size(); i++) {
				LOGGER.debug("item " + i + " " + cl.get(i));
			}
		} else {
			LOGGER.debug("No clustered list available");
		}

	}

	public void clusterDetail() throws IOException {

		LOGGER.debug(c.toString());
		Collection<Server> cs = c.getPeers();
		Iterator<Server> iter = cs.iterator();
		LOGGER.debug("Servers");
		while (iter.hasNext()) {
			Server s = iter.next();
			if (s.equals(c.getOwner())) {
				LOGGER.debug(s + " THIS SERVER");
			} else {
				LOGGER.debug(s.toString());
			}
		}
		Collection<ClusteredObject> co = c.getClusteredObjects();
		Iterator<ClusteredObject> iter2 = co.iterator();
		LOGGER.debug("Objects");
		while (iter2.hasNext()) {
			LOGGER.debug(iter2.next().toString());
		}

	}
}
