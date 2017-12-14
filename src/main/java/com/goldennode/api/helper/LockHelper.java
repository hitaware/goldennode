package com.goldennode.api.helper;

public class LockHelper {
    public static void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep(int timeout) {
        LockHelper.sleep((long) timeout);
    }
}
