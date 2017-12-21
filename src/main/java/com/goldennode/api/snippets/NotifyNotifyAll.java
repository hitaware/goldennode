package com.goldennode.api.snippets;

import org.slf4j.LoggerFactory;

/**
 * Java program to demonstrate How to use notify and notifyAll method in Java
 * and How notify and notifyAll method notifies thread, which thread gets woke
 * up etc.
 */
public class NotifyNotifyAll {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NotifyNotifyAll.class);
    @SuppressWarnings("unused")
    private volatile boolean go = false;

    public static void main(String args[]) throws InterruptedException {
        final NotifyNotifyAll test = new NotifyNotifyAll();
        Runnable waitTask = () -> {
            try {
                test.waitMeth();
            } catch (InterruptedException ex) {
                LOGGER.error("Error occured", ex);
            }
            LOGGER.debug(Thread.currentThread() + " finished Execution(wait test)");
        };
        Runnable notifyTask = () -> {
            test.notifyMeth();
            LOGGER.debug(Thread.currentThread() + " finished Execution(notify test)");
        };
        Thread t1 = new Thread(waitTask, "WT1"); // will wait
        Thread t2 = new Thread(waitTask, "WT2"); // will wait
        Thread t3 = new Thread(waitTask, "WT3"); // will wait
        Thread t4 = new Thread(notifyTask, "NT1"); // will notify
        // starting all waiting thread
        t1.start();
        t2.start();
        t3.start();
        // pause to ensure all waiting thread started successfully
        Thread.sleep(200);
        // starting notifying thread
        t4.start();
    }

    /*
     * wait and notify can only be called from synchronized method or bock
     */
    private synchronized void waitMeth() throws InterruptedException {
        // while (go != true) {
        LOGGER.debug(Thread.currentThread() + " is going to wait on this object");
        wait(); // release lock and reacquires on wakeup
        LOGGER.debug(Thread.currentThread() + " is woken up");
        // }
        go = false; // resetting condition
    }

    /*
     * both shouldGo() and go() are locked on current object referenced by
     * "this" keyword
     */
    private synchronized void notifyMeth() {
        // while (go == false) {
        LOGGER.debug(Thread.currentThread() + " is going to notify all or one thread waiting on this object");
        go = true; // making condition true for waiting thread
        // notify(); // only one out of three waiting thread WT1, WT2,WT3
        // will woke up
        notifyAll(); // all waiting thread WT1, WT2,WT3 will woke up
        // }
    }
}