package com.cornell.multitenantcache;

import lombok.*;

import java.time.Instant;
import java.util.*;

public class LRUMap<K,V> {

    private Node<K,V> first;
    private Node<K,V> last;
    int size;

    private HashMap<K,Node<K,V>> map;

    public LRUMap() {
        this.first = new Node<>();
        this.last = new Node<>();
        first.setNext(last);
        last.setPrev(first);
        this.map = new HashMap<>();
    }

    public V get(Object key) {

        if(!map.containsKey(key)) return null;
        Node<K, V> node = map.get(key);
        Node<K, V> prev = node.getPrev();
        Node<K, V> next = node.getNext();
        prev.setNext(next);
        node.setPrev(last.getPrev());
        node.setNext(last);
        last.setPrev(last);
        return node.getValue();
    }

    public Optional<V> optionalGet(Object key) {
        return Optional.ofNullable(get(key));
    }

    public V put(K key, V value) {

        if(map.containsKey(key)) {
            V prevValue = map.get(key).getValue();
            map.get(key).setValue(value);
            return prevValue;
        }


        Node<K, V> toAddNode = new Node<>(key, value, Instant.now(), last, last.getPrev());

        last.getPrev().setNext(toAddNode);
        last.setPrev(toAddNode);
        size += 1;
        map.put(key,toAddNode);
        return null;
    }

    public void removeFirst() {
        Node<K, V> toDelete = first.getNext();
        first.setNext(toDelete.getNext());
        map.remove(toDelete.getKey());
        size -= 1;
    }

    public V removeLast() {
        Node<K, V> prev = last.getPrev();
        map.remove(prev.getKey());
        last.setPrev(prev.getPrev());
        size -= 1;
        return prev.getValue();
    }

    public K getOldestKey() {
        return first.getNext().getKey();
    }

    public Instant getOldestLastAccessTime() {
        return first.getNext().getTime();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    class Node<K,V> {
        K key;
        V value;
        Instant time;
        Node<K,V> next;
        Node<K,V> prev;
    }

}
