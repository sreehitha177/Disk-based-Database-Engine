package org.example.Rows;

import java.nio.ByteBuffer;

public class TitleNameRow extends Row {
    private final byte[] title;
    private final byte[] name;

    public TitleNameRow(byte[] title, byte[] name) {
        this.title = title;
        this.name = name;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(title.length + name.length);
        buffer.put(title);
        buffer.put(name);
        return buffer.array();
    }

    @Override
    public int getSize() {
        return title.length + name.length;
    }

    public byte[] getTitle() {
        return title;
    }

    public byte[] getName() {
        return name;
    }
}
