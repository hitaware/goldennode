package com.goldennode.api.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.LoggerFactory;

public class ReplicatedMemorySet<E> extends ReplicatedMemoryObject implements Set<E> {
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
		if (getCluster() == null) {
			return _add(e);
		}
		return (boolean) safeOperate(new Operation(getPublicName(), "add", e));
	}

	public boolean _add(E e) {
		Boolean b = _base_add(e);
		createUndoRecord(new Operation(getPublicName(), "base_remove", e));
		return b;
	}

	public boolean _base_add(E e) {
		return innerSet.add(e);
	}

	@Override
	public boolean remove(Object o) {
		if (getCluster() == null) {
			return _remove(o);
		}
		return (boolean) safeOperate(new Operation(getPublicName(), "remove", o));
	}

	public boolean _remove(Object o) {
		Boolean b = _base_remove(o);
		createUndoRecord(new Operation(getPublicName(), "base_add", o));
		return b;
	}

	public boolean _base_remove(Object o) {
		return innerSet.remove(o);
	}

	@Override
	public void clear() {
		if (getCluster() == null) {
			_clear();
			return;
		}
		safeOperate(new Operation(getPublicName(), "clear"));
	}

	public void _clear() {
		ArrayList<E> al = new ArrayList<E>(innerSet);
		_base_clear();
		createUndoRecord(new Operation(getPublicName(), "base_addAll", al));
	}

	public void _base_clear() {
		innerSet.clear();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	public boolean _base_addAll(Collection<? extends E> c) {
		return innerSet.addAll(c);
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
		return innerSet.size();
	}

	@Override
	public boolean isEmpty() {
		return innerSet.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return innerSet.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return innerSet.iterator();
	}

	@Override
	public Object[] toArray() {
		return innerSet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return innerSet.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return innerSet.containsAll(c);
	}
}
