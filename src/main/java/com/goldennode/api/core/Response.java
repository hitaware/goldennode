package com.goldennode.api.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private Object returnValue;
    private Exception exception;
    private Peer peerFrom;
    private Request request;

    public Response() {//
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Peer getPeerFrom() {
        return peerFrom;
    }

    public void setPeerFrom(Peer peerFrom) {
        this.peerFrom = peerFrom;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        return " > Response [returnValue=" + returnValue + ", exception=" + exception + ", peerFrom=" + peerFrom
                + "] ";
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
