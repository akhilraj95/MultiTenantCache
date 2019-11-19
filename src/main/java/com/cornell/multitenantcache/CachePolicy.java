package com.cornell.multitenantcache;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CachePolicy {

    private boolean cachedOnRead;
}
