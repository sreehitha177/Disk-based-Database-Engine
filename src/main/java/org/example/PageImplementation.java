package org.example;

import java.nio.ByteBuffer;

public class PageImplementation implements Page {

    private final int pageId;
    private final byte[] data;
    private final int pageSize;
    private int pinCount = 0;
    private boolean isDirty;
    private int nextFreeOffset; // Keeps track of the next free row position

    public PageImplementation(int pageId, int pageSize) {
        this.pageId = pageId;
        this.pageSize = pageSize;
        this.data = new byte[pageSize];
        this.isDirty = false; // Initially, page is clean
        this.nextFreeOffset = 0;
        this.pinCount = 0;
        initializeNextFreeOffset();
    }

    public PageImplementation(int pageId, byte[] data) {
        this.pageId = pageId;
        this.pageSize = data.length;
        this.data = data;
        initializeNextFreeOffset();
    }

    @Override
    public Row getRow(int rowId) {
        int offset = rowId * Row.ROW_SIZE;
        if (offset + Row.ROW_SIZE > data.length) {
            return null; // Out of bounds
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);

        byte[] movieIdBytes = new byte[9];
        byte[] titleBytes = new byte[30];

        buffer.get(movieIdBytes);
        buffer.get(titleBytes);

        byte[] trimmedMovieId = Utilities.removeTrailingBytes(movieIdBytes);
        byte[] trimmedTitle = Utilities.removeTrailingBytes(titleBytes);

        return new Row(trimmedMovieId, trimmedTitle);
    }

    @Override
    public int insertRow(Row row) {
        if (nextFreeOffset + Row.ROW_SIZE > data.length) {
            return -1; // Page full
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(nextFreeOffset);

        byte[] paddedMovieId = Utilities.truncateOrPadByteArray(row.movieId, 9);
        byte[] paddedTitle = Utilities.truncateOrPadByteArray(row.title, 30);

        buffer.put(paddedMovieId);
        buffer.put(paddedTitle);

        int insertedIndex = nextFreeOffset / Row.ROW_SIZE;
        nextFreeOffset += Row.ROW_SIZE;
        return insertedIndex;
    }

    @Override
    public boolean isFull() {
        return (nextFreeOffset + Row.ROW_SIZE) > data.length;
    }

    @Override
    public void markDirty() {
        this.isDirty = true; // Directly mark the page as dirty
    }

    @Override
    public int getPid() {
        return pageId;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void setData(byte[] data) {

    }

    public void pin() {
        pinCount++;
    }

    public void unpin() {
        if (pinCount > 0) {
            pinCount--;
        }
    }

    public int getPinCount() {
        return pinCount;
    }

    public boolean isDirty() {
        return isDirty; // Expose dirty status
    }

    private void initializeNextFreeOffset() {
        for (int i = 0; i < data.length; i += Row.ROW_SIZE) {
            if (data[i] == 0) { // Empty slot found
                nextFreeOffset = i;
                return;
            }
        }
        nextFreeOffset = data.length; // Page full
    }
}
