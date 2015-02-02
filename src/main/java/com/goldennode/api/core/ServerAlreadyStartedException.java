package com.goldennode.api.core;

public class ServerAlreadyStartedException extends ServerException {

	public ServerAlreadyStartedException(Exception e) {
		super(e);

	}

	public ServerAlreadyStartedException() {
		super();
	}

	private static final long serialVersionUID = 5932162096574588769L;
	//
}
