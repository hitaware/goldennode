package com.goldennode.api.test;

import com.goldennode.api.cluster.ClusteredList;
import com.goldennode.api.cluster.OperationException;

public class ErroneousClusteredList<E> extends ClusteredList<E> {

	private static final long serialVersionUID = 1365876379263713740L;

	@Override
	public Boolean _u_add(E e) {
		if (getCluster().getOwner().hashCode() % 2 == 0) {
			throw new OperationException("random error occured");
		}
		return innerList.add(e);

	}

}
