package com.goldennode.api.goldennodecluster;

public class TestLockRunnerWithoutLock implements Runnable {
	private int threadNo;
	private TestClusteredLock tl;
	private int loopCount;
	private int taskDuration;

	public TestLockRunnerWithoutLock(TestClusteredLock tl, int threadNo, int loopCount, int taskDuration) {
		this.threadNo = threadNo;
		this.tl = tl;
		this.loopCount = loopCount;
		this.taskDuration = taskDuration;
	}

	private void doJob() {
		try {
			for (int i = 0; i < loopCount; i++) {
				int tmp = tl.getCounter();
				Thread.sleep(taskDuration);
				tl.setCounter(tmp + 1);
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
