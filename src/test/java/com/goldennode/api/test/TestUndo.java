package com.goldennode.api.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.goldennode.api.cluster.ClusteredList;

public class TestUndo {
	List<String> list;

	@Before
	public void init() {
		list = new ClusteredList<String>();

	}

	@Test
	public void undoOperations() {
		Assert.assertEquals(((ClusteredList) list).getVersion(), 1);

		System.out.println("Adding 5 items");
		list.add("bir");
		list.add("iki");
		list.add("uc");
		list.add("dort");
		list.add("bes");
		Assert.assertEquals(list.size(), 5);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 6);

		System.out.println("Removing 2nd index");
		list.remove(2);
		Assert.assertEquals(list.size(), 4);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 6);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 7);

		System.out.println("undo");
		((ClusteredList) list).undoLatest(7);
		Assert.assertEquals(list.size(), 5);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 6);

		System.out.println("Adding alti");
		list.add("alti");
		Assert.assertEquals(list.size(), 6);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 6);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 7);

		System.out.println("undo");
		((ClusteredList) list).undoLatest(7);
		Assert.assertEquals(list.size(), 5);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 6);

		System.out.println("clear");
		list.clear();
		Assert.assertEquals(list.size(), 0);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 6);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 7);

		System.out.println("undo");
		((ClusteredList) list).undoLatest(7);
		Assert.assertEquals(list.size(), 5);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 6);

		System.out.println("3 undos");
		((ClusteredList) list).undoLatest(6);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 5);
		((ClusteredList) list).undoLatest(5);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 4);
		((ClusteredList) list).undoLatest(4);
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 2);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 3);

		System.out.println("Setting first element to on");
		((ClusteredList) list).set(0, "on");
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 3);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 4);

		System.out.println("undo");
		((ClusteredList) list).undoLatest(4);
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(((ClusteredList) list).getHistory().size(), 2);
		Assert.assertEquals(((ClusteredList) list).getVersion(), 3);

	}

	private void printList() {
		System.out.println("-----------");
		System.out.println("List Size:" + list.size());
		System.out.println("List Hist Size:"
				+ ((ClusteredList) list).getHistory().size());
		System.out.println("List:");
		for (int i = 0; i < list.size(); i++) {
			System.out.println("Element" + (i + 1) + ":" + list.get(i));
		}
		System.out.println("Hist:");
		for (int i = 0; i < ((ClusteredList) list).getHistory().size(); i++) {
			System.out.println("Element" + (i + 1) + ":"
					+ ((ClusteredList) list).getHistory().get(i));
		}

	}
}
