package com.goldennode.api.goldennodegrid;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.GridException;

public class ReplicatedMemoryMap<K, V> extends DistributedObject implements Map<K, V> {
    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMap.class);
    Hashtable<K, V> innerMap = new Hashtable<K, V>();

    public ReplicatedMemoryMap() {
        super();
    }

    public ReplicatedMemoryMap(String publicName) {
        super(publicName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        return (V) safeOperate(new Operation(getPublicName(), "put", key, value));
    }
    
    @SuppressWarnings("unchecked")
    public V putLocal(K key, V value) {
        return innerMap.put(key, value);
    }

    public V _put(K key, V value) {
        return innerMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        return (V) safeOperate(new Operation(getPublicName(), "remove", key));
    }
    
    public V removeLocal(Object key) {
        return innerMap.remove(key);
    }

    public V _remove(Object key) {
        return innerMap.remove(key);
    }

    @Override
    public void clear() {
        safeOperate(new Operation(getPublicName(), "clear"));
    }

    public void _clear() {
        innerMap.clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.size();
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
            return innerMap.isEmpty();
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
    public boolean containsKey(Object key) {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.containsKey(key);
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
    public boolean containsValue(Object value) {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.containsValue(value);
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
    public V get(Object key) {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.get(key);
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
    public Set<K> keySet() {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.keySet();
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
    public Collection<V> values() {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.values();
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
    public Set<Entry<K, V>> entrySet() {
        boolean locked = false;
        try {
            getGrid().readLock(this);
            locked = true;
            return innerMap.entrySet();
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
