package com.cornell.multitenantcache.integrations;

public class LRUMapFactory {

    public static <K,V>  LRUMap<K,V> getNewInstance(LRUMapType type) {
        if(type.equals(LRUMapType.IN_MEMORY)) {
            return new SimpleLRUMap<>();
        }
        if(type.equals(LRUMapType.JIFFY)) {
            return new JiffyLRUMap<>();
        }
        throw new RuntimeException("LRUMapType requested is not integrated");
    }
}
