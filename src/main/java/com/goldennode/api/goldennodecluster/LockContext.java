package com.goldennode.api.goldennodecluster;

public class LockContext {
	public static ThreadLocal<String> threadProcessId = new ThreadLocal<String>();
}
