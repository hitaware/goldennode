package com.goldennode.api.core;

public class RequestOptions {

	private int timeout = 60000;

	public RequestOptions() {
	}

	public RequestOptions(int timeout) {
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
