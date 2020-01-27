package com.cornell.multitenantcache.integrations;

import com.cornell.multitenantcache.integrations.inmemory.InMemoryDataStore;
import com.cornell.multitenantcache.integrations.jiffy.JiffyDataStore;

public class LRUMapFactory {

    public static <K,V>  LRUMap<K,V> getLRUWithInMemoryCache() {
        DataStore<K,V> dataStore = new InMemoryDataStore<K,V>();
        return new BasicLRUMap<K,V>(dataStore);
    }

    public static LRUMap<String, Byte[]> getLRUWithJiffyCache() {
        DataStore<String, Byte[]> dataStore = new JiffyDataStore();
        return new BasicLRUMap<>(dataStore);
    }

    public static LRUMap getNewInstance(LRUMapType type) {
        if(type.equals(LRUMapType.IN_MEMORY)) {
            return getLRUWithInMemoryCache();
        }
        if(type.equals(LRUMapType.JIFFY)) {
            return getLRUWithJiffyCache();
        }
        throw new IllegalArgumentException("LRUMapType requested " + String.valueOf(type) + "is not integrated");
    }
}
