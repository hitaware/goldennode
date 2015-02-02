package com.goldennode.api.core;

public interface ServerStateListener {
	public abstract void serverStarted(Server server);

	public abstract void serverStopping(Server server);

	public abstract void serverStopped(Server server);

	public abstract void serverStarting(Server server);
}
