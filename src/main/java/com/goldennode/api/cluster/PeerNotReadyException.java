package com.goldennode.api.cluster;

public class PeerNotReadyException extends OperationException {
	private static final long serialVersionUID = 1L;

	public PeerNotReadyException(Exception e) {
		super(e);
	}

	public PeerNotReadyException() {
		super();
	}
}
