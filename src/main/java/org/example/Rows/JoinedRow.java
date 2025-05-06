package org.example.Rows;

import java.nio.ByteBuffer;

public class JoinedRow extends Row {
    private final byte[] outerData;
    private final byte[] innerData;
    private final Row outer;
    private final Row inner;
    private final int size;

    public JoinedRow(Row outer, Row inner) {
        this.outerData = outer.getBytes();
        this.innerData = inner.getBytes();
        this.outer = outer;
        this.inner = inner;
        this.size = outerData.length + innerData.length;
    }

    public Row getOuter() {
        return outer;
    }

    public Row getInner() {
        return inner;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(outerData);
        buffer.put(innerData);
        return buffer.array();
    }

    @Override
    public int getSize() {
        return size;
    }

    // Optional: helpers to split outer and inner parts if needed
    public byte[] getOuterData() {
        return outerData;
    }

    public byte[] getInnerData() {
        return innerData;
    }
}
