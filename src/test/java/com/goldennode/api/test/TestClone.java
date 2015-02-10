package com.goldennode.api.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestClone {

	List<InnerObject> list;
	List<InnerObject> list2;

	@Before
	public void init() {

		list = Collections.synchronizedList(new ArrayList<InnerObject>());
		list.add(new InnerObject(new Date()));
		list.add(new InnerObject(new Date()));

		list2 = new ArrayList<InnerObject>();
		list2.add(new InnerObject(new Date()));
		list2.add(new InnerObject(new Date()));
	}

	@Test
	public void test() throws Exception {

		System.out.println("Cloning lists");
		List<InnerObject> cloneList = Collections
				.synchronizedList(new ArrayList<InnerObject>(list));
		List<InnerObject> cloneList2 = (List<InnerObject>) ((ArrayList) list2)
				.clone();

		cloneList.add(new InnerObject(new Date()));
		cloneList2.add(new InnerObject(new Date()));

		Assert.assertNotEquals(list.size(), cloneList.size());
		Assert.assertNotEquals(list2.size(), cloneList2.size());

	}

	class InnerObject {
		public Date str;

		public InnerObject(Date str) {
			this.str = str;
		}

	}
}