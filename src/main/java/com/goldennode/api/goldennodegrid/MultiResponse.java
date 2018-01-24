package com.goldennode.api.goldennodegrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Response;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.core.Peer;

public class MultiResponse {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MultiResponse.class);
    private Map<Peer, Object> responses = new LinkedHashMap<Peer, Object>();

    public int size() {
        return responses.size();
    }

    public void addSuccessfulResponse(Peer peer, Response response) {
        if (peer == null || response == null) {
            throw new RuntimeException("Parameter can not be null");
        }
        responses.put(peer, response);
    }

    public void addErroneusResponse(Peer peer, GridException exception) {
        if (peer == null || exception == null) {
            throw new RuntimeException("Parameter can not be null");
        }
        responses.put(peer, exception);
    }

    public Response getResponseFromSinglePeer(Peer peer) throws NoResponseException {
        Object o = responses.get(peer);
        if (o == null) {
            throw new NoResponseException();
        } else if (o instanceof GridException) {
            return null;
        }
        return (Response) o;
    }

    public Map<Peer, Response> getAllResponses() {
        Map<Peer, Response> tmpResponses = new LinkedHashMap<Peer, Response>();
        for (Entry<Peer, Object> entry : responses.entrySet()) {
            if (entry.getValue() instanceof Response) {
                tmpResponses.put(entry.getKey(), (Response) entry.getValue());
            } else {
                tmpResponses.put(entry.getKey(), null);
            }
        }
        return tmpResponses;
    }

    public boolean isSuccessfulCall(Object expectedResult) {
        try {
            Response r = getResponseAssertAllResponsesSameAndSuccessful();
            if (r.getReturnValue() == null && expectedResult == null) {
                LOGGER.debug("Successful response > " + null);
                return true;
            } else if (r.getReturnValue() == null && expectedResult != null) {
                LOGGER.debug("Unsuccessful expected > " + expectedResult + " actual > " + r.getReturnValue());
                return false;
            } else if (r.getReturnValue().equals(expectedResult)) {
                LOGGER.debug("Successful response > " + r.getReturnValue());
                return true;
            }
            LOGGER.debug("Unsuccessful expected > " + expectedResult + " actual > " + r.getReturnValue());
            return false;
        } catch (NoResponseException e) {
            LOGGER.debug("No responses");
            return true;
        } catch (GridException e) {
            LOGGER.error("Unsuccessful response > " + e);
            return false;
        }
    }

    public Response getResponseAssertAllResponsesSameAndSuccessful() throws GridException {
        if (responses.size() == 0) {
            throw new NoResponseException();
        }
        int i = 0;
        Response firstValue = null;
        Object previousValue = null;
        for (Entry<Peer, Object> entry : responses.entrySet()) {
            i++;
            if (entry.getValue() instanceof GridException) {
                LOGGER.debug("Response error > " + entry.getValue());
                throw (GridException) entry.getValue();
            }
            if (i > 1) {
                if (previousValue == null && ((Response) entry.getValue()).getReturnValue() == null) {// NOPMD
                    // Don't do anything
                } else if (previousValue == null && ((Response) entry.getValue()).getReturnValue() != null) {
                    throw new NonUniqueResultException(
                            "Previous > null. Current > " + ((Response) entry.getValue()).getReturnValue());
                } else if (!previousValue.equals(((Response) entry.getValue()).getReturnValue())) {
                    throw new NonUniqueResultException("Previous > " + previousValue + ". Current > "
                            + ((Response) entry.getValue()).getReturnValue());
                }
            }
            if (i == 1) {
                previousValue = ((Response) entry.getValue()).getReturnValue();
                firstValue = (Response) entry.getValue();
            }
        }
        return firstValue;
    }

    public Collection<Peer> getPeersWithNoErrorAndExpectedResult(Object expectedResult) {
        List<Peer> list = new ArrayList<Peer>();
        for (Entry<Peer, Object> entry : responses.entrySet()) {
            if (entry.getValue() instanceof Response) {
                Response r = (Response) entry.getValue();
                if (r.getReturnValue() == null && expectedResult == null) {
                    list.add(entry.getKey());
                } else if (r.getReturnValue() == null && expectedResult != null) {// NOPMD
                    // Don't do anything
                }
                if (r.getReturnValue().equals(expectedResult)) {
                    list.add(entry.getKey());
                }
            }
        }
        return list;
    }

    public Collection<Peer> getPeersWithNoError() {
        List<Peer> list = new ArrayList<Peer>();
        for (Entry<Peer, Object> entry : responses.entrySet()) {
            if (entry.getValue() instanceof Response) {
                list.add(entry.getKey());
            }
        }
        return list;
    }
}
