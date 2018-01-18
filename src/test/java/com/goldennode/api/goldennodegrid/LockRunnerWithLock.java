package com.goldennode.api.goldennodegrid;

import org.slf4j.LoggerFactory;

public class LockRunnerWithLock implements Runnable {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LockRunnerWithLock.class);
    private int threadNo;
    private DistributedLockTest tl;
    private int loopCount;
    private int taskDuration;

    public LockRunnerWithLock(DistributedLockTest tl, int threadNo, int loopCount, int taskDuration) {
        this.threadNo = threadNo;
        this.tl = tl;
        this.loopCount = loopCount;
        this.taskDuration = taskDuration;
    }

    private void doJob() {
        try {
            for (int i = 0; i < loopCount; i++) {
                LOGGER.debug("will acquire lock " + tl.lock[threadNo]);
                tl.lock[threadNo].lock();
                LOGGER.debug("acquired lock " + tl.lock[threadNo]);
                int tmp = tl.getCounter();
                Thread.sleep(taskDuration);
                tl.setCounter(tmp + 1);
                tl.lock[threadNo].unlock();
                LOGGER.debug("released lock " + tl.lock[threadNo]);
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
