package com.cornell.multitenantcache.integrations;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Getter
    @Setter
    @NoArgsConstructor
    static class Node<K, V> {
        K key;
        DataStore<K,V> dataStore;
        Instant time;
        Node<K,V> next;
        Node<K,V> prev;

        public Node(K key, V value, DataStore<K, V> dataStore, Instant time, Node<K, V> next, Node<K, V> prev) {
            this.key = key;
            this.dataStore = dataStore;
            this.time = time;
            this.next = next;
            this.prev = prev;
            this.setValue(value);
        }

        public V getValue() {
            return dataStore.read(key);
        }

        public void setValue(V data) {
            dataStore.write(key, data);
        }
    }
}
