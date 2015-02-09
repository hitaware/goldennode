package com.goldennode.api.cluster;

import java.util.Stack;

import com.goldennode.api.core.ReflectionUtils;

public class UndoableObject {
	protected transient Stack<Operation> history = new Stack<Operation>();
	protected transient int version = 1;

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

	public void undoLatest(int lastestVersion) {

		if (version != lastestVersion) {
			throw new OperationException(lastestVersion
					+ " is not the latest version. Objects version is "
					+ version);
		}
		if (version == 1) {
			throw new OperationException("Object in initial state");
		}
		Operation operation = history.pop();
		if (operation != null) {
			try {
				ReflectionUtils.callMethod(this, operation.getObjectMethod(),
						operation.getParams());
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
