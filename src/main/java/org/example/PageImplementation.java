package org.example;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PageImplementation implements Page {
    public static final int PAGE_SIZE = Page.PAGE_SIZE; // 4096 bytes
    private static final int HEADER_SIZE = 1; // Reserve 1 byte for the page type header

    private final int pageId;
    private final byte[] data;
    private int pinCount = 0;
    private boolean isDirty;
    private int nextFreeOffset; // Points to where the next row will be written

    public PageImplementation(int pageId, int pageSize) {
        if (pageSize != PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be " + PAGE_SIZE + " bytes");
        }
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        this.isDirty = false;
        // Start writing rows after the header
        this.nextFreeOffset = HEADER_SIZE;
    }

    public PageImplementation(int pageId, byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be " + PAGE_SIZE + " bytes");
        }
        this.pageId = pageId;
        this.data = Arrays.copyOf(data, PAGE_SIZE);
        initializeNextFreeOffset();
    }

    // Initialize nextFreeOffset by scanning from HEADER_SIZE onwards
    private void initializeNextFreeOffset() {
        for (int i = HEADER_SIZE; i < data.length; i += Row.ROW_SIZE) {
            if (data[i] == 0) {
                nextFreeOffset = i;
                return;
            }
        }
        nextFreeOffset = data.length; // Page is full
    }

    private boolean isDataPage() {
        if (data.length == 0) return false;
        return data[BTreePage.NODE_TYPE_OFFSET] == BTreePage.DATA_PAGE;
    }

    @Override
    public Row getRow(int rowId) {
        // Compute the offset for the row: header + row index * rowSize
        int rowSize = getRowSize();
        int offset = HEADER_SIZE + rowId * rowSize;
        if (isDataPage()) {
            return getDataRow(offset);
        } else {
            return getIndexRow(offset);
        }
    }

    // Determine the row size based on the page type stored in the header
    private int getRowSize() {
        byte pageType = data[BTreePage.NODE_TYPE_OFFSET];
        if (pageType == BTreePage.DATA_PAGE) {
            return DataRow.SIZE;
        } else if (pageType == BTreePage.LEAF_NODE) {
            return LeafRow.SIZE;
        } else {
            return NonLeafRow.SIZE;
        }
    }

    private Row getDataRow(int offset) {
        if (offset + DataRow.SIZE > data.length) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);
        byte[] movieId = new byte[9];
        byte[] title = new byte[30];
        buffer.get(movieId);
        buffer.get(title);
        return new DataRow(movieId, title);
    }

    private Row getIndexRow(int offset) {
        byte pageType = data[BTreePage.NODE_TYPE_OFFSET];
        if (pageType == BTreePage.LEAF_NODE) {
            if (offset + LeafRow.SIZE > data.length) return null;
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.position(offset);
            int keyLen = buffer.getInt();
            byte[] key = new byte[keyLen];
            buffer.get(key);
            int pid = buffer.getInt();
            int sid = buffer.getInt();
            return new LeafRow(key, new Rid(pid, sid));
        } else { // Internal node
            if (offset + NonLeafRow.SIZE > data.length) return null;
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.position(offset);
            int keyLen = buffer.getInt();
            byte[] key = new byte[keyLen];
            buffer.get(key);
            int childPageId = buffer.getInt();
            return new NonLeafRow(key, childPageId);
        }
    }

    @Override
    public int insertRow(Row row) {
        int rowSize;
        if (row instanceof DataRow) {
            rowSize = DataRow.SIZE;
            // Mark page as data page if not already marked
            if (data[BTreePage.NODE_TYPE_OFFSET] != BTreePage.DATA_PAGE) {
                data[BTreePage.NODE_TYPE_OFFSET] = BTreePage.DATA_PAGE;
            }
        } else if (row instanceof LeafRow) {
            rowSize = LeafRow.SIZE;
            if (data[BTreePage.NODE_TYPE_OFFSET] != BTreePage.LEAF_NODE) {
                data[BTreePage.NODE_TYPE_OFFSET] = BTreePage.LEAF_NODE;
            }
        } else { // NonLeafRow (internal node)
            rowSize = NonLeafRow.SIZE;
            if (data[BTreePage.NODE_TYPE_OFFSET] != BTreePage.INTERNAL_NODE) {
                data[BTreePage.NODE_TYPE_OFFSET] = BTreePage.INTERNAL_NODE;
            }
        }
        if (nextFreeOffset + rowSize > data.length) {
            return -1; // Page full
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(nextFreeOffset);
        buffer.put(row.getBytes());
        // Calculate the row index based on the HEADER_SIZE offset
        int insertedIndex = (nextFreeOffset - HEADER_SIZE) / rowSize;
        nextFreeOffset += rowSize;
        markDirty();
        return insertedIndex;
    }

    @Override
    public boolean isFull() {
        return (nextFreeOffset + getRowSize()) > PAGE_SIZE;
    }

    @Override
    public void markDirty() {
        this.isDirty = true;
    }

    @Override
    public int getPid() {
        return pageId;
    }

    @Override
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void setData(byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Data length must be " + PAGE_SIZE + " bytes");
        }
        System.arraycopy(data, 0, this.data, 0, PAGE_SIZE);
        this.isDirty = true;
        initializeNextFreeOffset();
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
        return isDirty;
    }
}