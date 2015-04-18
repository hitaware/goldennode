package com.goldennode.api.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Operation implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<Object> params = Collections.synchronizedList(new ArrayList<Object>());
	private String method;
	private String id;
	private String objectPublicName;
	private String objectMethod;

	public Operation(String objectPublicName, String objectMethod, Object... params) {
		id = java.util.UUID.randomUUID().toString();
		method = "op_";
		setParams(params);
		this.objectMethod = objectMethod;
		this.objectPublicName = objectPublicName;
	}

	private void setParams(Object[] params) {
		this.params.clear();
		for (int i = 0; i < params.length; i++) {
			this.params.add(params[i]);
		}
	}

	public String getObjectMethod() {
		return "_" + objectMethod;
	}

	public String getObjectPublicName() {
		return objectPublicName;
	}

	public String getId() {
		return id;
	}

	public String getMethod() {
		return "_" + method;
	}

	public Object[] getParams() {
		return params.toArray();
	}

	public int paramSize() {
		return params.size();
	}

	public Object get(int index) {
		return params.get(index);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < params.size(); i++) {
			sb.append("Param(" + (i + 1) + ") =" + params.get(i) + " ");
		}
		return " > Operation [objectMethod=" + objectMethod + "," + sb.toString() + "] ";
	}
}
