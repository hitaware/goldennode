package com.goldennode.api.goldennodecluster;

public enum LockTypes {
	APPLICATION("$application"), CLUSTERED_OBJECT_MANAGER("$clusteredObjectManager"), CLUSTERED_SERVER_MANAGER(
			"$clusteredServerManager"), HANDSHAKING("$handshaking");
	private String name;

	private LockTypes(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
