package com.goldennode.api.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ClusteredList<E> extends ClusteredObject implements List<E> {

	private static final long serialVersionUID = 6014458553479582985L;

	protected List<E> innerList = Collections
			.synchronizedList(new ArrayList<E>());

	public ClusteredList() {
		super();
	}

	public ClusteredList(String publicName, String ownerId, Cluster cluster)
			throws ClusterException {
		super(publicName, ownerId, cluster);
	}

	public ClusteredList(String publicName, String ownerId)
			throws ClusterException {
		super(publicName, ownerId);
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

	@Override
	public boolean add(E e) {

		if (getCluster() != null) {
			return (Boolean) getCluster().safeMulticast(
					new Operation(getPublicName(), "add", e)).getReturnValue();

		} else {
			return _add(e);
		}

	}

	public Boolean _add(E e) {
		Boolean b = _u_add(e);
		createUndoRecord(new Operation(getPublicName(), "u_remove", e));
		return b;
	}

	public Boolean _u_add(E e) {
		return innerList.add(e);
	}

	@Override
	public E set(int index, E element) {

		if (getCluster() != null) {

			return (E) getCluster().safeMulticast(
					new Operation(getPublicName(), "set", index, element))
					.getReturnValue();

		} else {
			return _set(index, element);
		}

	}

	public E _set(Integer index, E element) {

		E e = _u_set(index.intValue(), element);
		createUndoRecord(new Operation(getPublicName(), "u_set", index, e));
		return e;
	}

	public E _u_set(Integer index, E element) {

		E e = innerList.set(index.intValue(), element);

		return e;
	}

	@Override
	public void add(int index, E element) {

		if (getCluster() != null) {
			getCluster().safeMulticast(
					new Operation(getPublicName(), "add", index, element));

		} else {
			_add(index, element);
		}

	}

	public void _add(Integer index, E element) {
		_u_add(index, element);
		createUndoRecord(new Operation(getPublicName(), "u_remove", index));

	}

	public void _u_add(Integer index, E element) {

		innerList.add(index, element);

	}

	@Override
	public E remove(int index) {

		if (getCluster() != null) {

			return (E) getCluster().safeMulticast(
					new Operation(getPublicName(), "remove", index))
					.getReturnValue();
		} else {
			return _remove(index);
		}

	}

	public E _remove(Integer index) {
		E e = _u_remove(index.intValue());
		createUndoRecord(new Operation(getPublicName(), "u_add", index, e));
		return e;
	}

	public E _u_remove(Integer index) {
		E e = innerList.remove(index.intValue());
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

		_u_clear();
		createUndoRecord(new Operation(getPublicName(), "u_addAll",
				cloneInnerList()));
	}

	public void _u_clear() {

		innerList.clear();
	}

	@Override
	public boolean remove(Object o) {

		if (getCluster() != null) {

			return (Boolean) getCluster().safeMulticast(
					new Operation(getPublicName(), "remove", o))
					.getReturnValue();
		} else {
			return _remove(o);
		}

	}

	public Boolean _remove(Object o) {
		Boolean b = _u_remove(o);
		createUndoRecord(new Operation(getPublicName(), "u_remove", o));
		return b;

	}

	public boolean _u_remove(Object o) {

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

	private Collection<? extends E> cloneInnerList() {
		List<E> lst = new ArrayList<E>();
		for (int i = 0; i < innerList.size(); i++) {
			lst.add(innerList.get(i));
		}
		return lst;
	}

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
