package com.goldennode.api.cluster;

import java.io.Serializable;
import java.util.Stack;

import com.goldennode.api.helper.ReflectionUtils;

public class UndoableObject implements Serializable {

	private static final long serialVersionUID = 1L;
	protected Stack<Operation> history = new Stack<Operation>();
	protected int version = 1;

	public void createUndoRecord(Operation operation) {
		version++;
		history.push(operation);
	}

	public Stack<Operation> getHistory() {
		return history;
	}

	public Integer _getVersion() {
		return version;
	}

	public int getVersion() {
		return version;
	}

	public void undo(int currentVersion) {
		if (getVersion() != currentVersion) {
			throw new OperationException(currentVersion + " is not the latest version. Objects version is " + version);
		}
		if (getVersion() == 1) {
			throw new OperationException("Object in initial state");
		}
		doUndo();
	}

	private void doUndo() {

		Operation operation = history.pop();
		if (operation != null) {
			try {
				ReflectionUtils.callMethod(this, operation.getObjectMethod(), operation.getParams());
				version--;
			} catch (Exception e) {
				throw new OperationException(e);
			}
		}

	}

	/*
	 * public void beginTransaction() { history.clear(); }
	 * 
	 * public void commitTransaction() { history.clear(); }
	 * 
	 * public void rollbackTransaction() throws ObjectOperationException { while
	 * (!history.isEmpty()) { undo(); } }
	 */

}
