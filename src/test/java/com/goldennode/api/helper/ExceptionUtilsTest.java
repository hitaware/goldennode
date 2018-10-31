package com.goldennode.api.helper;

import java.net.SocketException;

import org.junit.Test;

import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class ExceptionUtilsTest extends GoldenNodeJunitRunner {
    @Test(expected = RuntimeException.class)
    @RepeatTest(times = 1)
    public void throwCauseIfThereIs() throws Throwable {

        Exception e = new Exception();

        RuntimeException r = new RuntimeException("R1");
        r.initCause(new RuntimeException("R2"));

        e.initCause(r);

        ExceptionUtils.throwCauseIfThereIs(e, RuntimeException.class);

    }

    @Test()
    @RepeatTest(times = 1)
    public void throwCauseIfThereIs2() throws Throwable {
        Exception e = new Exception();

        RuntimeException r = new RuntimeException("R1");
        r.initCause(new RuntimeException("R2"));

        e.initCause(r);

        ExceptionUtils.throwCauseIfThereIs(e, SocketException.class);

    }

}
