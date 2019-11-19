package com.cornell.multitenantcache;


import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Builder
public class MultitenantCachConfig {

    @Getter
    @Singular("client")
    Map<String, Integer> clientCacheCount;
}
