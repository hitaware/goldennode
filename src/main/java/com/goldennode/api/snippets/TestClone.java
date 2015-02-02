package com.goldennode.api.snippets;

import com.goldennode.api.cluster.ClusterException;

public class TestClone {

	public static void main(String[] args) {

		Exception c = new ClusterException();
		if (c instanceof Exception) {
			System.out.println("ok");
		}

		/*
		 * final List<String> clusteredList = Collections .synchronizedList(new
		 * ArrayList<String>()); clusteredList.add("1"); clusteredList.add("2");
		 * clusteredList.add("3"); clusteredList.add("4");
		 * clusteredList.add("5"); ((ArrayList) clusteredList).clone();
		 */

	}
}
