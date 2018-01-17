package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

public class ReplicatedMemoryMap<K, V> extends ReplicatedMemoryObject implements Map<K, V> {
    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMap.class);
    protected Hashtable<K, V> innerMap = new Hashtable<K, V>();

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

    public boolean _put(K key, V value) {
        return addToUncommited(new Operation(getPublicName(), "base_put", key, value));
    }

    public V _base_put(K key, V value) {
        return innerMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        return (V) safeOperate(new Operation(getPublicName(), "remove", key));
    }

    public boolean _remove(Object key) {
        return addToUncommited(new Operation(getPublicName(), "base_remove", key));
    }

    public V _base_remove(Object key) {
        return innerMap.remove(key);
    }

    @Override
    public void clear() {
        safeOperate(new Operation(getPublicName(), "clear"));
    }

    public boolean _clear() {
        return addToUncommited(new Operation(getPublicName(), "base_clear"));
    }

    public void _base_clear() {
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
            getCluster().readLock(this);
            locked = true;
            return innerMap.size();
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.isEmpty();
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.containsKey(key);
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public boolean containsValue(Object value) {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.containsValue(value);
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public V get(Object key) {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.get(key);
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public Set<K> keySet() {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.keySet();
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public Collection<V> values() {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.values();
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        boolean locked = false;
        try {
            getCluster().readLock(this);
            locked = true;
            return innerMap.entrySet();
        } catch (ClusterException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (locked) {
                try {
                    getCluster().unlockReadLock(this);
                } catch (ClusterException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }
}
