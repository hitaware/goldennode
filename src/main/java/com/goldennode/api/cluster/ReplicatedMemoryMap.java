package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

public class ReplicatedMemoryMap<K, V> extends ReplicatedMemoryObject implements Map<K, V> {
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
