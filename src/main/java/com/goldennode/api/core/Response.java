package com.goldennode.api.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 1L;
	private Object returnValue;
	private Server serverFrom;
	private Request request;

	Response() {//
	}

	public Request getRequest() {
		return request;
	}

	void setRequest(Request request) {
		this.request = request;
	}

	public Server getServerFrom() {
		return serverFrom;
	}

	void setServerFrom(Server serverFrom) {
		this.serverFrom = serverFrom;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	@Override
	public String toString() {
		return "Response [returnValue=" + returnValue + ", serverFrom="
				+ serverFrom + ", size=" + getBytes().length + "]";
	}

	public byte[] getBytes() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream gos;
			gos = new ObjectOutputStream(bos);
			gos.writeObject(this);
			gos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}
}
