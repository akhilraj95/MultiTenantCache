package com.cornell.multitenantcache.integrations.inmemory;

import com.cornell.multitenantcache.integrations.DataStore;

import java.util.HashMap;
import java.util.Map;

public class InMemoryDataStore<K,V> implements DataStore<K,V> {

    private Map<K,V> dataMap;

    public InMemoryDataStore() {
        this.dataMap = new HashMap<>();
    }

    @Override
    public V read(K key) {
        if(!dataMap.containsKey(key)) {
            throw new RuntimeException("Failed to read data " + key);
        }
        return dataMap.get(key);
    }

    @Override
    public void write(K key, V data) {
        dataMap.put(key, data);
    }
}
