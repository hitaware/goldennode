package com.goldennode.api.cluster;

import java.util.HashMap;

public class ClusterContext {
	public static final String PROCESS_ID = "_fom.userid_";

	private static final String _RESET_TIME = "_fom.resettime_";

	private static Context context = new Context();

	private static class Context extends ThreadLocal<HashMap<String, Object>> {
		@Override
		public HashMap<String, Object> initialValue() {
			HashMap<String, Object> ctx = new HashMap<String, Object>();
			ctx.put(ClusterContext._RESET_TIME, new Long(System.currentTimeMillis()));
			return ctx;
		}

		public HashMap<String, Object> getContext() {
			return super.get();
		}
	}

	public static void reset() {
		context.getContext().clear();
		context.getContext().put(ClusterContext._RESET_TIME, new Long(System.currentTimeMillis()));
	}

	public static void put(String key, Object value) {
		context.getContext().put(key, value);
	}

	public static Object get(String key) {
		return context.getContext().get(key);
	}

	public static long timeElapsed() {
		long now = System.currentTimeMillis();
		Long lastResetTime = (Long) context.getContext().get(ClusterContext._RESET_TIME);
		return now - lastResetTime.longValue();
	}

}
