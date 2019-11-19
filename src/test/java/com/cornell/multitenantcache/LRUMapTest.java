package com.cornell.multitenantcache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LRUMapTest {

    @Test
    public void writeIsOrdered() {
        LRUMap<String, String> lruMap = new LRUMap<>();
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
        LRUMap<String, String> lruMap = new LRUMap<>();
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