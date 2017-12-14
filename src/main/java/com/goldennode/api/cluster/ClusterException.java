package com.goldennode.api.cluster;

public class ClusterException extends Exception {
    private static final long serialVersionUID = 1L;

    public ClusterException() {
        super();
    }

    public ClusterException(Exception e) {
        super(e);
    }

    public ClusterException(String str) {
        super(str);
    }

    public ClusterException(Throwable cause) {
        super(cause);
    }
}
