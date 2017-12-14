package com.goldennode.api.cluster;

import com.goldennode.api.core.OperationBase;

public abstract class ClusterOperationBase implements OperationBase {
    public abstract Object _op_(Operation operation) throws OperationException;
}
