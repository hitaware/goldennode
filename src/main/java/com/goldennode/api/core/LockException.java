package com.goldennode.api.core;

public class LockException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LockException(String text) {
        super(text);
    }
}
