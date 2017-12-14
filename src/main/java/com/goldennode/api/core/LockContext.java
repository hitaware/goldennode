package com.goldennode.api.core;

public class LockContext {
    public static ThreadLocal<String> threadProcessId = new ThreadLocal<String>();
}
