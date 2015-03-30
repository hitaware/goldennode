package com.goldennode.api.snippets;

public class ThreadRunner {

	public static Runnable run(Runnable r, int threadCount, boolean start,
			boolean join) throws Exception {

		Thread[] th = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			th[i] = new Thread(r);
		}

		if (start) {
			for (int i = 0; i < threadCount; i++) {
				th[i].start();
			}
			if (join) {
				for (int i = 0; i < threadCount; i++) {
					th[i].join();
				}
			}
		}
		return r;

	}

}
