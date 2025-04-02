package org.example;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An implementation of BTree that uses BufferManager for storage
 */
public class BTreeBufferManagerImpl<K extends Comparable<K>> {
    private BufferManagerImplementation bufferManager;
    private String indexFileId;
    private int rootPageId;
    private int order; // B+ tree order (max number of keys in a node)
    private boolean isStringKey; // Flag to indicate if keys are strings
    
    /**
     * Creates a new B+ tree backed by the buffer manager
     * @param bufferManager The buffer manager to use
     * @param indexFileId The file ID where the index is stored
     * @param order The order of the B+ tree (max number of keys per node)
     * @param isStringKey Whether the key is a string type
     */
    public BTreeBufferManagerImpl(BufferManagerImplementation bufferManager, 
                                  String indexFileId, 
                                  int order,
                                  boolean isStringKey) {
        System.out.println("Creating new B+ tree with file ID: " + indexFileId + " and order: " + order);
        this.bufferManager = bufferManager;
        this.indexFileId = indexFileId;
        this.order = order;
        this.isStringKey = isStringKey;
        this.rootPageId = createRootPage();
    }
    
    /**
     * Creates a root page for the B+ tree
     * @return The page ID of the new root page
     */
    private int createRootPage() {
        System.out.println("Creating root page for B+ tree in file: " + indexFileId);
        Page page = bufferManager.createPage(indexFileId);
        
        if (page == null) {
            throw new RuntimeException("Failed to create root page");
        }
        
        int pageId = page.getPid();
        
        // Initialize as an empty leaf node
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        buffer.put((byte) 1); // isLeaf = true
        buffer.putInt(0); // numKeys = 0
        buffer.putInt(-1); // nextPageId = -1 (no next page)
        
        bufferManager.markDirty(indexFileId, pageId);
        bufferManager.unpinPage(indexFileId, pageId);
        
        System.out.println("Created root page with ID: " + pageId);
        return pageId;
    }
    
    public void insert(K key, Rid rid) {
        System.out.println("Inserting key: " + key + " with RID: [" + rid.getPid() + "," + rid.getSid() + "]");
        
        // Start at the root
        int currentPageId = rootPageId;
        List<Integer> path = new ArrayList<>(); // Track the path from root to leaf
        
        // Traverse to the leaf node
        while (!isLeafPage(currentPageId)) {
            path.add(currentPageId);
            currentPageId = findChildPageId(currentPageId, key);
        }
        
        // Insert into the leaf node
        boolean needsSplit = insertIntoLeaf(currentPageId, key, rid);
        
        // If the leaf node was split, we need to insert the new key into the parent
        if (needsSplit) {
            System.out.println("Leaf node split required, updating parent nodes");
            handleSplit(path, currentPageId, key);
        }
    }
    
    /**
     * Checks if a page is a leaf node
     * @param pageId The page ID to check
     * @return True if the page is a leaf node, false otherwise
     */
    private boolean isLeafPage(int pageId) {
        Page page = bufferManager.getPage(indexFileId, pageId);
        
        if (page == null) {
            throw new RuntimeException("Failed to load page: " + pageId);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        boolean isLeaf = buffer.get() == 1;
        
        bufferManager.unpinPage(indexFileId, pageId);
        
        return isLeaf;
    }
    
    /**
     * Finds the child page ID that would contain the given key
     * @param pageId The current page ID
     * @param key The key to search for
     * @return The child page ID
     */
    private int findChildPageId(int pageId, K key) {
        Page page = bufferManager.getPage(indexFileId, pageId);
        
        if (page == null) {
            throw new RuntimeException("Failed to load page: " + pageId);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        buffer.get(); // Skip isLeaf flag
        int numKeys = buffer.getInt();
        
        // Find the right child pointer
        int childPageId = -1;
        for (int i = 0; i < numKeys; i++) {
            K currentKey = readKey(buffer);
            int currentChildId = buffer.getInt();
            
            if (key.compareTo(currentKey) < 0) {
                childPageId = currentChildId;
                break;
            }
            
            // If this is the last key, use the last child pointer
            if (i == numKeys - 1) {
                childPageId = buffer.getInt(); // Read the last child pointer
            }
        }
        
        bufferManager.unpinPage(indexFileId, pageId);
        
        if (childPageId == -1) {
            throw new RuntimeException("Failed to find child page for key: " + key);
        }
        
        return childPageId;
    }
    
    /**
     * Inserts a key-RID pair into a leaf node
     * @param pageId The leaf node page ID
     * @param key The key to insert
     * @param rid The RID to insert
     * @return True if the node was split, false otherwise
     */
    private boolean insertIntoLeaf(int pageId, K key, Rid rid) {
        Page page = bufferManager.getPage(indexFileId, pageId);
        
        if (page == null) {
            throw new RuntimeException("Failed to load page: " + pageId);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        buffer.get(); // Skip isLeaf flag
        int numKeys = buffer.getInt();
        int nextPageId = buffer.getInt();
        
        // Check if we need to split
        if (numKeys >= order) {
            System.out.println("Leaf node is full, performing split");
            splitLeafNode(pageId, key, rid);
            bufferManager.unpinPage(indexFileId, pageId);
            return true;
        }
        
        // Find the position to insert
        int insertPos = 0;
        List<K> keys = new ArrayList<>();
        List<Rid> rids = new ArrayList<>();
        
        // Read existing keys and RIDs
        for (int i = 0; i < numKeys; i++) {
            K currentKey = readKey(buffer);
            keys.add(currentKey);
            
            int currentPid = buffer.getInt();
            int currentSid = buffer.getInt();
            rids.add(new Rid(currentPid, currentSid));
            
            if (key.compareTo(currentKey) > 0) {
                insertPos = i + 1;
            }
        }
        
        // Insert the new key and RID
        keys.add(insertPos, key);
        rids.add(insertPos, rid);
        
        // Write back to page
        buffer.position(0);
        buffer.put((byte) 1); // isLeaf = true
        buffer.putInt(numKeys + 1); // Updated number of keys
        buffer.putInt(nextPageId); // Keep the same next page ID
        
        // Write all keys and RIDs
        for (int i = 0; i < keys.size(); i++) {
            writeKey(buffer, keys.get(i));
            buffer.putInt(rids.get(i).getPid());
            buffer.putInt(rids.get(i).getSid());
        }
        
        bufferManager.markDirty(indexFileId, pageId);
        bufferManager.unpinPage(indexFileId, pageId);
        
        System.out.println("Inserted key: " + key + " into leaf node: " + pageId);
        return false;
    }
    
    /**
     * Splits a leaf node and redistributes the keys
     * @param pageId The leaf node page ID to split
     * @param newKey The new key to insert
     * @param newRid The new RID to insert
     * @return The page ID of the new leaf node and the first key in it
     */
    private Object[] splitLeafNode(int pageId, K newKey, Rid newRid) {
        Page page = bufferManager.getPage(indexFileId, pageId);
        
        if (page == null) {
            throw new RuntimeException("Failed to load page: " + pageId);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        buffer.get(); // Skip isLeaf flag
        int numKeys = buffer.getInt();
        int nextPageId = buffer.getInt();
        
        // Read all keys and RIDs
        List<K> keys = new ArrayList<>();
        List<Rid> rids = new ArrayList<>();
        
        for (int i = 0; i < numKeys; i++) {
            K currentKey = readKey(buffer);
            keys.add(currentKey);
            
            int currentPid = buffer.getInt();
            int currentSid = buffer.getInt();
            rids.add(new Rid(currentPid, currentSid));
        }
        
        // Add the new key and RID
        int insertPos = 0;
        while (insertPos < keys.size() && newKey.compareTo(keys.get(insertPos)) > 0) {
            insertPos++;
        }
        
        keys.add(insertPos, newKey);
        rids.add(insertPos, newRid);
        
        // Create a new leaf node
        Page newPage = bufferManager.createPage(indexFileId);
        
        if (newPage == null) {
            throw new RuntimeException("Failed to create new leaf node");
        }
        
        int newPageId = newPage.getPid();
        
        // Calculate split point
        int splitPoint = (keys.size() + 1) / 2;
        
        // Update the original node
        buffer.position(0);
        buffer.put((byte) 1); // isLeaf = true
        buffer.putInt(splitPoint); // Number of keys in first half
        buffer.putInt(newPageId); // Set next page to the new page
        
        for (int i = 0; i < splitPoint; i++) {
            writeKey(buffer, keys.get(i));
            buffer.putInt(rids.get(i).getPid());
            buffer.putInt(rids.get(i).getSid());
        }
        
        // Initialize the new node
        ByteBuffer newBuffer = ByteBuffer.wrap(newPage.getData());
        newBuffer.put((byte) 1); // isLeaf = true
        newBuffer.putInt(keys.size() - splitPoint); // Number of keys in second half
        newBuffer.putInt(nextPageId); // Use original node's next page
        
        for (int i = splitPoint; i < keys.size(); i++) {
            writeKey(newBuffer, keys.get(i));
            buffer.putInt(rids.get(i).getPid());
            buffer.putInt(rids.get(i).getSid());
        }
        
        bufferManager.markDirty(indexFileId, pageId);
        bufferManager.markDirty(indexFileId, newPageId);
        bufferManager.unpinPage(indexFileId, pageId);
        bufferManager.unpinPage(indexFileId, newPageId);
        
        System.out.println("Split leaf node: " + pageId + " into new node: " + newPageId);
        
        // Return the new page ID and the first key in the new page
        return new Object[] { newPageId, keys.get(splitPoint) };
    }
    
    private void handleSplit(List<Integer> path, int splitPageId, K splitKey) {
        if (path.isEmpty()) {
            // Need to create a new root
            System.out.println("Creating new root node");
            createNewRoot(splitPageId, splitKey);
            return;
        }
        
        // Handle internal node updates
        int parentId = path.get(path.size() - 1);
        System.out.println("Updating parent node: " + parentId + " with split key: " + splitKey);
        
        Page parentPage = bufferManager.getPage(indexFileId, parentId);
        if (parentPage == null) {
            throw new RuntimeException("Failed to load parent page: " + parentId);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(parentPage.getData());
        boolean isLeaf = buffer.get() == 1;
        if (isLeaf) {
            throw new RuntimeException("Parent node is leaf, this should not happen");
        }
        
        int numKeys = buffer.getInt();
        
        // If parent is also full, need to split it recursively
        if (numKeys >= order - 1) {
            bufferManager.unpinPage(indexFileId, parentId);
            // Split parent and continue up the tree
            // This would require implementing more complex internal node split logic
            // For now, let's assume parents don't get full
            System.out.println("Parent node is full, would need to split (not implemented)");
            return;
        }
        
        // Read existing keys and child pointers
        List<K> keys = new ArrayList<>();
        List<Integer> childPointers = new ArrayList<>();
        
        for (int i = 0; i < numKeys; i++) {
            K currentKey = readKey(buffer);
            keys.add(currentKey);
            childPointers.add(buffer.getInt());
        }
        // Read the last child pointer
        childPointers.add(buffer.getInt());
        
        // Find position to insert new key
        int insertPos = 0;
        while (insertPos < keys.size() && splitKey.compareTo(keys.get(insertPos)) > 0) {
            insertPos++;
        }
        
        // Insert the new key and child pointer
        keys.add(insertPos, splitKey);
        childPointers.add(insertPos + 1, splitPageId);
        
        // Write back to page
        buffer.position(0);
        buffer.put((byte) 0); // isLeaf = false
        buffer.putInt(numKeys + 1);
        
        for (int i = 0; i < keys.size(); i++) {
            writeKey(buffer, keys.get(i));
            buffer.putInt(childPointers.get(i));
        }
        buffer.putInt(childPointers.get(childPointers.size() - 1));
        
        bufferManager.markDirty(indexFileId, parentId);
        bufferManager.unpinPage(indexFileId, parentId);
        
        System.out.println("Updated parent node: " + parentId + " with new key and child pointer");
    }
    
    /**
 * Creates a new root node when the tree height increases
 * @param childId The child page ID
 * @param key The key to insert
 */
private void createNewRoot(int childId, K splitKey) {
    System.out.println("Actually creating new root node");
    
    // Get the split child info
    Page childPage = bufferManager.getPage(indexFileId, childId);
    if (childPage == null) {
        throw new RuntimeException("Failed to load child page: " + childId);
    }
    
    ByteBuffer childBuffer = ByteBuffer.wrap(childPage.getData());
    boolean isLeaf = childBuffer.get() == 1;
    bufferManager.unpinPage(indexFileId, childId);
    
    // Create a new node for the new root
    Page newRootPage = bufferManager.createPage(indexFileId);
    if (newRootPage == null) {
        throw new RuntimeException("Failed to create new root page");
    }
    
    int newRootId = newRootPage.getPid();
    
    // Initialize as non-leaf node
    ByteBuffer buffer = ByteBuffer.wrap(newRootPage.getData());
    buffer.put((byte) 0); // isLeaf = false
    buffer.putInt(1); // One key (the splitKey)
    
    // Add the splitKey
    writeKey(buffer, splitKey);
    
    // Add child pointers
    buffer.putInt(rootPageId); // First child is old root
    buffer.putInt(childId);    // Second child is new split node
    
    bufferManager.markDirty(indexFileId, newRootId);
    bufferManager.unpinPage(indexFileId, newRootId);
    
    // Update the root page ID
    rootPageId = newRootId;
    
    System.out.println("New root created with ID: " + newRootId);
 }
    
    public Iterator<Rid> search(K key) {
        System.out.println("Searching for key: " + key);
        
        // Find the leaf node that would contain the key
        int currentPageId = rootPageId;
        
        while (!isLeafPage(currentPageId)) {
            currentPageId = findChildPageId(currentPageId, key);
        }
        
        // Search in the leaf node
        List<Rid> results = searchInLeaf(currentPageId, key);
        
        return results.iterator();
    }
    
    /**
     * Searches for a key in a leaf node
     * @param pageId The leaf node page ID
     * @param key The key to search for
     * @return A list of RIDs matching the key
     */
    private List<Rid> searchInLeaf(int pageId, K key) {
        Page page = bufferManager.getPage(indexFileId, pageId);
        List<Rid> results = new ArrayList<>();
        
        if (page == null) {
            throw new RuntimeException("Failed to load page: " + pageId);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(page.getData());
        buffer.get(); // Skip isLeaf flag
        int numKeys = buffer.getInt();
        buffer.getInt(); // Skip nextPageId
        
        // Search for the key
        for (int i = 0; i < numKeys; i++) {
            K currentKey = readKey(buffer);
            
            int currentPid = buffer.getInt();
            int currentSid = buffer.getInt();
            
            if (currentKey.equals(key)) {
                results.add(new Rid(currentPid, currentSid));
            }
        }
        
        bufferManager.unpinPage(indexFileId, pageId);
        
        System.out.println("Found " + results.size() + " results for key: " + key);
        return results;
    }
    
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        System.out.println("Performing range search from: " + startKey + " to: " + endKey);
        
        return new RangeIterator(startKey, endKey);
    }
    
    /**
     * Iterator for range queries
     */
    private class RangeIterator implements Iterator<Rid> {
        private K startKey;
        private K endKey;
        private int currentPageId;
        private int currentPos;
        private List<K> currentKeys;
        private List<Rid> currentRids;
        private boolean initialized;
        
        public RangeIterator(K startKey, K endKey) {
            this.startKey = startKey;
            this.endKey = endKey;
            this.initialized = false;
            this.currentKeys = new ArrayList<>();
            this.currentRids = new ArrayList<>();
            this.currentPos = 0;
        }
        
        private void initialize() {
            // Find the leaf node that would contain the start key
            currentPageId = rootPageId;
            
            while (!isLeafPage(currentPageId)) {
                currentPageId = findChildPageId(currentPageId, startKey);
            }
            
            loadNextPage();
            initialized = true;
            
            // Position at or after the start key
            while (currentPos < currentKeys.size() && 
                   currentKeys.get(currentPos).compareTo(startKey) < 0) {
                currentPos++;
            }
        }
        
        private void loadNextPage() {
            Page page = bufferManager.getPage(indexFileId, currentPageId);
            
            if (page == null) {
                throw new RuntimeException("Failed to load page: " + currentPageId);
            }
            
            ByteBuffer buffer = ByteBuffer.wrap(page.getData());
            buffer.get(); // Skip isLeaf flag
            int numKeys = buffer.getInt();
            int nextPageId = buffer.getInt();
            
            currentKeys.clear();
            currentRids.clear();
            
            for (int i = 0; i < numKeys; i++) {
                K key = readKey(buffer);
                currentKeys.add(key);
                
                int pid = buffer.getInt();
                int sid = buffer.getInt();
                currentRids.add(new Rid(pid, sid));
            }
            
            currentPageId = nextPageId;
            currentPos = 0;
            
            bufferManager.unpinPage(indexFileId, page.getPid());
        }
        
        @Override
        public boolean hasNext() {
            if (!initialized) {
                initialize();
            }
            
            while (currentPos >= currentKeys.size() && currentPageId != -1) {
                loadNextPage();
            }
            
            return currentPos < currentKeys.size() && 
                   currentKeys.get(currentPos).compareTo(endKey) <= 0;
        }
        
        @Override
        public Rid next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            return currentRids.get(currentPos++);
        }
    }
    
    /**
     * Reads a key from the buffer based on the key type
     * @param buffer The buffer to read from
     * @return The key
     */
    @SuppressWarnings("unchecked")
    private K readKey(ByteBuffer buffer) {
        if (isStringKey) {
            int length = buffer.getInt();
            byte[] bytes = new byte[length];
            buffer.get(bytes);
            return (K) new String(bytes);
        } else {
            // Assume Integer for now
            return (K) Integer.valueOf(buffer.getInt());
        }
    }
    
    /**
     * Writes a key to the buffer based on the key type
     * @param buffer The buffer to write to
     * @param key The key to write
     */
    private void writeKey(ByteBuffer buffer, K key) {
        if (isStringKey) {
            byte[] bytes = key.toString().getBytes();
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        } else {
            // Assume Integer for now
            buffer.putInt(((Integer) key).intValue());
        }
    }
}