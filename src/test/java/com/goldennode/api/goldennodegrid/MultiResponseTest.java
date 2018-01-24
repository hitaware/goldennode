package com.goldennode.api.goldennodegrid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.core.MockPeerImpl;
import com.goldennode.api.core.Response;
import com.goldennode.api.goldennodegrid.MultiResponse;
import com.goldennode.api.goldennodegrid.NoResponseException;
import com.goldennode.api.goldennodegrid.NonUniqueResultException;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.core.Peer;
import com.goldennode.api.core.PeerException;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class MultiResponseTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MultiResponseTest.class);
    private MultiResponse mrSuccessful;
    private MultiResponse mrErrorneous;
    private MultiResponse mrUnsuccessful;
    private MultiResponse mrNoResponse;
    private MultiResponse mrErrorneous2;
    private MultiResponse mrErrorneous3;

    private Peer getGoldenNodePeer(String id) throws PeerException {
        return new MockPeerImpl(id);
    }

    private Response getUnErrorneousResponse(String text) {
        Response r = new Response();
        r.setReturnValue(text);
        return r;
    }

    private Response getRandomValueResponse() {
        Response r = new Response();
        r.setReturnValue(Math.random() * 5);
        return r;
    }

    @Before
    public void init() throws PeerException {
        // These responses have no exception(Unerrorneous) and they are the
        // same, which means successful!
        mrSuccessful = new MultiResponse();
        mrSuccessful.addSuccessfulResponse(getGoldenNodePeer("1"), getUnErrorneousResponse("control_text"));
        mrSuccessful.addSuccessfulResponse(getGoldenNodePeer("2"), getUnErrorneousResponse("control_text"));
        mrSuccessful.addSuccessfulResponse(getGoldenNodePeer("3"), getUnErrorneousResponse("control_text"));
        mrSuccessful.addSuccessfulResponse(getGoldenNodePeer("4"), getUnErrorneousResponse("control_text"));
        // These responses have no exception(Unerrorneous) but the responses
        // differ in value, which means unsuccessful
        mrUnsuccessful = new MultiResponse();
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodePeer("1"), getUnErrorneousResponse("control_text"));
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodePeer("2"), getRandomValueResponse());
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodePeer("3"), getRandomValueResponse());
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodePeer("4"), getRandomValueResponse());
        // These responses have some exceptions, which means unsuccessful
        mrErrorneous = new MultiResponse();
        mrErrorneous.addSuccessfulResponse(getGoldenNodePeer("1"), getUnErrorneousResponse("control_text"));
        mrErrorneous.addSuccessfulResponse(getGoldenNodePeer("2"), getUnErrorneousResponse("control_text"));
        mrErrorneous.addSuccessfulResponse(getGoldenNodePeer("3"), getUnErrorneousResponse("control_text"));
        mrErrorneous.addErroneusResponse(getGoldenNodePeer("4"), new GridException());
        mrErrorneous.addErroneusResponse(getGoldenNodePeer("5"), new GridException());
        mrErrorneous2 = new MultiResponse();
        mrErrorneous2.addSuccessfulResponse(getGoldenNodePeer("1"), getRandomValueResponse());
        mrErrorneous2.addSuccessfulResponse(getGoldenNodePeer("2"), getRandomValueResponse());
        mrErrorneous2.addSuccessfulResponse(getGoldenNodePeer("3"), getRandomValueResponse());
        mrErrorneous2.addErroneusResponse(getGoldenNodePeer("4"), new GridException());
        mrErrorneous2.addErroneusResponse(getGoldenNodePeer("5"), new GridException());
        mrErrorneous3 = new MultiResponse();
        mrErrorneous3.addErroneusResponse(getGoldenNodePeer("4"), new GridException());
        mrErrorneous3.addErroneusResponse(getGoldenNodePeer("5"), new GridException());
        mrErrorneous3.addSuccessfulResponse(getGoldenNodePeer("1"), getUnErrorneousResponse("control_text"));
        mrErrorneous3.addSuccessfulResponse(getGoldenNodePeer("2"), getUnErrorneousResponse("control_text"));
        mrErrorneous3.addSuccessfulResponse(getGoldenNodePeer("3"), getUnErrorneousResponse("control_text"));
        mrNoResponse = new MultiResponse();
    }

    @Test(expected = RuntimeException.class)
    @RepeatTest(times = 1)
    public void testAddSuccessfulResponse() throws PeerException {
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodePeer("1"), null);
    }

    @Test(expected = RuntimeException.class)
    @RepeatTest(times = 1)
    public void testAddErroneusResponse() throws PeerException {
        mrUnsuccessful.addErroneusResponse(getGoldenNodePeer("1"), null);
    }

    @Test
    @RepeatTest(times = 1)
    public void testIsSuccessfulCall() {
        Assert.assertEquals(true, mrSuccessful.isSuccessfulCall("control_text"));
        Assert.assertEquals(false, mrUnsuccessful.isSuccessfulCall("control_text"));
        Assert.assertEquals(false, mrErrorneous.isSuccessfulCall("control_text"));
        Assert.assertEquals(true, mrNoResponse.isSuccessfulCall("control_text"));
    }

    @Test
    @RepeatTest(times = 1)
    public void testGetUnErrorneousPeers() {
        Assert.assertEquals(4, mrSuccessful.getPeersWithNoError().size());
        Assert.assertEquals(4, mrUnsuccessful.getPeersWithNoError().size());
        Assert.assertEquals(3, mrErrorneous.getPeersWithNoError().size());
        Assert.assertEquals(0, mrNoResponse.getPeersWithNoError().size());
    }

    @Test
    @RepeatTest(times = 1)
    public void testGetSuccessfulPeers() {
        Assert.assertEquals(4, mrSuccessful.getPeersWithNoErrorAndExpectedResult("control_text").size());
        Assert.assertEquals(1, mrUnsuccessful.getPeersWithNoErrorAndExpectedResult("control_text").size());
        Assert.assertEquals(3, mrErrorneous.getPeersWithNoErrorAndExpectedResult("control_text").size());
        Assert.assertEquals(0, mrNoResponse.getPeersWithNoErrorAndExpectedResult("control_text").size());
    }

    @Test
    @RepeatTest(times = 1)
    public void testGetResponsesNoCheck() {
        Assert.assertEquals(4, mrSuccessful.getAllResponses().size());
        Assert.assertEquals(4, mrUnsuccessful.getAllResponses().size());
        Assert.assertEquals(5, mrErrorneous.getAllResponses().size());
        Assert.assertEquals(0, mrNoResponse.getAllResponses().size());
    }

    @Test
    @RepeatTest(times = 1)
    public void testGetResponseFromSinglePeer() throws GridException, PeerException {
        Assert.assertEquals("control_text",
                mrSuccessful.getResponseFromSinglePeer(getGoldenNodePeer("1")).getReturnValue());
        Assert.assertEquals("control_text",
                mrErrorneous.getResponseFromSinglePeer(getGoldenNodePeer("1")).getReturnValue());
    }

    @Test
    @RepeatTest(times = 1)
    public void testGetResponseFromSinglePeer2() throws GridException, PeerException {
        Assert.assertNull(mrErrorneous.getResponseFromSinglePeer(getGoldenNodePeer("4")));
    }

    @Test(expected = NoResponseException.class)
    @RepeatTest(times = 1)
    public void testGetResponseFromSinglePeer3() throws GridException, PeerException {
        mrErrorneous.getResponseFromSinglePeer(getGoldenNodePeer("not_available_peer")).getReturnValue();
    }

    @Test(expected = NoResponseException.class)
    @RepeatTest(times = 1)
    public void testGetResponseFromSinglePeer4() throws GridException, PeerException {
        mrNoResponse.getResponseFromSinglePeer(getGoldenNodePeer("not_available_peer")).getReturnValue();
    }

    @Test
    @RepeatTest(times = 1)
    public void testGetResponseAssertAllResponsesSameAndSuccessful() throws GridException {
        Assert.assertEquals("control_text",
                mrSuccessful.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue());
    }

    @Test(expected = NonUniqueResultException.class)
    @RepeatTest(times = 1)
    public void testGetResponseAssertAllResponsesSameAndSuccessful2() throws GridException {
        mrUnsuccessful.getResponseAssertAllResponsesSameAndSuccessful();
    }

    @Test(expected = GridException.class)
    @RepeatTest(times = 1)
    public void testGetResponseAssertAllResponsesSameAndSuccessful3() throws GridException {
        mrErrorneous.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }

    @Test(expected = NonUniqueResultException.class)
    @RepeatTest(times = 1)
    public void testGetResponseAssertAllResponsesSameAndSuccessful4() throws GridException {
        mrErrorneous2.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }

    @Test(expected = GridException.class)
    @RepeatTest(times = 1)
    public void testGetResponseAssertAllResponsesSameAndSuccessful5() throws GridException {
        mrErrorneous3.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }

    @Test(expected = NoResponseException.class)
    @RepeatTest(times = 1)
    public void testGetResponseAssertAllResponsesSameAndSuccessful6() throws GridException {
        mrNoResponse.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }
}
