package com.goldennode.api.goldennodegrid;

import com.goldennode.api.core.OperationBase;

public abstract class GridOperationBase implements OperationBase {
    public abstract Object _op_(Operation operation) throws OperationException;
}
