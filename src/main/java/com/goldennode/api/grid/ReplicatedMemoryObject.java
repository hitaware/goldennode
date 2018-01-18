package com.goldennode.api.grid;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.goldennode.api.helper.ReflectionUtils;

public class ReplicatedMemoryObject extends DistributedObject {
    private static final long serialVersionUID = 1L;
    public ReplicatedMemoryObject(String publicName) {
        super(publicName);
    }

    public ReplicatedMemoryObject() {
        super();
    }

}
