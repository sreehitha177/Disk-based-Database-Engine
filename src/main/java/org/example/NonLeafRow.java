package org.example;

import java.nio.ByteBuffer;

public class NonLeafRow extends Row {
    public static final int SIZE = 12; // Adjust based on your needs
    private final byte[] key;
    private final int childPageId;

    public NonLeafRow(byte[] key, int childPageId) {
        this.key = key;
        this.childPageId = childPageId;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.putInt(key.length);
        buffer.put(key);
        buffer.putInt(childPageId);
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

    public int getChildPageId() {
        return childPageId;
    }
}