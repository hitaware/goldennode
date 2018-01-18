package com.goldennode.api.core;

public class CallTimeoutException extends PeerException {
    private static final long serialVersionUID = -5041355222846712138L;

    public CallTimeoutException(String str) {
        super(str);
    }
}
