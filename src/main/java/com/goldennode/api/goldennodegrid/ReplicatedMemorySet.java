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
        return (boolean) safeOperate(new Operation(getPublicName(), "add", e));
    }

    public boolean _add(E e) {
        return innerSet.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return (boolean) safeOperate(new Operation(getPublicName(), "remove", o));
    }

    public boolean _remove(Object o) {
        return innerSet.remove(o);
    }

    @Override
    public void clear() {
        safeOperate(new Operation(getPublicName(), "clear"));
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
    }

    @Override
    public boolean isEmpty() {
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
    }

    @Override
    public boolean contains(Object o) {
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
    }

    @Override
    public Iterator<E> iterator() {
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
    }

    @Override
    public Object[] toArray() {
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
    }

    @Override
    public <T> T[] toArray(T[] a) {
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
    }

    @Override
    public boolean containsAll(Collection<?> c) {
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
}
