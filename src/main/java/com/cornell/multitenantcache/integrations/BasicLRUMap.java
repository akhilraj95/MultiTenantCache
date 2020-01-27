package com.cornell.multitenantcache.integrations;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicLRUMap<K,V> implements LRUMap<K, V> {

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private DataStore<K, V> dataStore;
    private Node<K,V> first;
    private Node<K,V> last;
    private HashMap<K, Node<K,V>> map;
    int size;

    public BasicLRUMap(DataStore store){
        this.dataStore = store;
        this.first = new Node<>();
        this.last = new Node<>();
        first.setNext(last);
        last.setPrev(first);
        this.map = new HashMap<>();
    }

    @Override
    public V get(K key) {
        if(!map.containsKey(key)) return null;
        Node<K, V> node = map.get(key);
        Node<K, V> prev = node.getPrev();
        Node<K, V> next = node.getNext();
        prev.setNext(next);
        next.setPrev(prev);
        node.setPrev(last.getPrev());
        node.setNext(last);
        last.getPrev().setNext(node);
        last.setPrev(node);
        logList();
        return node.getValue();
    }

    @Override
    public Optional<V> optionalGet(K key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public V put(K key, V value) {

        if(map.containsKey(key)) {
            V prevValue = map.get(key).getValue();
            map.get(key).setValue(value);
            // TODO: 19/11/19 move it back
            return prevValue;
        }

        Node<K, V> toAddNode = new Node<>(key, value, dataStore, Instant.now(), last, last.getPrev());

        last.getPrev().setNext(toAddNode);
        last.setPrev(toAddNode);
        size += 1;
        map.put(key,toAddNode);
        logList();
        return null;
    }

    @Override
    public void removeFirst() {
        Node<K, V> toDelete = first.getNext();
        first.setNext(toDelete.getNext());
        map.remove(toDelete.getKey());
        logList();
        size -= 1;
    }

    @Override
    public V removeLast() {
        Node<K, V> prev = last.getPrev();
        map.remove(prev.getKey());
        last.setPrev(prev.getPrev());
        size -= 1;
        logList();
        return prev.getValue();
    }

    @Override
    public K getOldestKey() {
        return first.getNext().getKey();
    }

    @Override
    public Optional<Instant> getOldestLastAccessTime() {
        if(first.getNext() == null) return Optional.empty();
        return Optional.ofNullable(first.getNext().getTime());
    }

    @Override
    public void logList() {
        String log = "";
        Node<K, V> iter = first;
        while(iter != null) {
            log = log + iter.getKey() + "->";
            iter = iter.getNext();
        }
        logger.log(Level.INFO, log);
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

}
