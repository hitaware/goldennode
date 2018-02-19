package com.goldennode.api.goldennodegrid;

public class OperationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OperationException(Exception e) {
        super(e);
    }

    public OperationException(String string) {
        super(string);
    }

    public OperationException(Throwable cause) {
        super(cause);
    }

    public OperationException() {
        super();
    }
}
