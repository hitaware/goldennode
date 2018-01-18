package com.goldennode.api.core;

public class PeerNotStartedException extends PeerException {
    public PeerNotStartedException(Exception e) {
        super(e);
    }

    public PeerNotStartedException() {
        super();
    }

    private static final long serialVersionUID = 3543616569402815267L;
}
