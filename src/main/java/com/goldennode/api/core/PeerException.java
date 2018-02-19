package com.goldennode.api.core;

public class PeerException extends Exception {
    private static final long serialVersionUID = 1L;

    public PeerException(String str) {
        super(str);
    }

    public PeerException(Exception e) {
        super(e);
    }

    public PeerException() {
        super();
    }

    public PeerException(Throwable cause) {
        super(cause);
    }
}
