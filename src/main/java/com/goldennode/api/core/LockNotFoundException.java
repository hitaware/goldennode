package com.goldennode.api.core;

public class LockNotFoundException extends LockException {
    private static final long serialVersionUID = 1L;

    public LockNotFoundException(String lockName) {
        super("lock not found > " + lockName);
    }
}
