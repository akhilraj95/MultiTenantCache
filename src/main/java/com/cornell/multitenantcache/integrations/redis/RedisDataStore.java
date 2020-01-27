package com.cornell.multitenantcache.integrations.redis;

import com.cornell.multitenantcache.integrations.DataStore;

public class RedisDataStore<K,D> implements DataStore<K, D> {

    @Override
    public D read(K key) {
        return null;
    }

    @Override
    public void write(K key, D data) {

    }
}
