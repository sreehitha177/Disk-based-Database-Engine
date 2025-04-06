package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BTreePage {
    // Make constants public
    public static final byte LEAF_NODE = 0;
    public static final byte INTERNAL_NODE = 1;
    public static final int NODE_TYPE_OFFSET = 0;
    public static final int KEY_COUNT_OFFSET = 1;
    public static final int DATA_START_OFFSET = 5; // 1 byte type + 4 bytes key count
    public static final byte DATA_PAGE = 2;

    public static byte[] serializeLeafNode(List<Comparable> keys, List<Rid> rids) {
        ByteBuffer buffer = ByteBuffer.allocate(Page.PAGE_SIZE);
        buffer.put(LEAF_NODE);
        buffer.putInt(keys.size());

        for (int i = 0; i < keys.size(); i++) {
            // Serialize key
            byte[] keyBytes = serializeKey(keys.get(i));
            buffer.putInt(keyBytes.length);
            buffer.put(keyBytes);

            // Serialize Rid
            buffer.putInt(rids.get(i).getPid());
            buffer.putInt(rids.get(i).getSid());
        }

        return buffer.array();
    }

    public static Rid getRidFromLeafPage(byte[] pageData, int keyIndex) {
        ByteBuffer buffer = ByteBuffer.wrap(pageData);
        buffer.position(DATA_START_OFFSET);

        int keyCount = buffer.getInt();
        if (keyIndex >= keyCount) throw new IndexOutOfBoundsException();

        // Skip to the requested key
        for (int i = 0; i < keyIndex; i++) {
            // Skip key
            int keyLen = buffer.getInt();
            buffer.position(buffer.position() + keyLen);
            // Skip Rid
            buffer.position(buffer.position() + 8); // 4 bytes pid + 4 bytes sid
        }

        // Read Rid
        int pid = buffer.getInt();
        int sid = buffer.getInt();
        return new Rid(pid, sid);
    }

    public static byte[] serializeKey(Comparable key) {
        if (key instanceof String) {
            return ((String)key).getBytes(StandardCharsets.UTF_8);
        } else if (key instanceof Integer) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt((Integer)key);
            return buffer.array();
        }
        throw new UnsupportedOperationException("Unsupported key type");
    }
}