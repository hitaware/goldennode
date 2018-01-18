package com.goldennode.api.core;

public class PeerAlreadyStoppedException extends PeerException {
    public PeerAlreadyStoppedException(Exception e) {
        super(e);
    }

    public PeerAlreadyStoppedException() {
        super();
    }

    private static final long serialVersionUID = 8209782315122036217L;
}
