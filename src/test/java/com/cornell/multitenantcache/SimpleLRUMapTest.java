package com.cornell.multitenantcache;

import com.cornell.multitenantcache.integrations.LRUMap;
import com.cornell.multitenantcache.integrations.LRUMapFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class BasicLRUMapTest {

    @Test
    public void writeIsOrdered() {
        LRUMap<Object, Object> lruMap = LRUMapFactory.getLRUWithInMemoryCache();
        lruMap.put("1", "Val1");
        lruMap.put("2", "Val2");
        lruMap.put("3", "Val3");
        lruMap.put("4", "Val4");
        assertEquals("1", lruMap.getOldestKey());
        lruMap.removeFirst();
        assertEquals("2", lruMap.getOldestKey());
        lruMap.removeFirst();
        assertEquals("3", lruMap.getOldestKey());
        lruMap.removeFirst();
        assertEquals("4", lruMap.getOldestKey());
    }

    @Test
    public void orderIsShuffledOnRead() {
        LRUMap<Object, Object> lruMap = LRUMapFactory.getLRUWithInMemoryCache();
        lruMap.put("1", "Val1");
        lruMap.put("2", "Val2");
        lruMap.put("3", "Val3");
        lruMap.put("4", "Val4");
        assertEquals("1", lruMap.getOldestKey());
        lruMap.get("1");
        assertEquals("2", lruMap.getOldestKey());
        lruMap.get("2");
        assertEquals("3", lruMap.getOldestKey());
        lruMap.get("3");
        assertEquals("4", lruMap.getOldestKey());
        lruMap.get("4");
        assertEquals("1", lruMap.getOldestKey());
    }
}