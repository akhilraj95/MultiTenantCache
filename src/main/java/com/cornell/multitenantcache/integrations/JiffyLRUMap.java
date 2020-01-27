package com.cornell.multitenantcache.integrations;

import jiffy.JiffyClient;
import jiffy.storage.FileReader;
import lombok.NoArgsConstructor;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Optional;

public class JiffyLRUMap implements LRUMap<String, ByteBuffer> {

    private JiffyClient client;

    private int DEFAULT_SIZE = 10;

    public JiffyLRUMap() {
        try {
            this.client = new JiffyClient("https://localhost.com", 8000,9000);
        } catch (TException e) {
            throw new RuntimeException("Jiffy Client Creation Failed");
        }
    }

    @Override
    public ByteBuffer get(String key) {
        try {
            FileReader fileReader = client.openFile(key);
            return fileReader.read(DEFAULT_SIZE);
        } catch (TException e) {
            throw new RuntimeException(key + "File Read Failed.");
        }
    }

    @Override
    public Optional<ByteBuffer> optionalGet(String key) {
        return Optional.empty();
    }

    @Override
    public ByteBuffer put(String key, ByteBuffer value) {
        return null;
    }

    @Override
    public void removeFirst() {

    }

    @Override
    public ByteBuffer removeLast() {
        return null;
    }

    @Override
    public String getOldestKey() {
        return null;
    }

    @Override
    public Optional<Instant> getOldestLastAccessTime() {
        return Optional.empty();
    }

    @Override
    public void logList() {

    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }


    @NoArgsConstructor
    static class JiffyNode {
        String key;
        String value;
        Instant time;
        JiffyNode next;
        JiffyNode prev;
    }
}
