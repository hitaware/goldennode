package com.goldennode.testutils;

import java.util.TimerTask;

public class ThreadUtils {
	public static void threadInterrupter(final Thread th, int delay) {
		new java.util.Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				th.interrupt();
			}
		}, delay);
	}

	public static void run(final Runnable r, int delay) {
		new java.util.Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		}, delay);
	}

	public static void run(final Runnable r) {
		new java.util.Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		}, 0);
	}

	public static Runnable run(Runnable r, int threadCount, boolean start, boolean join) throws InterruptedException {
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
