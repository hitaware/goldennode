package com.goldennode.api.helper;

public class ExceptionUtils {
    public static boolean hasCause(Throwable t, Class<? extends Throwable> cause) {
        while (t != null) {
            if (cause.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    public static void throwCauseIfThereIs(Throwable t, Class<? extends Throwable> cause) throws Throwable {
        while (t != null) {
            if (cause.isInstance(t)) {
                throw t;
            }
            t = t.getCause();
        }
    }
}
