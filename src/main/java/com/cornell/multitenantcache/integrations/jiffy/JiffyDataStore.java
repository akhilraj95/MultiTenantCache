package com.cornell.multitenantcache.integrations.jiffy;

import com.cornell.multitenantcache.integrations.DataStore;
import jiffy.JiffyClient;
import jiffy.storage.FileReader;
import jiffy.storage.FileWriter;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

public class JiffyDataStore implements DataStore<String, Byte[]> {

    private JiffyClient client;
    private int DEFAULT_SIZE = 10;

    public JiffyDataStore() {
        try {
            this.client = new JiffyClient("https://localhost.com", 8000,9000);
        } catch (TException e) {
            throw new RuntimeException("Jiffy Client Creation Failed");
        }
    }

    private byte[] getPrimitiveBytes(Byte[] bytesObjects) {
        byte[] bytes = new byte[10];
        int j=0;
        for(Byte b: bytesObjects)
            bytes[j++] = b.byteValue();
        return bytes;
    }

    private Byte[] getBoxedBytes(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];
        int i=0;
        for(byte b: bytes)
            byteObjects[i++] = b;
        return byteObjects;
    }

    private Byte[] toBytes(ByteBuffer bb) {
        final byte[] bytes = new byte[bb.remaining()];
        bb.duplicate().get(bytes);
        return getBoxedBytes(bytes);
    }

    private ByteBuffer fromByteArray(Byte[] bytesObjects) {
        byte[] bytes = getPrimitiveBytes(bytesObjects);
        final ByteBuffer ret = ByteBuffer.wrap(new byte[bytes.length]);
        ret.put(bytes);
        ret.flip();
        return ret;
    }

    @Override
    public Byte[] read(String key) {
        try {
            FileReader fileReader = client.openFile(key);
            ByteBuffer bb = fileReader.read(DEFAULT_SIZE);
            return toBytes(bb);
        } catch (TException e) {
            throw new RuntimeException("Failed to read file " + key);
        }
    }

    @Override
    public void write(String key, Byte[] data) {
        try {
            FileWriter file = client.createFile(key);
            ByteBuffer byteBuffer = fromByteArray(data);
            file.write(byteBuffer);
        } catch (TException e) {
            throw new RuntimeException("Failed to write file " + key);
        }
    }
}
