package com.goldennode.testutils;

import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.LockHelper;

public class ThreadUtils {
    private static final int THREAD_CHECK_RETRY_INTERVAL = 10;
    private static final int THREAD_CHECK_RETRY = 10;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);

    public static boolean hasThreadNamedLike(String text) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            t.isAlive();
            if (t.getName().contains(text)) {
                for (int i = 0; i < ThreadUtils.THREAD_CHECK_RETRY; i++) {
                    if (t.isAlive()) {// TODO find another way of refreshing the
                                      // thread getAllStackTraces set
                        LockHelper.sleep(ThreadUtils.THREAD_CHECK_RETRY_INTERVAL);
                    }
                }
                if (t.isAlive()) {
                    LOGGER.debug("Thread is alive");
                    return true;
                } else {
                    LOGGER.debug("Thread is dead");
                    return false;
                }
            }
        }
        return false;
    }

    public static void threadInterrupter(final Thread th, long delay) {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                th.interrupt();
            }
        }, delay);
    }

    public static void run(final Runnable r, int delay) {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, delay);
    }

    public static void run(final Runnable r) {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, 0);
    }

    public static Thread[] run(Runnable r, int threadCount, boolean start, boolean join) throws InterruptedException {
        Thread[] th = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            th[i] = new Thread(r);
        }
        if (start) {
            for (int i = 0; i < threadCount; i++) {
                th[i].start();
            }
            if (join) {
                for (int i = 0; i < threadCount; i++) {
                    th[i].join();
                }
            }
        }
        return th;
    }
}
