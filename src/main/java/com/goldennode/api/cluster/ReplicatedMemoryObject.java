package com.goldennode.api.cluster;

public class ReplicatedMemoryObject extends ClusteredObject {
    private static final long serialVersionUID = 1L;

    public ReplicatedMemoryObject(String publicName) {
        super(publicName);
    }

    public ReplicatedMemoryObject() {
        super();
    }
}
