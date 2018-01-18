package com.goldennode.api.core;

public class PeerAlreadyStartedException extends PeerException {
    public PeerAlreadyStartedException(Exception e) {
        super(e);
    }

    public PeerAlreadyStartedException() {
        super();
    }

    private static final long serialVersionUID = 5932162096574588769L;
    //
}
