package com.goldennode.api.core;

import java.util.Map;

import org.junit.Assert;
import org.slf4j.LoggerFactory;

public class OperationBaseImpl implements OperationBase, PeerStateListener {
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

    public int _putMap(Map map) {
        LOGGER.debug("map.size() = " + map.size());
        return map.size();
    }
    
    public String _nullParamTest(String param) {
        Assert.assertNull(param);
        LOGGER.debug("Param is null? param = " + null);
        return "x";
    }

    @Override
    public void peerStarted(Peer peer) {
        LOGGER.debug("Peer started." + peer.toString());
    }

    @Override
    public void peerStopping(Peer peer) {
        LOGGER.debug("Peer stopped." + peer.toString());
    }

    @Override
    public void peerStopped(Peer peer) {
        //
    }

    @Override
    public void peerStarting(Peer peer) {
        //
    }
}