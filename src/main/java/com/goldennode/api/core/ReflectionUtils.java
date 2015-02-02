package com.goldennode.api.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

	public static Object callMethod(Object cls, String methodName,
			Object[] params) throws Exception {

		try {
			@SuppressWarnings("rawtypes")
			Class[] cz = new Class[params.length];
			for (int i = 0; i < params.length; i++) {
				Object s = params[i];
				cz[i] = s.getClass();
			}
			Method m = MethodUtils.getMatchingAccessibleMethod(cls.getClass(),
					methodName, cz);

			return m.invoke(cls, params);
		} catch (InvocationTargetException e) {
			throw e;
		}

	}
}
