package com.cornell.multitenantcache;

import java.io.Serializable;

public interface Cacheable<K extends Serializable, D extends Serializable> {

    public D read(K key);

    public void write(K key, D data);

}
