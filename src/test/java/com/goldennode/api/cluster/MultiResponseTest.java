package com.goldennode.api.cluster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.goldennode.api.core.MockGoldenNodeServer;
import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;
import com.goldennode.api.core.ServerException;
import com.goldennode.testutils.GoldenNodeJunitRunner;

public class MultiResponseTest extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MultiResponseTest.class);
    private MultiResponse mrSuccessful;
    private MultiResponse mrErrorneous;
    private MultiResponse mrUnsuccessful;
    private MultiResponse mrNoResponse;
    private MultiResponse mrErrorneous2;
    private MultiResponse mrErrorneous3;

    private Server getGoldenNodeServer(String id) throws ServerException {
        return new MockGoldenNodeServer(id);
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
    public void init() throws ServerException {
        // These responses have no exception(Unerrorneous) and they are the
        // same, which means successful!
        mrSuccessful = new MultiResponse();
        mrSuccessful.addSuccessfulResponse(getGoldenNodeServer("1"), getUnErrorneousResponse("control_text"));
        mrSuccessful.addSuccessfulResponse(getGoldenNodeServer("2"), getUnErrorneousResponse("control_text"));
        mrSuccessful.addSuccessfulResponse(getGoldenNodeServer("3"), getUnErrorneousResponse("control_text"));
        mrSuccessful.addSuccessfulResponse(getGoldenNodeServer("4"), getUnErrorneousResponse("control_text"));
        // These responses have no exception(Unerrorneous) but the responses
        // differ in value, which means unsuccessful
        mrUnsuccessful = new MultiResponse();
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodeServer("1"), getUnErrorneousResponse("control_text"));
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodeServer("2"), getRandomValueResponse());
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodeServer("3"), getRandomValueResponse());
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodeServer("4"), getRandomValueResponse());
        // These responses have some exceptions, which means unsuccessful
        mrErrorneous = new MultiResponse();
        mrErrorneous.addSuccessfulResponse(getGoldenNodeServer("1"), getUnErrorneousResponse("control_text"));
        mrErrorneous.addSuccessfulResponse(getGoldenNodeServer("2"), getUnErrorneousResponse("control_text"));
        mrErrorneous.addSuccessfulResponse(getGoldenNodeServer("3"), getUnErrorneousResponse("control_text"));
        mrErrorneous.addErroneusResponse(getGoldenNodeServer("4"), new ClusterException());
        mrErrorneous.addErroneusResponse(getGoldenNodeServer("5"), new ClusterException());
        mrErrorneous2 = new MultiResponse();
        mrErrorneous2.addSuccessfulResponse(getGoldenNodeServer("1"), getRandomValueResponse());
        mrErrorneous2.addSuccessfulResponse(getGoldenNodeServer("2"), getRandomValueResponse());
        mrErrorneous2.addSuccessfulResponse(getGoldenNodeServer("3"), getRandomValueResponse());
        mrErrorneous2.addErroneusResponse(getGoldenNodeServer("4"), new ClusterException());
        mrErrorneous2.addErroneusResponse(getGoldenNodeServer("5"), new ClusterException());
        mrErrorneous3 = new MultiResponse();
        mrErrorneous3.addErroneusResponse(getGoldenNodeServer("4"), new ClusterException());
        mrErrorneous3.addErroneusResponse(getGoldenNodeServer("5"), new ClusterException());
        mrErrorneous3.addSuccessfulResponse(getGoldenNodeServer("1"), getUnErrorneousResponse("control_text"));
        mrErrorneous3.addSuccessfulResponse(getGoldenNodeServer("2"), getUnErrorneousResponse("control_text"));
        mrErrorneous3.addSuccessfulResponse(getGoldenNodeServer("3"), getUnErrorneousResponse("control_text"));
        mrNoResponse = new MultiResponse();
    }

    @Test(expected = RuntimeException.class)
    public void testAddSuccessfulResponse() throws ServerException {
        mrUnsuccessful.addSuccessfulResponse(getGoldenNodeServer("1"), null);
    }

    @Test(expected = RuntimeException.class)
    public void testAddErroneusResponse() throws ServerException {
        mrUnsuccessful.addErroneusResponse(getGoldenNodeServer("1"), null);
    }

    @Test
    public void testIsSuccessfulCall() {
        Assert.assertEquals(true, mrSuccessful.isSuccessfulCall("control_text"));
        Assert.assertEquals(false, mrUnsuccessful.isSuccessfulCall("control_text"));
        Assert.assertEquals(false, mrErrorneous.isSuccessfulCall("control_text"));
        Assert.assertEquals(true, mrNoResponse.isSuccessfulCall("control_text"));
    }

    @Test
    public void testGetUnErrorneousServers() {
        Assert.assertEquals(4, mrSuccessful.getServersWithNoError().size());
        Assert.assertEquals(4, mrUnsuccessful.getServersWithNoError().size());
        Assert.assertEquals(3, mrErrorneous.getServersWithNoError().size());
        Assert.assertEquals(0, mrNoResponse.getServersWithNoError().size());
    }

    @Test
    public void testGetSuccessfulServers() {
        Assert.assertEquals(4, mrSuccessful.getServersWithNoErrorAndExpectedResult("control_text").size());
        Assert.assertEquals(1, mrUnsuccessful.getServersWithNoErrorAndExpectedResult("control_text").size());
        Assert.assertEquals(3, mrErrorneous.getServersWithNoErrorAndExpectedResult("control_text").size());
        Assert.assertEquals(0, mrNoResponse.getServersWithNoErrorAndExpectedResult("control_text").size());
    }

    @Test
    public void testGetResponsesNoCheck() {
        Assert.assertEquals(4, mrSuccessful.getAllResponses().size());
        Assert.assertEquals(4, mrUnsuccessful.getAllResponses().size());
        Assert.assertEquals(5, mrErrorneous.getAllResponses().size());
        Assert.assertEquals(0, mrNoResponse.getAllResponses().size());
    }

    @Test
    public void testGetResponseFromSingleServer() throws ClusterException, ServerException {
        Assert.assertEquals("control_text",
                mrSuccessful.getResponseFromSingleServer(getGoldenNodeServer("1")).getReturnValue());
        Assert.assertEquals("control_text",
                mrErrorneous.getResponseFromSingleServer(getGoldenNodeServer("1")).getReturnValue());
    }

    @Test
    public void testGetResponseFromSingleServer2() throws ClusterException, ServerException {
        Assert.assertNull(mrErrorneous.getResponseFromSingleServer(getGoldenNodeServer("4")));
    }

    @Test(expected = NoResponseException.class)
    public void testGetResponseFromSingleServer3() throws ClusterException, ServerException {
        mrErrorneous.getResponseFromSingleServer(getGoldenNodeServer("not_available_server")).getReturnValue();
    }

    @Test(expected = NoResponseException.class)
    public void testGetResponseFromSingleServer4() throws ClusterException, ServerException {
        mrNoResponse.getResponseFromSingleServer(getGoldenNodeServer("not_available_server")).getReturnValue();
    }

    @Test
    public void testGetResponseAssertAllResponsesSameAndSuccessful() throws ClusterException {
        Assert.assertEquals("control_text",
                mrSuccessful.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue());
    }

    @Test(expected = NonUniqueResultException.class)
    public void testGetResponseAssertAllResponsesSameAndSuccessful2() throws ClusterException {
        mrUnsuccessful.getResponseAssertAllResponsesSameAndSuccessful();
    }

    @Test(expected = ClusterException.class)
    public void testGetResponseAssertAllResponsesSameAndSuccessful3() throws ClusterException {
        mrErrorneous.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }

    @Test(expected = NonUniqueResultException.class)
    public void testGetResponseAssertAllResponsesSameAndSuccessful4() throws ClusterException {
        mrErrorneous2.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }

    @Test(expected = ClusterException.class)
    public void testGetResponseAssertAllResponsesSameAndSuccessful5() throws ClusterException {
        mrErrorneous3.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }

    @Test(expected = NoResponseException.class)
    public void testGetResponseAssertAllResponsesSameAndSuccessful6() throws ClusterException {
        mrNoResponse.getResponseAssertAllResponsesSameAndSuccessful().getReturnValue();
    }
}
