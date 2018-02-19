package com.goldennode.api.core;

public class CallTimeoutException extends PeerException {

    private static final long serialVersionUID = 1L;

    public CallTimeoutException(String str) {
        super(str);
    }
}
