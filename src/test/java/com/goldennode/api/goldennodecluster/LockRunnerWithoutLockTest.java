package com.goldennode.api.goldennodecluster;

public class LockRunnerWithoutLockTest implements Runnable {
	private int threadNo;
	private ClusteredLockTest tl;
	private int loopCount;
	private int taskDuration;

	public LockRunnerWithoutLockTest(ClusteredLockTest tl, int threadNo, int loopCount, int taskDuration) {
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
