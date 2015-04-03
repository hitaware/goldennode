package com.goldennode.api.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.LoggerFactory;

public class ClusteredList<E> extends ClusteredObject implements List<E>, Serializable {
	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClusteredList.class);
	protected List<E> innerList = Collections.synchronizedList(new ArrayList<E>());
	private int counter = 0;

	public ClusteredList() {
		super();
	}

	public int getcounter() {
		return counter;
	}

	public int inccounter() {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			return (int) getCluster().safeMulticast(new Operation(getPublicName(), "inccounter"));
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
	}

	public int _inccounter() {
		counter = counter + 1;
		return counter;
	}

	public ClusteredList(String publicName, String ownerId, Cluster cluster) {
		super(publicName, ownerId, cluster);
	}

	@Override
	public int size() {
		return innerList.size();
	}

	@Override
	public boolean isEmpty() {
		return innerList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return innerList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return innerList.iterator();
	}

	@Override
	public Object[] toArray() {
		return innerList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return innerList.toArray(a);
	}

	// TODO Integer Object conflict
	@Override
	public boolean add(E e) {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			return (Boolean) getCluster().safeMulticast(new Operation(getPublicName(), "add", e));
		} catch (ClusterException e1) {
			throw new RuntimeException(e1);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
	}

	public Boolean _add(E e) {
		Boolean b = _base_add(e);
		// TODO getCluster().getProxy().createUndoRecord(
		// new Operation(getPublicName(), "base_remove", e));
		return b;
	}

	public Boolean _base_add(E e) {
		return innerList.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E element) {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			return (E) getCluster().safeMulticast(new Operation(getPublicName(), "set", index, element));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public E _set(int index, E element) {
		E e = _base_set(index, element);
		// TODO getCluster().getProxy().createUndoRecord(new
		// Operation(getPublicName(), "base_set", index, e));
		return e;
	}

	public E _base_set(int index, E element) {
		E e = innerList.set(index, element);
		return e;
	}

	@Override
	public void add(int index, E element) {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			getCluster().safeMulticast(new Operation(getPublicName(), "add", index, element));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void _add(int index, E element) {
		_base_add(index, element);
		// TODO getCluster().getProxy().createUndoRecord(new
		// Operation(getPublicName(), "base_remove", index));
	}

	public void _base_add(int index, E element) {
		innerList.add(index, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index) {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			return (E) getCluster().safeMulticast(new Operation(getPublicName(), "remove", index));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public E _remove(int index) {
		E e = _base_remove(index);
		// TODO getCluster().getProxy().createUndoRecord(new
		// Operation(getPublicName(), "base_add", index, e));
		return e;
	}

	public E _base_remove(int index) {
		E e = innerList.remove(index);
		return e;
	}

	@Override
	public void clear() {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			getCluster().safeMulticast(new Operation(getPublicName(), "clear"));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void _clear() {
		// TODO getCluster().getProxy().createUndoRecord(
		// new Operation(getPublicName(), "base_addAll", new
		// ArrayList<E>(innerList)));
		_base_clear();
	}

	public void _base_clear() {
		innerList.clear();
	}

	@Override
	public boolean remove(Object o) {
		boolean locked = false;
		try {
			getCluster().lock(this);
			locked = true;
			return (boolean) getCluster().safeMulticast(new Operation(getPublicName(), "remove", o));
		} catch (ClusterException e) {
			throw new RuntimeException(e);
		} finally {
			if (locked) {
				try {
					getCluster().unlock(this);
				} catch (ClusterException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public boolean _remove(Object o) {
		Boolean b = _base_remove(o);
		// TODOgetCluster().getProxy().createUndoRecord(new
		// Operation(getPublicName(), "base_add", o));
		return b;
	}

	public boolean _base_remove(Object o) {
		return innerList.remove(o);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	public void _base_addAll(Collection<? extends E> c) {
		try {
			innerList.addAll(c);
		} finally {
		}
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
	public boolean containsAll(Collection<?> c) {
		return innerList.containsAll(c);
	}

	@Override
	public E get(int index) {
		return innerList.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return innerList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return innerList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return innerList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return innerList.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return innerList.subList(fromIndex, toIndex);
	}
}
