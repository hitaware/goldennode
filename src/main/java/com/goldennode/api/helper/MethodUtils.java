package com.goldennode.api.helper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class MethodUtils {
    public static Method getMatchingAccessibleMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = cls.getMethod(methodName, parameterTypes);
            MethodUtils.setAccessibleWorkaround(method);
            return method;
        } catch (NoSuchMethodException e) { // NOPMD - Swallow the exception
        }
        // search through all methods
        Method bestMatch = null;
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            // compare name and parameters
            if (method.getName().equals(methodName)
                    && MethodUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                // get accessible version of method
                Method accessibleMethod = MethodUtils.getAccessibleMethod(method);
                if (accessibleMethod != null
                        && (bestMatch == null || MethodUtils.compareParameterTypes(accessibleMethod.getParameterTypes(),
                                bestMatch.getParameterTypes(), parameterTypes) < 0)) {
                    bestMatch = accessibleMethod;
                }
            }
        }
        if (bestMatch != null) {
            MethodUtils.setAccessibleWorkaround(bestMatch);
        }
        return bestMatch;
    }

    public static Method getAccessibleMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        try {
            return MethodUtils.getAccessibleMethod(cls.getMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getAccessibleMethod(Method method) {
        if (!MethodUtils.isAccessible(method)) {
            return null;
        }
        // If the declaring class is public, we are done
        Class<?> cls = method.getDeclaringClass();
        if (Modifier.isPublic(cls.getModifiers())) {
            return method;
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        // Check the implemented interfaces and subinterfaces
        method = MethodUtils.getAccessibleMethodFromInterfaceNest(cls, methodName, parameterTypes);
        // Check the superclass chain
        if (method == null) {
            method = MethodUtils.getAccessibleMethodFromSuperclass(cls, methodName, parameterTypes);
        }
        return method;
    }

    private static Method getAccessibleMethodFromSuperclass(Class<?> cls, String methodName,
            Class<?>... parameterTypes) {
        Class<?> parentClass = cls.getSuperclass();
        while (parentClass != null) {
            if (Modifier.isPublic(parentClass.getModifiers())) {
                try {
                    return parentClass.getMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    return null;
                }
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    static boolean isAccessible(Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

    static void setAccessibleWorkaround(AccessibleObject o) {
        if (o == null || o.isAccessible()) {
            return;
        }
        Member m = (Member) o;
        if (Modifier.isPublic(m.getModifiers()) && MethodUtils.isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                o.setAccessible(true);
            } catch (SecurityException e) { // NOPMD
                // ignore in favor of subsequent IllegalAccessException
            }
        }
    }

    static boolean isPackageAccess(int modifiers) {
        return (modifiers & MethodUtils.ACCESS_TEST) == 0;
    }

    private static final int ACCESS_TEST = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    static int compareParameterTypes(Class<?>[] left, Class<?>[] right, Class<?>[] actual) {
        float leftCost = MethodUtils.getTotalTransformationCost(actual, left);
        float rightCost = MethodUtils.getTotalTransformationCost(actual, right);
        return leftCost < rightCost ? -1 : rightCost < leftCost ? 1 : 0;
    }

    private static float getTotalTransformationCost(Class<?>[] srcArgs, Class<?>[] destArgs) {
        float totalCost = 0.0f;
        for (int i = 0; i < srcArgs.length; i++) {
            Class<?> srcClass, destClass;
            srcClass = srcArgs[i];
            destClass = destArgs[i];
            totalCost += MethodUtils.getObjectTransformationCost(srcClass, destClass);
        }
        return totalCost;
    }

    private static float getObjectTransformationCost(Class<?> srcClass, Class<?> destClass) {
        if (destClass.isPrimitive()) {
            return MethodUtils.getPrimitivePromotionCost(srcClass, destClass);
        }
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isInterface() && MethodUtils.isAssignable(srcClass, destClass)) {
                // slight penalty for interface match.
                // we still want an exact match to override an interface match,
                // but
                // an interface match should override anything where we have to
                // get a superclass.
                cost += 0.25f;
                break;
            }
            cost++;
            srcClass = srcClass.getSuperclass();
        }
        /*
         * If the destination class is null, we've travelled all the way up to
         * an Object match. We'll penalize this by adding 1.5 to the cost.
         */
        if (srcClass == null) {
            cost += 1.5f;
        }
        return cost;
    }

    private static float getPrimitivePromotionCost(final Class<?> srcClass, final Class<?> destClass) {
        float cost = 0.0f;
        Class<?> cls = srcClass;
        if (!cls.isPrimitive()) {
            // slight unwrapping penalty
            cost += 0.1f;
            cls = MethodUtils.wrapperToPrimitive(cls);
        }
        for (int i = 0; cls != destClass && i < MethodUtils.ORDERED_PRIMITIVE_TYPES.length; i++) {
            if (cls == MethodUtils.ORDERED_PRIMITIVE_TYPES[i]) {
                cost += 0.1f;
                if (i < MethodUtils.ORDERED_PRIMITIVE_TYPES.length - 1) {
                    cls = MethodUtils.ORDERED_PRIMITIVE_TYPES[i + 1];
                }
            }
        }
        return cost;
    }

    private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = { Byte.TYPE, Short.TYPE, Character.TYPE, Integer.TYPE,
            Long.TYPE, Float.TYPE, Double.TYPE };

    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls, String methodName,
            Class<?>... parameterTypes) {
        Method method = null;
        // Search up the superclass chain
        for (; cls != null; cls = cls.getSuperclass()) {
            // Check the implemented interfaces of the parent class
            Class<?>[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                // Is this interface public?
                if (!Modifier.isPublic(interfaces[i].getModifiers())) {
                    continue;
                }
                // Does the method exist on this interface?
                try {
                    method = interfaces[i].getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) { // NOPMD
                    /*
                     * Swallow, if no method is found after the loop then this
                     * method returns null.
                     */
                }
                if (method != null) {
                    break;
                }
                // Recursively check our parent interfaces
                method = MethodUtils.getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
                if (method != null) {
                    break;
                }
            }
        }
        return method;
    }

    public static Class<?> wrapperToPrimitive(Class<?> cls) {
        return MethodUtils.wrapperPrimitiveMap.get(cls);
    }

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
    static {
        MethodUtils.primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        MethodUtils.primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        MethodUtils.primitiveWrapperMap.put(Character.TYPE, Character.class);
        MethodUtils.primitiveWrapperMap.put(Short.TYPE, Short.class);
        MethodUtils.primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        MethodUtils.primitiveWrapperMap.put(Long.TYPE, Long.class);
        MethodUtils.primitiveWrapperMap.put(Double.TYPE, Double.class);
        MethodUtils.primitiveWrapperMap.put(Float.TYPE, Float.class);
        MethodUtils.primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();
    static {
        for (Class<?> primitiveClass : MethodUtils.primitiveWrapperMap.keySet()) {
            Class<?> wrapperClass = MethodUtils.primitiveWrapperMap.get(primitiveClass);
            if (!primitiveClass.equals(wrapperClass)) {
                MethodUtils.wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?>... toClassArray) {
        return MethodUtils.isAssignable(classArray, toClassArray,
                MethodUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
    }

    public static boolean isJavaVersionAtLeast(JavaVersion requiredVersion) {
        return MethodUtils.JAVA_SPECIFICATION_VERSION_AS_ENUM.atLeast(requiredVersion);
    }

    public enum JavaVersion {
        /**
         * The Java version reported by Android. This is not an official Java
         * version number.
         */
        JAVA_0_9(1.5f, "0.9"),
        /**
         * Java 1.1.
         */
        JAVA_1_1(1.1f, "1.1"),
        /**
         * Java 1.2.
         */
        JAVA_1_2(1.2f, "1.2"),
        /**
         * Java 1.3.
         */
        JAVA_1_3(1.3f, "1.3"),
        /**
         * Java 1.4.
         */
        JAVA_1_4(1.4f, "1.4"),
        /**
         * Java 1.5.
         */
        JAVA_1_5(1.5f, "1.5"),
        /**
         * Java 1.6.
         */
        JAVA_1_6(1.6f, "1.6"),
        /**
         * Java 1.7.
         */
        JAVA_1_7(1.7f, "1.7"),
        /**
         * Java 1.8.
         */
        JAVA_1_8(1.8f, "1.8");
        /**
         * The float value.
         */
        private float value;
        /**
         * The standard name.
         */
        private String name;

        /**
         * Constructor.
         *
         * @param value
         *            the float value
         * @param name
         *            the standard name, not null
         */
        JavaVersion(final float value, final String name) {
            this.value = value;
            this.name = name;
        }

        // -----------------------------------------------------------------------
        /**
         * <p>
         * Whether this version of Java is at least the version of Java passed
         * in.
         * </p>
         *
         * <p>
         * For example:<br>
         * {@code myVersion.atLeast(JavaVersion.JAVA_1_4)}
         * <p>
         *
         * @param requiredVersion
         *            the version to check against, not null
         * @return true if this version is equal to or greater than the
         *         specified version
         */
        public boolean atLeast(JavaVersion requiredVersion) {
            return value >= requiredVersion.value;
        }

        /**
         * Transforms the given string with a Java version number to the
         * corresponding constant of this enumeration class. This method is used
         * internally.
         *
         * @param nom
         *            the Java version as string
         * @return the corresponding enumeration constant or <b>null</b> if the
         *         version is unknown
         */
        // helper for static importing
        static JavaVersion getJavaVersion(final String nom) {
            return JavaVersion.get(nom);
        }

        /**
         * Transforms the given string with a Java version number to the
         * corresponding constant of this enumeration class. This method is used
         * internally.
         *
         * @param nom
         *            the Java version as string
         * @return the corresponding enumeration constant or <b>null</b> if the
         *         version is unknown
         */
        static JavaVersion get(final String nom) {
            if ("0.9".equals(nom)) {
                return JAVA_0_9;
            } else if ("1.1".equals(nom)) {
                return JAVA_1_1;
            } else if ("1.2".equals(nom)) {
                return JAVA_1_2;
            } else if ("1.3".equals(nom)) {
                return JAVA_1_3;
            } else if ("1.4".equals(nom)) {
                return JAVA_1_4;
            } else if ("1.5".equals(nom)) {
                return JAVA_1_5;
            } else if ("1.6".equals(nom)) {
                return JAVA_1_6;
            } else if ("1.7".equals(nom)) {
                return JAVA_1_7;
            } else if ("1.8".equals(nom)) {
                return JAVA_1_8;
            } else {
                return null;
            }
        }

        // -----------------------------------------------------------------------
        /**
         * <p>
         * The string value is overridden to return the standard name.
         * </p>
         *
         * <p>
         * For example, <code>"1.5"</code>.
         * </p>
         *
         * @return the name, not null
         */
        @Override
        public String toString() {
            return name;
        }
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, boolean autoboxing) {
        if (MethodUtils.isSameLength(classArray, toClassArray) == false) {
            return false;
        }
        if (classArray == null) {
            classArray = MethodUtils.EMPTY_CLASS_ARRAY;
        }
        if (toClassArray == null) {
            toClassArray = MethodUtils.EMPTY_CLASS_ARRAY;
        }
        for (int i = 0; i < classArray.length; i++) {
            if (MethodUtils.isAssignable(classArray[i], toClassArray[i], autoboxing) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
        return MethodUtils.isAssignable(cls, toClass, MethodUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
    }

    public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        // autoboxing:
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = MethodUtils.primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = MethodUtils.wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (toClass.isPrimitive() == false) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    public static final String JAVA_SPECIFICATION_VERSION = MethodUtils.getSystemProperty("java.specification.version");

    private static String getSystemProperty(String property) {
        try {
            return System.getProperty(property);
        } catch (SecurityException ex) {
            // we are not allowed to look at this property
            System.err.println("Caught a SecurityException reading the system property '" + property
                    + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

    private static final JavaVersion JAVA_SPECIFICATION_VERSION_AS_ENUM = JavaVersion
            .get(MethodUtils.JAVA_SPECIFICATION_VERSION);

    public static Class<?> primitiveToWrapper(Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = MethodUtils.primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    public static boolean isSameLength(Object[] array1, Object[] array2) {
        if (array1 == null && array2 != null && array2.length > 0
                || array2 == null && array1 != null && array1.length > 0
                || array1 != null && array2 != null && array1.length != array2.length) {
            return false;
        }
        return true;
    }
}
