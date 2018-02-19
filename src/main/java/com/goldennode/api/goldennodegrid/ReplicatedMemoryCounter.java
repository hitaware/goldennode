package com.goldennode.api.goldennodegrid;

import java.io.Serializable;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.GridException;

public class ReplicatedMemoryCounter extends DistributedObject {
	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryCounter.class);
	private Counter counter = new Counter();

	class Counter implements Serializable {
		private static final long serialVersionUID = 1L;
		private int counter = 0;

		public synchronized int getCounter() {
			return counter;
		}

		public synchronized void incrementCounter() {
			counter++;
		}
	}

	public ReplicatedMemoryCounter() {
		super();
	}

	public ReplicatedMemoryCounter(String publicName) {
		super(publicName);
	}

	public int getcounter() throws GridException {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return counter.getCounter();
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return getcounter();
		}
	}

	public void inccounter() {
		if (getGrid() != null) {
			safeOperate(new Operation(getPublicName(), "inccounter"));
		} else {
			_inccounter();
		}
	}

	public void _inccounter() {
		counter.incrementCounter();
	}
}
