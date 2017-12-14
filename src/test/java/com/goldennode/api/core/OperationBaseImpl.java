package com.goldennode.api.core;

import org.junit.Assert;
import org.slf4j.LoggerFactory;

public class OperationBaseImpl implements OperationBase, ServerStateListener {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OperationBaseImpl.class);
    private int getSumCalled = 0;
    private int getSumExceptionCalled = 0;
    private int echoCalled = 0;

    public int getGetSumCalled() {
        return getSumCalled;
    }

    public int getGetSumExceptionCalled() {
        return getSumExceptionCalled;
    }

    public int getEchoCalled() {
        return echoCalled;
    }

    public Integer _getSum(Integer param1, Integer param2) {
        getSumCalled++;
        LOGGER.debug("getSum(" + param1 + "," + param2 + ")");
        return new Integer(param1.intValue() + param2.intValue());
    }

    public Integer _getSumException(Integer param1, Integer param2) throws Exception {
        getSumExceptionCalled++;
        throw new RuntimeException("sum exception");
    }

    public void _echo(String param) {
        echoCalled++;
        LOGGER.debug("echo " + param);
    }

    public String _nullParamTest(String param) {
        Assert.assertNull(param);
        LOGGER.debug("Param is null? param = " + null);
        return "x";
    }

    @Override
    public void serverStarted(Server server) {
        LOGGER.debug("Server started." + server.toString());
    }

    @Override
    public void serverStopping(Server server) {
        LOGGER.debug("Server stopped." + server.toString());
    }

    @Override
    public void serverStopped(Server server) {
        //
    }

    @Override
    public void serverStarting(Server server) {
        //
    }
}