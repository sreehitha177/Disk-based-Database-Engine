package org.example;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PageImplementation implements Page {
    public static final int PAGE_SIZE = Page.PAGE_SIZE; // 4096 bytes
    private static final int HEADER_SIZE = 1; // Reserve 1 byte for the page type header
    public static final byte DATA_PAGE = 2;
    public static final byte WORKEDON_PAGE = 3;
    public static final byte PEOPLE_PAGE = 4;

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
        return data[BTreePage.NODE_TYPE_OFFSET] == DATA_PAGE;
    }

    @Override
//    public Row getRow(int rowId) {
//        // Compute the offset for the row: header + row index * rowSize
//        int rowSize = getRowSize();
//        int offset = HEADER_SIZE + rowId * rowSize;
//        if (isDataPage()) {
//            return getDataRow(offset);
//        } else {
//            return getIndexRow(offset);
//        }
//    }
    public Row getRow(int rowId) {
        byte pageType = data[BTreePage.NODE_TYPE_OFFSET];
        if (pageType == BTreePage.LEAF_NODE || pageType == BTreePage.INTERNAL_NODE) {
            int rowSize = getRowSize();
            int offset = HEADER_SIZE + rowId * rowSize;
            return getIndexRow(offset);
        } else {
            throw new UnsupportedOperationException("getRow() called on Data Page. Use specific getXXXRow() from ScanOperator.");
        }
    }


    // Determine the row size based on the page type stored in the header
    private int getRowSize() {
        byte pageType = data[BTreePage.NODE_TYPE_OFFSET];
        if (pageType == DATA_PAGE) {
            return DataRow.SIZE;
        } else if (pageType == BTreePage.LEAF_NODE) {
            return LeafRow.SIZE;
        } else {
            return NonLeafRow.SIZE;
        }
    }

    public Row getDataRow(int offset) {
        if (offset + DataRow.SIZE > data.length) {
            return null;
        }
        System.out.println("getDataRow @ offset " + offset);

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);

        for (int i = 0; i < 39; i++) {
            System.out.printf("%02X ", buffer.get(i));
        }
        System.out.println();

        byte[] movieId = new byte[9];
        byte[] title = new byte[30];
        buffer.get(movieId);
        buffer.get(title);
        return new DataRow(movieId, title);
    }

    public Row getDataRowBySlot(int slotId) {
        int offset = HEADER_SIZE + slotId * DataRow.SIZE;
        if (offset + DataRow.SIZE > nextFreeOffset) {
            return null;
        }
        return getDataRow(offset);
    }


    public Row getWorkedOnRow(int slotId) {
        int offset = HEADER_SIZE + slotId * WorkedOnRow.SIZE;
        if (offset + WorkedOnRow.SIZE > data.length) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);
        byte[] movieId = new byte[9];
        byte[] personId = new byte[10];
        byte[] category = new byte[20];
        buffer.get(movieId);
        buffer.get(personId);
        buffer.get(category);
        return new WorkedOnRow(movieId, personId, category);
    }

    public Row getPeopleRow(int slotId) {
        int offset = HEADER_SIZE + slotId * PeopleRow.SIZE;
        if (offset + PeopleRow.SIZE > data.length) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);
        byte[] personId = new byte[10];
        byte[] name = new byte[105];
        buffer.get(personId);
        buffer.get(name);
        System.out.println("Reading PeopleRow @ offset = " + offset);
        System.out.printf("Bytes: %02X %02X %02X %02X ...\n", data[offset], data[offset+1], data[offset+2], data[offset+3]);

        return new PeopleRow(personId, name);
    }

    public Row getTempRow(int slotId) {
        int offset = HEADER_SIZE + slotId * TempRow.SIZE;
        if (offset + TempRow.SIZE > data.length) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);
        byte[] movieId = new byte[9];
        byte[] personId = new byte[10];
        buffer.get(movieId);
        buffer.get(personId);
        return new TempRow(movieId, personId);
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
            if (data[BTreePage.NODE_TYPE_OFFSET] != DATA_PAGE) {
                data[BTreePage.NODE_TYPE_OFFSET] = DATA_PAGE;
            }
        } else if (row instanceof WorkedOnRow) {
            rowSize = WorkedOnRow.SIZE;
            // Mark page as data page if not already marked
            if (data[BTreePage.NODE_TYPE_OFFSET] != WORKEDON_PAGE) {
                data[BTreePage.NODE_TYPE_OFFSET] = WORKEDON_PAGE;
            }
        } else if (row instanceof PeopleRow) {
            rowSize = WorkedOnRow.SIZE;
            // Mark page as data page if not already marked
            if (data[BTreePage.NODE_TYPE_OFFSET] != PEOPLE_PAGE) {
                data[BTreePage.NODE_TYPE_OFFSET] = PEOPLE_PAGE;
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