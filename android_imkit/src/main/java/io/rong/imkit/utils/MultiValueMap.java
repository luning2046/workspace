package io.rong.imkit.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhjchen on 14-3-20.
 */
public class MultiValueMap<K, V> {
    private Map<K, List<V>> mMap;

    public MultiValueMap() {
        this(0);
    }

    public MultiValueMap(int capacity) {
        mMap = new ConcurrentHashMap<K, List<V>>(capacity);
    }

    public List<V> get(K key) {
        return mMap.get(key);
    }

    public void put(K key, V value) {
        List<V> values = mMap.get(key);
        if (values == null) {
            values = new ArrayList<V>();
            values.add(value);
            mMap.put(key, values);
        } else if (!containsValue(key, value)) {
            values.add(value);
        }
    }

    public void putAll(K key, ArrayList<V> values) {
        List<V> valuesMap = mMap.get(key);
        if (valuesMap == null) {
            valuesMap = new ArrayList<V>();
            mMap.put(key, valuesMap);
        }
        if (values != null) {
            for (V v : values) {
                if (!containsValue(key, v)) {
                    valuesMap.add(v);
                }
            }
        }
    }

    public List<V> remove(K key) {
        List<V> values = mMap.get(key);
        if (values != null) {
            values.clear();
        }
        return mMap.remove(key);
    }

    public boolean remove(K key, V value) {
        List<V> values = mMap.get(key);
        if (values != null) {
            return values.remove(value);
        }
        return false;
    }

    public boolean removeValue(V value) {
        boolean isRemove = true;
        for (Map.Entry<K, List<V>> entry : mMap.entrySet()) {
            List<V> values = entry.getValue();
            if (values != null) {
                if (!values.remove(value)) {
                    isRemove = false;
                }
            }
        }
        return isRemove;
    }

    public void clear() {
        for (Map.Entry<K, List<V>> entry : mMap.entrySet()) {
            List<V> values = entry.getValue();
            if (values != null) {
                values.clear();
            }
        }
        mMap.clear();
    }

    public boolean containsKey(K key) {
        return mMap.containsKey(key);
    }

    public boolean containsValue(K key, V value) {
        List<V> values = mMap.get(key);
        return values != null && values.contains(value);
    }

    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    public int size() {
        return mMap.size();
    }

    public Set<K> keySet() {
        return mMap.keySet();
    }

    public Set<Map.Entry<K, List<V>>> entrySet() {
        return mMap.entrySet();
    }

    public Collection<List<V>> values() {
        return mMap.values();
    }
}
