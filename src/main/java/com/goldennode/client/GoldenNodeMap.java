package com.goldennode.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import com.goldennode.client.service.MapService;

/**
 * Hello world!
 * 
 * @param <K>
 *
 */
public class GoldenNodeMap<K extends Serializable, V extends Serializable> implements Map<K, V> {
    MapService<K,V> service;
    private String id;

    public GoldenNodeMap() {
        id = service.initializeMap();
    }

    public int size() {
        return service.size(id);
    }

    public boolean isEmpty() {
        return service.isEmpty(id);
    }

    public boolean containsKey(Object key) {
        return service.containsKey(id,key);
    }

    public boolean containsValue(Object value) {
        return service.containsValue(id,value);
    }

    public V get(Object key) {
        return service.get(id,key);
    }

    public V put(K key, V value) {
        return service.put(id,key,value);
    }

    public V remove(Object key) {
        return service.remove(id,key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        service.putAll(id,m);
    }

    public void clear() {
        service.clear(id);
    }

    public Set<K> keySet() {
        return service.keySet(id);
    }

    public Collection<V> values() {
        return service.values(id);
    }

    public Set<Entry<K, V>> entrySet() {
        return service.entrySet(id);
    }
}
