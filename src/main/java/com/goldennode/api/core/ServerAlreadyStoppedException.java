package com.goldennode.api.core;

public class ServerAlreadyStoppedException extends ServerException {

	public ServerAlreadyStoppedException(Exception e) {
		super(e);

	}

	public ServerAlreadyStoppedException() {
		super();
	}

	private static final long serialVersionUID = 8209782315122036217L;

}
