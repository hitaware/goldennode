package com.goldennode.api.cluster;

public class ClusteredObjectNotAvailableException extends OperationException {
    private static final long serialVersionUID = 1L;

    public ClusteredObjectNotAvailableException(Exception e) {
        super(e);
    }

    public ClusteredObjectNotAvailableException() {
        super();
    }
}
