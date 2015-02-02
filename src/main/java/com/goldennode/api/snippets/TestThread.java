package com.goldennode.api.snippets;

import com.goldennode.api.core.Logger;

/**
 * Java program to demonstrate How to use notify and notifyAll method in Java
 * and How notify and notifyAll method notifies thread, which thread gets woke
 * up etc.
 */
public class TestThread {

	private volatile boolean go = false;

	public static void main(String args[]) throws InterruptedException {
		final TestThread test = new TestThread();

		Runnable waitTask = new Runnable() {

			@Override
			public void run() {
				try {
					test.waitMeth();
				} catch (InterruptedException ex) {
					Logger.error(ex);
				}
				Logger.debug(Thread.currentThread()
						+ " finished Execution(wait test)");
			}
		};

		Runnable notifyTask = new Runnable() {

			@Override
			public void run() {
				test.notifyMeth();
				Logger.debug(Thread.currentThread()
						+ " finished Execution(notify test)");
			}
		};

		Thread t1 = new Thread(waitTask, "WT1"); // will wait
		Thread t2 = new Thread(waitTask, "WT2"); // will wait
		Thread t3 = new Thread(waitTask, "WT3"); // will wait
		Thread t4 = new Thread(notifyTask, "NT1"); // will notify

		// starting all waiting thread
		t1.start();
		t2.start();
		t3.start();

		// pause to ensure all waiting thread started successfully
		Thread.sleep(200);

		// starting notifying thread
		t4.start();

	}

	/*
	 * wait and notify can only be called from synchronized method or bock
	 */
	private synchronized void waitMeth() throws InterruptedException {
		// while (go != true) {
		Logger.debug(Thread.currentThread()
				+ " is going to wait on this object");
		wait(); // release lock and reacquires on wakeup
		Logger.debug(Thread.currentThread() + " is woken up");
		// }
		go = false; // resetting condition
	}

	/*
	 * both shouldGo() and go() are locked on current object referenced by
	 * "this" keyword
	 */
	private synchronized void notifyMeth() {
		// while (go == false) {
		Logger.debug(Thread.currentThread()
				+ " is going to notify all or one thread waiting on this object");

		go = true; // making condition true for waiting thread
		// notify(); // only one out of three waiting thread WT1, WT2,WT3
		// will woke up
		notifyAll(); // all waiting thread WT1, WT2,WT3 will woke up
		// }

	}

}