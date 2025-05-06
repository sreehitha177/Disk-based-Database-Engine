package org.example.Rows;

import java.nio.ByteBuffer;

public class LeafRow extends Row {
    public static final int SIZE = 16; // Adjust based on your needs
    private final byte[] key;
    private final Rid rid;

    public LeafRow(byte[] key, Rid rid) {
        this.key = key;
        this.rid = rid;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.putInt(key.length);
        buffer.put(key);
        buffer.putInt(rid.getPid());
        buffer.putInt(rid.getSid());
        return buffer.array();
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    // Getters
    public byte[] getKey() {
        return key;
    }

    public Rid getRid() {
        return rid;
    }
}