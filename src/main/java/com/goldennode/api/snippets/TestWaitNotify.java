package com.goldennode.api.snippets;

import java.util.concurrent.locks.ReentrantLock;

public class TestWaitNotify {

	Object lck = new Object();

	public static void main(String[] args) {

		TestWaitNotify twn = new TestWaitNotify();
		Thread th1 = new TH(twn.lck, 1);
		Thread th2 = new TH(twn.lck, 2);
		th1.start();
		th2.start();

	}

}

class TH extends Thread {

	private int ind;
	private Object lck;

	public TH(Object lck, int ind) {
		this.ind = ind;
		this.lck = lck;
	}

	public void doWait() {
		System.out.println("will sync myMonitorObject in doWait");
		synchronized (lck) {
			try {
				System.out.println("will wait");
				lck.wait();
				System.out.println("waited");
				for (int i = 0; i < 10; i++) {
					System.out.println("doing job in doWait" + i);
				}
				ReentrantLock lock = new ReentrantLock();

			} catch (InterruptedException e) {//

			}
			System.out.println("end wait");
		}
	}

	public void doNotify() {
		System.out.println("will sync myMonitorObject in doNotify");
		synchronized (lck) {
			System.out.println("will notify");
			lck.notify();
			System.out.println("notified");

			for (int i = 0; i < 10; i++) {
				System.out.println("doing job in doNotify" + i);
			}
			System.out.println("end notify");

		}
	}

	@Override
	public void run() {
		if (ind == 1) {
			doWait();
		}
		if (ind == 2) {
			doNotify();
		}

	}
}
