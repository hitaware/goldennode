package com.goldennode.api.goldennodegrid;

public enum LockTypes {
    APPLICATION("$application"), DISTRUBUTED_OBJECT_MANAGER("$distributedObjectManager"), PEER_MANAGER(
            "$peerManager");
    private String name;

    private LockTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
