package com.goldennode.api.goldennodegrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.GridException;

public class ReplicatedMemoryList<E> extends DistributedObject implements List<E> {
	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryList.class);
	protected List<E> innerList = Collections.synchronizedList(new ArrayList<E>());

	public ReplicatedMemoryList() {
		super();
	}

	public ReplicatedMemoryList(String publicName) {
		super(publicName);
	}

	@Override
	public boolean add(E e) {
		if (getGrid() != null)
			return (boolean) safeOperate(new Operation(getPublicName(), "add", e));
		else
			return _add(e);
	}

	public boolean _add(E e) {
		return innerList.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E element) {
		if (getGrid() != null)
			return (E) safeOperate(new Operation(getPublicName(), "set", index, element));
		else
			return _set(index, element);
	}

	public E _set(int index, E element) {
		E e = innerList.set(index, element);
		return e;
	}

	@Override
	public void add(int index, E element) {
		if (getGrid() != null)
			safeOperate(new Operation(getPublicName(), "add", index, element));
		else
			_add(index, element);
	}

	public void _add(int index, E element) {
		innerList.add(index, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index) {
		if (getGrid() != null)
			return (E) safeOperate(new Operation(getPublicName(), "remove", index));
		else
			return _remove(index);
	}

	public E _remove(int index) {
		E e = innerList.remove(index);
		return e;
	}

	@Override
	public void clear() {
		if (getGrid() != null)
			safeOperate(new Operation(getPublicName(), "clear"));
		else
			_clear();
	}

	public void _clear() {
		innerList.clear();
	}

	@Override
	public boolean remove(Object o) {
		if (getGrid() != null)
			return (boolean) safeOperate(new Operation(getPublicName(), "remove", o));
		else
			return _remove(o);
	}

	public boolean _remove(Object o) {
		return innerList.remove(o);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	// Methods below don't modify list.
	@Override
	public int size() {

		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.size();
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
			return innerList.size();
		}
	}

	@Override
	public boolean isEmpty() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.isEmpty();
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
			return innerList.isEmpty();
		}
	}

	@Override
	public boolean contains(Object o) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.contains(o);
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
			return innerList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.iterator();
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
			return innerList.iterator();
	}

	@Override
	public Object[] toArray() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.toArray();
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
			return innerList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.toArray(a);
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
			return innerList.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.containsAll(c);
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
			return innerList.containsAll(c);
	}

	@Override
	public E get(int index) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.get(index);
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
			return innerList.get(index);
	}

	@Override
	public int indexOf(Object o) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.indexOf(o);
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
			return innerList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.lastIndexOf(o);
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
			return innerList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.listIterator();
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
			return innerList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.listIterator(index);
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
			return innerList.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerList.subList(fromIndex, toIndex);
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
			return innerList.subList(fromIndex, toIndex);
	}
}