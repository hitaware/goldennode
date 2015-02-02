package com.goldennode.api.core;

public class ServerException extends Exception {

	private static final long serialVersionUID = 4558751574175980501L;

	public ServerException(String str) {
		super(str);
	}

	public ServerException(Exception e) {
		super(e);
	}

	public ServerException() {
		super();
	}

	public ServerException(Throwable cause) {
		super(cause);
	}
}
