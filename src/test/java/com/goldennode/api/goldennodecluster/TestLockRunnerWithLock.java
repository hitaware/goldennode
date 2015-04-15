package com.goldennode.api.goldennodecluster;

public class TestLockRunnerWithLock implements Runnable {
	private int threadNo;
	private TestClusteredLock tl;
	private int loopCount;
	private int taskDuration;

	public TestLockRunnerWithLock(TestClusteredLock tl, int threadNo, int loopCount, int taskDuration) {
		this.threadNo = threadNo;
		this.tl = tl;
		this.loopCount = loopCount;
		this.taskDuration = taskDuration;
	}

	private void doJob() {
		try {
			for (int i = 0; i < loopCount; i++) {
				tl.lock[threadNo].lock();
				int tmp = tl.getCounter();
				Thread.sleep(taskDuration);
				tl.setCounter(tmp + 1);
				tl.lock[threadNo].unlock();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		doJob();
	}
}
