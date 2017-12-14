package com.goldennode.api.core;

public interface ServerStateListener {
    void serverStarted(Server server);

    void serverStopping(Server server);

    void serverStopped(Server server);

    void serverStarting(Server server);
}
