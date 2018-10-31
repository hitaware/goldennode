package com.goldennode.client.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface MapService<K extends Serializable, V extends Serializable>  extends Service {
    
    String initializeMap();

    int size(String id);

    boolean isEmpty(String id);

    boolean containsKey(String id, Object key);

    boolean containsValue(String id, Object value);

    V get(String id, Object key);

    V put(String id, K key, V value);

    V remove(String id, Object key);

    void putAll(String id, Map<? extends K, ? extends V> m);

    void clear(String id);

    Set<K> keySet(String id);

    Collection<V> values(String id);

    Set<Entry<K, V>> entrySet(String id);

    
}
