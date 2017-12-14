package com.goldennode.api.snippets;

import org.slf4j.LoggerFactory;

public class WaitNotify {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WaitNotify.class);
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
    private int ind;
    private Object lck;

    public TH(Object lck, int ind) {
        this.ind = ind;
        this.lck = lck;
    }

    public void doWait() {
        WaitNotify.LOGGER.debug("will sync myMonitorObject in doWait");
        synchronized (lck) {
            try {
                WaitNotify.LOGGER.debug("will wait");
                lck.wait();
                WaitNotify.LOGGER.debug("waited");
                for (int i = 0; i < 10; i++) {
                    WaitNotify.LOGGER.debug("doing job in doWait" + i);
                }
            } catch (InterruptedException e) {//
            }
            WaitNotify.LOGGER.debug("end wait");
        }
    }

    public void doNotify() {
        WaitNotify.LOGGER.debug("will sync myMonitorObject in doNotify");
        synchronized (lck) {
            WaitNotify.LOGGER.debug("will notify");
            lck.notify();
            WaitNotify.LOGGER.debug("notified");
            for (int i = 0; i < 10; i++) {
                WaitNotify.LOGGER.debug("doing job in doNotify" + i);
            }
            WaitNotify.LOGGER.debug("end notify");
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
