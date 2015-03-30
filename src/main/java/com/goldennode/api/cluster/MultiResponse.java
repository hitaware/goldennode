package com.goldennode.api.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Response;
import com.goldennode.api.core.Server;

public class MultiResponse {

	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MultiResponse.class);

	private Map<Server, Object> responses = new LinkedHashMap<Server, Object>();

	public int size() {
		return responses.size();
	}

	public void addSuccessfulResponse(Server server, Response response) {
		if (server == null || response == null) {
			throw new InvalidResponseException();
		}
		responses.put(server, response);
	}

	public void addErroneusResponse(Server server, ClusterException exception) {
		if (server == null || exception == null) {
			throw new InvalidResponseException();
		}
		responses.put(server, exception);
	}

	public Response getResponseFromSingleServer(Server server) throws ClusterException {
		Object o = responses.get(server);
		if (o == null) {
			throw new NoResponseException();
		} else if (o instanceof ClusterException) {
			throw (ClusterException) o;
		}
		return (Response) o;
	}

	public Map<Server, Response> getResponsesNoCheck() {
		Map<Server, Response> tmpResponses = new LinkedHashMap<Server, Response>();

		for (Entry<Server, Object> entry : responses.entrySet()) {
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
			}

			else if (r.getReturnValue().equals(expectedResult)) {
				LOGGER.debug("Successful response > " + r.getReturnValue());
				return true;
			}
			LOGGER.debug("Unsuccessful expected > " + expectedResult + " actual > " + r.getReturnValue());

			return false;
		} catch (ClusterException e) {
			LOGGER.error("Unsuccessful response > " + e);
			return false;
		}

	}

	public Response getResponseAssertAllResponsesSameAndSuccessful() throws ClusterException {
		if (responses.size() == 0) {
			throw new NoResponseException();
		}
		int i = 0;
		Object firstValue = null;
		Object previousValue = null;
		for (Entry<Server, Object> entry : responses.entrySet()) {
			i++;
			if (entry.getValue() instanceof ClusterException) {
				LOGGER.debug("Response error > " + entry.getValue());
				throw (ClusterException) entry.getValue();
			}
			if (i > 1) {
				if (previousValue == null && entry.getValue() == null) {
					LOGGER.debug("Previous value is null and current value is null");
				} else if (previousValue == null && entry.getValue() != null) {
					throw new NonUniqueResultException("Previous > null. Current > " + entry.getValue());
				} else if (!previousValue.equals(((Response) entry.getValue()).getReturnValue())) {
					throw new NonUniqueResultException("Previous > " + previousValue + ". Current > "
							+ entry.getValue());
				}
			}
			if (i == 1) {
				previousValue = ((Response) entry.getValue()).getReturnValue();
				firstValue = entry.getValue();
			}
		}
		return (Response) firstValue;

	}

	public Collection<Server> getSuccessfulServers(Object expectedResult) {
		List<Server> list = new ArrayList<Server>();
		for (Entry<Server, Object> entry : responses.entrySet()) {
			if (entry.getValue() instanceof Response) {
				Response r = (Response) entry.getValue();
				if (r.getReturnValue() == null && expectedResult == null) {
					LOGGER.debug("Value is null and expected value is null");
					list.add(entry.getKey());
				} else if (r.getReturnValue() == null && expectedResult != null) {
					LOGGER.debug("Don't do anything");
					// Don't do anything
				}
				if (r.getReturnValue().equals(expectedResult)) {
					LOGGER.debug("Value equals to expected value");
					list.add(entry.getKey());
				}
			}
		}
		return list;
	}

	public Collection<Server> getUnErrorneousServers() {
		List<Server> list = new ArrayList<Server>();
		for (Entry<Server, Object> entry : responses.entrySet()) {
			if (entry.getValue() instanceof Response) {
				list.add(entry.getKey());
			}
		}
		return list;
	}
}
