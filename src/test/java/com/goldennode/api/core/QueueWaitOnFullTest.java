package com.goldennode.api.core;

import java.util.Timer;
import java.util.TimerTask;

import org.junit.Assert;
import org.junit.Test;

public class QueueWaitOnFullTest {
	@Test
	public void test() throws InterruptedException {
		LockService ls = new LockServiceImpl();
		ls.createLock("lock1");
		final QueueLockServiceImpl q = new QueueLockServiceImpl(20, ls);
		final Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for (int i = 0; i < 60; i++) {
						System.out.println("Putting " + i);
						q.put(i);
					}
				} catch (InterruptedException e) {
				}
			}
		});
		final Thread th2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for (int i = 0; i < 5; i++) {
						// System.out.println("Polling " + q.take());
					}
				} catch (Exception e) {
				}
			}
		});
		th.start();
		th2.start();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				th.interrupt();
				th2.interrupt();
			}
		}, 3000);
		th.join();
		th2.join();
		Assert.assertEquals(20, q.count);
	}
}
