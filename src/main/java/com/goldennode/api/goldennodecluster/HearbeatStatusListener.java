package com.goldennode.api.goldennodecluster;

import com.goldennode.api.core.Server;

public interface HearbeatStatusListener {

	public void serverUnreachable(Server server);

}
