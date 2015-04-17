package com.goldennode.api.goldennodecluster;

import com.goldennode.api.core.Server;

public interface HearbeatStatusListener {
	void serverUnreachable(Server server);
}
