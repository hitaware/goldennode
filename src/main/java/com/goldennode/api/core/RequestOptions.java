package com.goldennode.api.core;

import com.goldennode.api.helper.SystemUtils;

public class RequestOptions {
    private static final int REQUEST_TIMEOUT = Integer
            .parseInt(SystemUtils.getSystemProperty("60000", "com.goldennode.api.core.RequestOptions.requestTimeout"));
    private int timeout = RequestOptions.REQUEST_TIMEOUT;

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
