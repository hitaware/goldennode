package com.goldennode.api.helper;

public class LockHelper {

	public static void sleep(int timeout) {

		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	public static void wait(Object object, int timeout) {
		synchronized (object) {
			try {
				object.wait(timeout);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

		}

	}

	public static void notifyAll(Object object) throws RuntimeException {
		synchronized (object) {

			object.notifyAll();

		}

	}

}
