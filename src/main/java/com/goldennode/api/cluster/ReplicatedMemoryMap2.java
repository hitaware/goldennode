package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

public class ReplicatedMemoryMap2<K, V> extends ReplicatedMemoryObject implements Map<K, V> {
    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMap2.class);
    protected Hashtable<K, V> innerMap = new Hashtable<K, V>();

    public ReplicatedMemoryMap2() {
        super();
    }

    public ReplicatedMemoryMap2(String publicName) {
        super(publicName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        if (getCluster() == null) {
            return _put(key, value);
        }
        return (V) safeOperate(new Operation(getPublicName(), "put", key, value));
    }

    public V _put(K key, V value) {
        V previousValue = null;
        if (innerMap.containsKey(key)) {
            previousValue = _base_put(key, value);
            createUndoRecord(new Operation(getPublicName(), "base_put", key, previousValue));
            return previousValue;
        } else {
            _base_put(key, value);
            createUndoRecord(new Operation(getPublicName(), "base_remove", key));
            return null;
        }
    }

    public V _base_put(K key, V value) {
        return innerMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        if (getCluster() == null) {
            return _remove(key);
        }
        return (V) safeOperate(new Operation(getPublicName(), "remove", key));
    }

    public V _remove(Object key) {
        V previousValue = null;
        if (innerMap.containsKey(key)) {
            previousValue = _base_remove(key);
            createUndoRecord(new Operation(getPublicName(), "base_put", key, previousValue));
            return previousValue;
        }
        return null;
    }

    public V _base_remove(Object key) {
        return innerMap.remove(key);
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
        @SuppressWarnings("unchecked")
        Hashtable<K, V> ht = (Hashtable<K, V>) innerMap.clone();
        _base_clear();
        createUndoRecord(new Operation(getPublicName(), "base_putAll", ht));
    }

    public void _base_clear() {
        innerMap.clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    public void _base_putAll(Map<? extends K, ? extends V> m) {
        innerMap.putAll(m);
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return innerMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return innerMap.get(key);
    }

    @Override
    public Set<K> keySet() {
        return innerMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return innerMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return innerMap.entrySet();
    }
}
