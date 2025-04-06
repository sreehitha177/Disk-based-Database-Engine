package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BTreeImplementation<K extends Comparable<K>, V> implements BTree<K, V> {
    private final BufferManager bufferManager;
    private final String indexFile;
    private int rootPageId;
    private final int order;
    private BTreeNode<K, V> root;

    public BTreeImplementation(int order, BufferManager bufferManager, String indexFile) {
        this.order = order;
        this.bufferManager = bufferManager;
        this.indexFile = indexFile;
        this.rootPageId = -1; // Will be initialized on first insert
        this.root = new BTreeLeafNode<>(this);
    }

    public int getOrder() {
        return order;
    }

    @Override
    public void insert(K key, Rid rid) {
        if (rootPageId == -1) {
            initializeRootPage();
        }

        BTreeNode.SplitResult<K, V> split = root.insert(key, rid);
        if (split != null) {
            BTreeNonLeafNode<K, V> newRoot = new BTreeNonLeafNode<>(this);
            newRoot.addKey(split.splitKey);
            newRoot.addChild(root);
            newRoot.addChild(split.rightNode);
            root = newRoot;
        }
        // Implementation of insert logic
        // ...
    }

    @Override
    public Iterator<Rid> search(K key) {
        if (rootPageId == -1) return Collections.emptyIterator();

        return root.search(key);
    }

    @Override
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        if (rootPageId == -1) return Collections.emptyIterator();

        List<Rid> results = new ArrayList<>();
        int currentPageId = rootPageId;

        while (true) {
            Page page = bufferManager.getPage(indexFile, currentPageId);
            byte[] pageData = page.getData();

            if (isLeafPage(pageData)) {
                // Collect all Rids in range from leaf page
                collectRidsInRange(pageData, startKey, endKey, results);
                bufferManager.unpinPage(indexFile, currentPageId);
                break;
            } else {
                // Internal node - navigate to appropriate child
                currentPageId = getChildPageForRange(pageData, startKey);
                bufferManager.unpinPage(indexFile, currentPageId);
            }
        }

        return results.iterator();
    }

//    private void collectRidsInRange(byte[] pageData, K startKey, K endKey, List<Rid> results) {
//        ByteBuffer buffer = ByteBuffer.wrap(pageData);
//        buffer.position(BTreePage.DATA_START_OFFSET);
//
//        int keyCount = buffer.getInt();
//        for (int i = 0; i < keyCount; i++) {
//            int keyLen = buffer.getInt();
//            byte[] keyBytes = new byte[keyLen];
//            buffer.get(keyBytes);
//            K currentKey = deserializeKey(keyBytes);
//
//            // Check if key is in range
//            if (currentKey.compareTo(startKey) >= 0 && currentKey.compareTo(endKey) <= 0) {
//                int pid = buffer.getInt();
//                int sid = buffer.getInt();
//                results.add(new Rid(pid, sid));
//            } else {
//                // Skip Rid if key is out of range
//                buffer.position(buffer.position() + 8);
//            }
//        }
//    }
//
//    private int getChildPageForRange(byte[] pageData, K startKey) {
//        ByteBuffer buffer = ByteBuffer.wrap(pageData);
//        buffer.position(BTreePage.DATA_START_OFFSET);
//
//        int keyCount = buffer.getInt();
//        for (int i = 0; i < keyCount; i++) {
//            int keyLen = buffer.getInt();
//            byte[] keyBytes = new byte[keyLen];
//            buffer.get(keyBytes);
//            K currentKey = deserializeKey(keyBytes);
//
//            if (startKey.compareTo(currentKey) < 0) {
//                // Return left child pointer
//                return buffer.getInt();
//            }
//            // Skip right child pointer
//            buffer.position(buffer.position() + 4);
//        }
//        // Return last child pointer
//        return buffer.getInt();
//    }
//
//    private boolean isLeafPage(byte[] pageData) {
//        return pageData[BTreePage.NODE_TYPE_OFFSET] == BTreePage.LEAF_NODE;
//    }

    private void collectRidsInRange(byte[] pageData, K startKey, K endKey, List<Rid> results) {
        ByteBuffer buffer = ByteBuffer.wrap(pageData);
        buffer.position(BTreePage.DATA_START_OFFSET);

        int keyCount = buffer.getInt();
        for (int i = 0; i < keyCount; i++) {
            int keyLen = buffer.getInt();
            byte[] keyBytes = new byte[keyLen];
            buffer.get(keyBytes);
            K currentKey = deserializeKey(keyBytes);

            if (currentKey.compareTo(startKey) >= 0 && currentKey.compareTo(endKey) <= 0) {
                int pid = buffer.getInt();
                int sid = buffer.getInt();
                results.add(new Rid(pid, sid));
            } else {
                buffer.position(buffer.position() + 8);
            }
        }
    }

    private int getChildPageForRange(byte[] pageData, K startKey) {
        ByteBuffer buffer = ByteBuffer.wrap(pageData);
        buffer.position(BTreePage.DATA_START_OFFSET);

        int keyCount = buffer.getInt();
        for (int i = 0; i < keyCount; i++) {
            int keyLen = buffer.getInt();
            byte[] keyBytes = new byte[keyLen];
            buffer.get(keyBytes);
            K currentKey = deserializeKey(keyBytes);

            if (startKey.compareTo(currentKey) < 0) {
                return buffer.getInt();
            }
            buffer.position(buffer.position() + 4);
        }
        return buffer.getInt();
    }

    private boolean isLeafPage(byte[] pageData) {
        return pageData[BTreePage.NODE_TYPE_OFFSET] == BTreePage.LEAF_NODE;
    }

    @SuppressWarnings("unchecked")
    private K deserializeKey(byte[] keyBytes) {
        // Implement based on your key type
        return (K) new String(keyBytes, StandardCharsets.UTF_8);
    }

    private void initializeRootPage() {
        Page rootPage = bufferManager.createPage(indexFile);
        this.rootPageId = rootPage.getPid();

        // Serialize empty leaf node
        byte[] emptyLeaf = BTreePage.serializeLeafNode(
                Collections.emptyList(), Collections.emptyList());
        rootPage.setData(emptyLeaf);
        bufferManager.markDirty(indexFile, rootPageId);
        bufferManager.unpinPage(indexFile, rootPageId);
    }


}