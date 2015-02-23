package com.goldennode.api.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ClusteredList<E> extends ClusteredObject implements List<E> {

	private static final long serialVersionUID = 1L;

	protected List<E> innerList = Collections.synchronizedList(new ArrayList<E>());

	public ClusteredList() {
		super();
	}

	public ClusteredList(String publicName, String ownerId) throws ClusterException {
		super(publicName, ownerId);
	}

	public ClusteredList(String publicName, String ownerId, Cluster cluster) throws ClusterException {
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

		if (getCluster() != null) {
			return (Boolean) getCluster().safeMulticast(new Operation(getPublicName(), "add", e)).get(0)
					.getReturnValue();

		} else {
			return _add(e);
		}

	}

	public Boolean _add(E e) {
		Boolean b = _u_add(e);
		if (getCluster() != null) {
			// TODO getCluster().getProxy().createUndoRecord(
			// new Operation(getPublicName(), "u_remove", e));
		}
		return b;
	}

	public Boolean _u_add(E e) {
		return innerList.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E element) {

		if (getCluster() != null) {

			return (E) getCluster().safeMulticast(new Operation(getPublicName(), "set", index, element)).get(0)
					.getReturnValue();

		} else {
			return _set(index, element);
		}

	}

	public E _set(int index, E element) {

		E e = _u_set(index, element);
		if (getCluster() != null) {
			// TODO getCluster().getProxy().createUndoRecord(new
			// Operation(getPublicName(), "u_set", index, e));
		}
		return e;
	}

	public E _u_set(int index, E element) {

		E e = innerList.set(index, element);

		return e;
	}

	@Override
	public void add(int index, E element) {

		if (getCluster() != null) {
			getCluster().safeMulticast(new Operation(getPublicName(), "add", index, element));

		} else {
			_add(index, element);
		}

	}

	public void _add(int index, E element) {
		_u_add(index, element);
		if (getCluster() != null) {
			// TODO getCluster().getProxy().createUndoRecord(new
			// Operation(getPublicName(), "u_remove", index));
		}

	}

	public void _u_add(int index, E element) {

		innerList.add(index, element);

	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index) {

		if (getCluster() != null) {

			return (E) getCluster().safeMulticast(new Operation(getPublicName(), "remove", index)).get(0)
					.getReturnValue();
		} else {
			return _remove(index);
		}

	}

	public E _remove(int index) {
		E e = _u_remove(index);
		if (getCluster() != null) {
			// TODO getCluster().getProxy().createUndoRecord(new
			// Operation(getPublicName(), "u_add", index, e));
		}
		return e;
	}

	public E _u_remove(int index) {
		E e = innerList.remove(index);
		return e;
	}

	@Override
	public void clear() {

		if (getCluster() != null) {
			getCluster().safeMulticast(new Operation(getPublicName(), "clear"));

		} else {
			_clear();
		}

	}

	public void _clear() {
		if (getCluster() != null) {
			// TODO getCluster().getProxy().createUndoRecord(
			// new Operation(getPublicName(), "u_addAll", new
			// ArrayList<E>(innerList)));
		}
		_u_clear();
	}

	public void _u_clear() {
		innerList.clear();
	}

	@Override
	public boolean remove(Object o) {

		if (getCluster() != null) {

			return (boolean) getCluster().safeMulticast(new Operation(getPublicName(), "remove", o)).get(0)
					.getReturnValue();
		} else {
			return _remove(o);
		}

	}

	public boolean _remove(Object o) {
		Boolean b = _u_remove(o);
		if (getCluster() != null) {
			// TODOgetCluster().getProxy().createUndoRecord(new
			// Operation(getPublicName(), "u_add", o));
		}
		return b;

	}

	public boolean _u_remove(Object o) {

		return innerList.remove(o);

	}

	@Override
	public boolean addAll(Collection<? extends E> c) {

		throw new UnsupportedOperationException();

	}

	public void _u_addAll(Collection<? extends E> c) {

		innerList.addAll(c);

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
