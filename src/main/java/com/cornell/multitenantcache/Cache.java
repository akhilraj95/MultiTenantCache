package com.cornell.multitenantcache;

import java.io.Serializable;
import java.util.Optional;

public interface Cache<K extends Serializable, D extends Serializable> {

    boolean isPresent(String clientId, K key);

    Optional<D> read(String clientId, K key);

    boolean write(String clientId, K key, D data);
}
