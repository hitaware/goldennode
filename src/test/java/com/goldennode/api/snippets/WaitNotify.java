package com.goldennode.api.snippets;

import org.slf4j.LoggerFactory;

public class WaitNotify {

	static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(WaitNotify.class);

	Object lck = new Object();

	public static void main(String[] args) {

		WaitNotify twn = new WaitNotify();
		Thread th1 = new TH(twn.lck, 1);
		Thread th2 = new TH(twn.lck, 2);
		th1.start();
		th2.start();

	}

}

class TH extends Thread {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TH.class);

	private int ind;
	private Object lck;

	public TH(Object lck, int ind) {
		this.ind = ind;
		this.lck = lck;
	}

	public void doWait() {
		LOGGER.debug("will sync myMonitorObject in doWait");
		synchronized (lck) {
			try {
				LOGGER.debug("will wait");
				lck.wait();
				LOGGER.debug("waited");
				for (int i = 0; i < 10; i++) {
					LOGGER.debug("doing job in doWait" + i);
				}

			} catch (InterruptedException e) {//

			}
			LOGGER.debug("end wait");
		}
	}

	public void doNotify() {
		LOGGER.debug("will sync myMonitorObject in doNotify");
		synchronized (lck) {
			LOGGER.debug("will notify");
			lck.notify();
			LOGGER.debug("notified");

			for (int i = 0; i < 10; i++) {
				LOGGER.debug("doing job in doNotify" + i);
			}
			LOGGER.debug("end notify");

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
