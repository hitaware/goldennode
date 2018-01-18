package com.goldennode.api.grid;

public class GridException extends Exception {
    private static final long serialVersionUID = 1L;

    public GridException() {
        super();
    }

    public GridException(Exception e) {
        super(e);
    }

    public GridException(String str) {
        super(str);
    }

    public GridException(Throwable cause) {
        super(cause);
    }
}
