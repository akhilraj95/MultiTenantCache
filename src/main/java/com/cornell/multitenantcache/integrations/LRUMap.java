package com.cornell.multitenantcache.integrations;

import java.time.Instant;
import java.util.Optional;

public interface LRUMap<K,V> {

    V get(K key);

    Optional<V> optionalGet(K key);

    V put(K key, V value);

    void removeFirst();

    V removeLast();

    K getOldestKey();

    Optional<Instant> getOldestLastAccessTime();

    void logList();

    boolean containsKey(K key);
}
