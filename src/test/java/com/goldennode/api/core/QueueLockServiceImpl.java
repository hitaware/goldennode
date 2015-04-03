package com.goldennode.api.core;

public class QueueLockServiceImpl {
	int putptr, takeptr, count;
	final Object[] items;
	private LockService lock;
	private int notFull;
	private int notEmpty;

	public QueueLockServiceImpl(int size, LockService lock) {
		items = new Object[size];
		this.lock = lock;
		notFull = lock.newCondition("lock1");
		notEmpty = lock.newCondition("lock1");
	}

	public void put(Object x) throws InterruptedException {
		lock.lock("lock1", Thread.currentThread().getName(), 60000);
		try {
			while (count == items.length) {
				lock.await(notFull, Thread.currentThread().getName());
			}
			items[putptr] = x;
			if (++putptr == items.length) {
				putptr = 0;
			}
			++count;
			lock.signal(notEmpty, Thread.currentThread().getName());
		} finally {
			lock.unlock("lock1", Thread.currentThread().getName());
		}
	}

	public Object take() throws InterruptedException {
		lock.lock("lock1", Thread.currentThread().getName(), 60000);
		try {
			while (count == 0) {
				lock.await(notEmpty, Thread.currentThread().getName());
			}
			Object x = items[takeptr];
			if (++takeptr == items.length) {
				takeptr = 0;
			}
			--count;
			lock.signal(notFull, Thread.currentThread().getName());
			return x;
		} finally {
			lock.unlock("lock1", Thread.currentThread().getName());
		}
	}
}
