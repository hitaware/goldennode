package com.goldennode.api.helper;

import org.junit.Test;

import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;
import com.goldennode.testutils.ThreadUtils;

public class LockHelperTest extends GoldenNodeJunitRunner {
    @Test(timeout = 1100)
    @RepeatTest(times = 1)
    public void sleep() {
        final long waitInMs = 1000;
        ThreadUtils.threadInterrupter(Thread.currentThread(), waitInMs);
        LockHelper.sleep(waitInMs + 100);
        try {
            synchronized (this) {
                wait(waitInMs);
            }
        } catch (InterruptedException e) {
            // Don't throw
        }
    }

    @Test(expected = InterruptedException.class, timeout = 1100)
    @RepeatTest(times = 1)
    public void threadSleep() throws InterruptedException {
        final long waitInMs = 1000;
        ThreadUtils.threadInterrupter(Thread.currentThread(), waitInMs);
        Thread.sleep(waitInMs + 100);
    }
}
