package com.goldennode.api.goldennodegrid;

public class DistriburedObjectNotAvailableException extends OperationException {
    private static final long serialVersionUID = 1L;

    public DistriburedObjectNotAvailableException(Exception e) {
        super(e);
    }

    public DistriburedObjectNotAvailableException() {
        super();
    }
}
