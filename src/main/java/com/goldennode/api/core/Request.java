package com.goldennode.api.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.goldennode.api.helper.StringUtils;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String method;
    private ArrayList<Object> params = new ArrayList<Object>();
    private RequestType requestType;
    private Peer peerFrom;
    private int timeout;// seconds
    private String processId;

    Request() {
        id = java.util.UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params.toArray();
    }

    void addParams(Object param) {
        params.add(param);
    }

    void addParams(Object... params) {
        for (int i = 0; i < params.length; i++) {
            this.params.add(params[i]);
        }
    }

    public RequestType getRequestType() {
        return requestType;
    }

    void setRequestType(RequestType requestType) {
        this.requestType = requestType;
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

    public Peer getPeerFrom() {
        return peerFrom;
    }

    void setPeerFrom(Peer peerFrom) {
        this.peerFrom = peerFrom;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Override
    public String toString() {
        return " > Request [id=" + StringUtils.shortId(id) + ", method=" + method + ", params=" + params
                + ", requestType=" + requestType + ", peerFrom=" + peerFrom + "] ";
    }
}
