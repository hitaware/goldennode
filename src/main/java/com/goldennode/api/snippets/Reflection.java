package com.goldennode.api.snippets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.goldennode.api.core.MethodUtils;

public class Reflection<T> {

	public void _process(String publicName, Object... params)
			throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException {

	}

	public static void main(String[] args) {

		try {
			Class c = Reflection.class;
			Reflection ref = new Reflection();
			Class[] params = new Class[] { String.class };
			Method m = MethodUtils.getMatchingAccessibleMethod(
					Reflection.class, "_process", String.class, Object[].class);
			m.invoke(ref, "ozgen", null);
			System.out.println(m);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
