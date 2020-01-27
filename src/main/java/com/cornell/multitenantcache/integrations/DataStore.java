package com.cornell.multitenantcache.integrations;

public interface DataStore<K, D> {

    D read(K key);

    void write(K key, D data);
}
