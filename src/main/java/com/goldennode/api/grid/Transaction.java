package com.goldennode.api.grid;

import java.util.Stack;

import com.goldennode.api.helper.ReflectionUtils;

public class Transaction {
    protected Stack<Operation> history = new Stack<Operation>();

    public void createUndoRecord(Operation operation) {
        history.push(operation);
    }

    public Stack<Operation> getHistory() {
        return history;
    }

    public void undo() {
        Operation operation = history.pop();
        if (operation != null) {
            try {
                ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
            } catch (Exception e) {
                throw new OperationException(e);
            }
        }
    }

    public void beginTransaction() {
        history.clear();
    }

    public void commitTransaction() {
        history.clear();
    }

    public void rollbackTransaction() {
        while (!history.isEmpty()) {
            undo();
        }
    }
}
