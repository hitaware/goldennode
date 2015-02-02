package com.goldennode.api.test;

import java.util.List;

import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.OperationException;

public class TestUndo {
	static List<String> list = new ClusteredList<String>();

	public static void main(String[] args) {

		System.out.println("Adding 5 items");
		list.add("bir");
		list.add("iki");
		list.add("uc");
		list.add("dort");
		list.add("bes");
		printList();
		System.out.println("Removing 2nd index");
		list.remove(2);
		printList();
		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(1);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		printList();
		System.out.println("Adding alti");
		list.add("alti");
		printList();

		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(2);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		printList();
		System.out.println("clear");
		list.clear();
		printList();
		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(3);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		printList();

		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(4);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(5);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(6);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		printList();

		((ClusteredList) list).set(0, "on");
		printList();
		try {
			System.out.println("undo");
			((ClusteredList) list).undoLatest(7);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		printList();
	}

	public static void printList() {
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
