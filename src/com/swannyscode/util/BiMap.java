package com.swannyscode.util;

import java.util.*;

public class BiMap<K, V> {

    private final Map<K,V> map = new HashMap<>();
    private final Map<V,K> inverseMap = new HashMap<>();

    public BiMap() {}

    public void put(K k, V v) {
        map.put(k, v);
        inverseMap.put(v, k);
    }

    public V get(K k) {
        return map.get(k);
    }

    public K getKey(V v) {
        return inverseMap.get(v);
    }

    public Collection<V> getValues() {
        return map.values();
    }
}
