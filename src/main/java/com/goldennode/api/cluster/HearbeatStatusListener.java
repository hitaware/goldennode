package com.goldennode.api.cluster;

import com.goldennode.api.core.Server;

public interface HearbeatStatusListener {

	public void serverUnreachable(Server server);

}
