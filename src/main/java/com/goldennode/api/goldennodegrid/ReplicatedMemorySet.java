package com.goldennode.api.goldennodegrid;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.GridException;

public class ReplicatedMemorySet<E> extends DistributedObject implements Set<E> {
	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemorySet.class);
	protected Set<E> innerSet = Collections.synchronizedSet(new HashSet<E>());

	public ReplicatedMemorySet() {
		super();
	}

	public ReplicatedMemorySet(String publicName) {
		super(publicName);
	}

	@Override
	public boolean add(E e) {
		if (getGrid() != null) {
			return (boolean) safeOperate(new Operation(getPublicName(), "add", e));
		} else {
			return _add(e);
		}
	}

	public boolean _add(E e) {
		return innerSet.add(e);
	}

	@Override

	public boolean remove(Object o) {
		if (getGrid() != null) {
			return (boolean) safeOperate(new Operation(getPublicName(), "remove", o));
		} else {
			return _remove(o);
		}
	}

	public boolean _remove(Object o) {
		return innerSet.remove(o);
	}

	@Override
	public void clear() {
		if (getGrid() != null) {
			safeOperate(new Operation(getPublicName(), "clear"));
		} else {
			_clear();
		}
	}

	public void _clear() {
		innerSet.clear();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.size();
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
			return innerSet.size();
		}
	}

	@Override
	public boolean isEmpty() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.isEmpty();
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
			return innerSet.isEmpty();
		}
	}

	@Override
	public boolean contains(Object o) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.contains(o);
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
			return innerSet.contains(o);
		}
	}

	@Override
	public Iterator<E> iterator() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.iterator();
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
			return innerSet.iterator();
		}
	}

	@Override
	public Object[] toArray() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.toArray();
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
			return innerSet.toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.toArray(a);
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

		} else

		{
			return toArray(a);
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerSet.containsAll(c);
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
		}

		else

		{
			return innerSet.containsAll(c);
		}
	}
}
