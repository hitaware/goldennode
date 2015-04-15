package com.goldennode.api.cluster;

public class NonUniqueResultException extends ClusterException {
	private static final long serialVersionUID = 1L;

	public NonUniqueResultException(String string) {
		super(string);
	}
}
