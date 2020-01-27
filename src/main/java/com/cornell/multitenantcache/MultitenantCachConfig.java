package com.cornell.multitenantcache;


import com.cornell.multitenantcache.integrations.LRUMapType;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

@Getter
@Builder
public class MultitenantCachConfig {

    @Builder.Default private Duration isolationGurantee = Duration.ofSeconds(1);

    @Builder.Default private LRUMapType lruMapType = LRUMapType.IN_MEMORY;

    @Singular("client")
    private Map<String, Integer> clientCacheCount;
}
