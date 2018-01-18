package com.goldennode.api.grid;

import com.goldennode.api.core.OperationBase;

public abstract class GridOperationBase implements OperationBase {
    public abstract Object _op_(Operation operation) throws OperationException;
}
