package com.goldennode.api.cluster;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.goldennode.api.core.Server;
import com.goldennode.api.helper.SystemUtils;

public class HeartbeatTimer {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HeartbeatTimer.class);
	private Timer timer;
	private Cluster cluster;
	private static final int TASK_DELAY = Integer.parseInt(SystemUtils.getSystemProperty("1000",
			"com.goldennode.api.cluster.HeartbeatTimer.taskDelay"));
	private static final int TASK_PERIOD = Integer.parseInt(SystemUtils.getSystemProperty("2000",
			"com.goldennode.api.cluster.HeartbeatTimer.taskPeriod"));

	public HeartbeatTimer(Cluster cluster) {
		this.cluster = cluster;
	}

	public void start() {
		timer = new Timer();
	}

	public void stop() {
		timer.cancel();
	}

	public void schedule(final Server server, final HearbeatStatusListener listener) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					cluster.unicastTCP(server, new Operation(null, "ping", cluster.getOwner().getId()));
				} catch (ClusterException e) {
					LOGGER.error("Can't ping peer: " + server);
					cancel();
					listener.serverUnreachable(server);
				}
			}
		}, TASK_DELAY, TASK_PERIOD);
	}
}
