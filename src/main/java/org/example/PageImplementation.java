package org.example;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PageImplementation implements Page {
    // Use the constant from Page interface
    public static final int PAGE_SIZE = Page.PAGE_SIZE; // 4096 bytes

    private final int pageId;
    private final byte[] data;
    private int pinCount = 0;
    private boolean isDirty;
    private int nextFreeOffset;

    public PageImplementation(int pageId, int pageSize) {
        if (pageSize != PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be " + PAGE_SIZE + " bytes");
        }
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        this.isDirty = false;
        this.nextFreeOffset = 0;
        initializeNextFreeOffset();
    }

    public PageImplementation(int pageId, byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be " + PAGE_SIZE + " bytes");
        }
        this.pageId = pageId;
        this.data = Arrays.copyOf(data, PAGE_SIZE);
        initializeNextFreeOffset();
    }


//    @Override
//    public Row getRow(int rowId) {
//        int offset = rowId * Row.ROW_SIZE;
//        if (offset + Row.ROW_SIZE > data.length) {
//            return null; // Out of bounds
//        }
//
//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        buffer.position(offset);
//
//        byte[] movieIdBytes = new byte[9];
//        byte[] titleBytes = new byte[30];
//
//        buffer.get(movieIdBytes);
//        buffer.get(titleBytes);
//
//        byte[] trimmedMovieId = utilities_new.removeTrailingBytes(movieIdBytes);
//        byte[] trimmedTitle = utilities_new.removeTrailingBytes(titleBytes);
//
//        return new Row(trimmedMovieId, trimmedTitle);
//    }
//
//    @Override
//    public int insertRow(Row row) {
//        if (nextFreeOffset + Row.ROW_SIZE > data.length) {
//            return -1; //  Page full
//        }
//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        buffer.position(nextFreeOffset);
//
//        byte[] paddedMovieId = utilities_new.truncateOrPadByteArray(row.movieId, 9);
//        byte[] paddedTitle = utilities_new.truncateOrPadByteArray(row.title, 30);
//
//        buffer.put(paddedMovieId);
//        buffer.put(paddedTitle);
//
//        int insertedIndex = nextFreeOffset / Row.ROW_SIZE;
//        nextFreeOffset += Row.ROW_SIZE;
////        markDirty();
//        return insertedIndex;
//    }

    private boolean isDataPage() {
        if (data.length == 0) return false; // Empty page can't be determined
        return data[BTreePage.NODE_TYPE_OFFSET] == BTreePage.DATA_PAGE;
    }

    @Override
    public Row getRow(int rowId) {
        if (isDataPage()) {
            return getDataRow(rowId);
        } else {
            return getIndexRow(rowId);
        }
    }

    private Row getDataRow(int rowId) {
        int offset = rowId * DataRow.SIZE;
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

    private Row getIndexRow(int rowId) {
        boolean isLeaf = data[BTreePage.NODE_TYPE_OFFSET] == BTreePage.LEAF_NODE;
        int rowSize = isLeaf ? LeafRow.SIZE : NonLeafRow.SIZE;

        int offset = rowId * rowSize;
        if (offset + rowSize > data.length) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);

        if (isLeaf) {
            int keyLen = buffer.getInt();
            byte[] key = new byte[keyLen];
            buffer.get(key);
            int pid = buffer.getInt();
            int sid = buffer.getInt();
            return new LeafRow(key, new Rid(pid, sid));
        } else {
            int keyLen = buffer.getInt();
            byte[] key = new byte[keyLen];
            buffer.get(key);
            int childPageId = buffer.getInt();
            return new NonLeafRow(key, childPageId);
        }
    }

    @Override
    public int insertRow(Row row) {
        // Determine row size based on instance type
        int rowSize;
        if (row instanceof DataRow) {
            rowSize = DataRow.SIZE;
            // Ensure page is marked as data page
            if (data.length == 0) {
                data[BTreePage.NODE_TYPE_OFFSET] = BTreePage.DATA_PAGE;
            }
        } else if (row instanceof LeafRow) {
            rowSize = LeafRow.SIZE;
            // Ensure page is marked as leaf node
            if (data.length == 0) {
                data[BTreePage.NODE_TYPE_OFFSET] = BTreePage.LEAF_NODE;
            }
        } else { // NodePointerRow
            rowSize = NonLeafRow.SIZE;
            // Ensure page is marked as internal node
            if (data.length == 0) {
                data[BTreePage.NODE_TYPE_OFFSET] = BTreePage.INTERNAL_NODE;
            }
        }

        if (nextFreeOffset + rowSize > data.length) {
            return -1; // Page full
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(nextFreeOffset);
        buffer.put(row.getBytes());

        int insertedIndex = nextFreeOffset / rowSize;
        nextFreeOffset += rowSize;
        markDirty();
        return insertedIndex;
    }


//    private int getRowSize() {
//        // Determine based on page type
//        if (data.length == 0) return DataRow.SIZE; // Default
//
//        if (/* is data page */) {
//            return DataRow.SIZE;
//        } else if (data[BTreePage.NODE_TYPE_OFFSET] == BTreePage.LEAF_NODE) {
//            return LeafRow.SIZE;
//        } else {
//            return NonLeafRow.SIZE;
//        }
//    }

    @Override
    public boolean isFull() {
        return (nextFreeOffset + Row.ROW_SIZE) > PAGE_SIZE;
    }

    @Override
    public void markDirty() {
        this.isDirty = true;  // ✅ Directly mark the page as dirty
    }


    @Override
    public int getPid() {
        return pageId;
    }

    @Override
    public byte[] getData() {
        return Arrays.copyOf(data, data.length); // Return defensive copy
    }

    @Override
    public void setData(byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Data length must be " + PAGE_SIZE + " bytes");
        }
        System.arraycopy(data, 0, this.data, 0, PAGE_SIZE);
        this.isDirty = true;
        initializeNextFreeOffset(); // Recalculate free space
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
        return isDirty;  // ✅ Expose dirty status
    }

    private void initializeNextFreeOffset() {
        for (int i = 0; i < data.length; i += Row.ROW_SIZE) {
            if (data[i] == 0) { // ✅ Empty slot found
                nextFreeOffset = i;
                return;
            }
        }
        nextFreeOffset = data.length; // ✅ Page full
    }
}