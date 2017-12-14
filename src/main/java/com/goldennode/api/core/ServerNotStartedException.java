package com.goldennode.api.core;

public class ServerNotStartedException extends ServerException {
    public ServerNotStartedException(Exception e) {
        super(e);
    }

    public ServerNotStartedException() {
        super();
    }

    private static final long serialVersionUID = 3543616569402815267L;
}
