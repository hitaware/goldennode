package com.goldennode.api.core;

import java.util.ArrayList;
import java.util.Collection;

public class FixedSizeList<E> extends ArrayList<E> {
    private static final long serialVersionUID = 7011407150698793925L;
    private int maxSize;

    public FixedSizeList(int size) {
        if (size == -1) {
            size = Integer.MAX_VALUE;
        }
        maxSize = size;
    }

    private boolean checkSize(int cSize) {
        return !(super.size() + cSize > maxSize);
    }

    @Override
    public boolean add(E objectToAdd) {
        if (!checkSize(1)) {
            return false;
        }
        return super.add(objectToAdd);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (!checkSize(c.size())) {
            return false;
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (!checkSize(c.size())) {
            return false;
        }
        return super.addAll(index, c);
    }
}