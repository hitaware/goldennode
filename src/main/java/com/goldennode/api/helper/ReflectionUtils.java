package com.goldennode.api.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Object callMethod(Object cls, String methodName, Object[] params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        @SuppressWarnings("rawtypes")
        Class[] cz = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            Object s = params[i];
            if (s == null) {
                cz[i] = null;
            } else {
                cz[i] = s.getClass();
            }
        }
        Method m = MethodUtils.getMatchingAccessibleMethod(cls.getClass(), methodName, cz);
        if (m == null) {
            throw new NoSuchMethodException(methodName);
        }
        return m.invoke(cls, params);
    }
}
