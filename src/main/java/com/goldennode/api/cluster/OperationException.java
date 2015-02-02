package com.goldennode.api.cluster;

public class OperationException extends RuntimeException {

	public OperationException(Exception e) {
		super(e);
	}

	public OperationException(String string) {
		super(string);
	}

	public OperationException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
