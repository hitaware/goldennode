package com.goldennode.api.test;

import java.util.concurrent.locks.Lock;

import com.goldennode.api.cluster.ClusteredLock;

public class TestLock extends Thread {

	Lock l = new ClusteredLock();

	public TestLock() {

	}

	@Override
	public void run() {
		try {
			l.lockInterruptibly();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		Thread.currentThread().interrupt();
		// Thread.currentThread().interrupt();
		// l.unlock();

	}

	public static void main(String[] args) {
		final TestLock tl = new TestLock();
		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				tl.run();
			}
		});
		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				tl.run();
			}
		});
		th1.start();
		// th2.start();

	}

}
