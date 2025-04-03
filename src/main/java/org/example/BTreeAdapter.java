package org.example;

import java.io.File;
import java.util.Iterator;

/**
 * An adapter class that implements the BTree interface
 * using the BTreeBufferManagerImpl as the underlying implementation
 */
public class BTreeAdapter<K extends Comparable<K>> implements BTree<K, Rid> {
    private BTreeBufferManagerImpl<K> btreeImpl;
    
    /**
     * Creates a new BTreeAdapter for a specific index
     * @param bufferManager The buffer manager to use
     * @param indexFileId The file ID of the index
     * @param order The order of the B+ tree
     * @param isStringKey Whether the key is a string type
     */
    public BTreeAdapter(BufferManagerImplementation bufferManager, 
                         String indexFileId, 
                         int order,
                         boolean isStringKey) {
        System.out.println("Creating BTreeAdapter for index: " + indexFileId);
        // Ensure the index file path is absolute
        File indexFile = new File(indexFileId);
        String absolutePath = indexFile.getAbsolutePath();
        this.btreeImpl = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, order, isStringKey);
    }
    
    @Override
    public void insert(K key, Rid r) {
        System.out.println("BTreeAdapter: Inserting key: " + key + " with RID: [" + r.getPid() + "," + r.getSid() + "]");
        btreeImpl.insert(key, r);
    }
    
    @Override
    public Iterator<Rid> search(K key) {
        System.out.println("BTreeAdapter: Searching for key: " + key);
        return btreeImpl.search(key);
    }
    
    @Override
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        System.out.println("BTreeAdapter: Range searching from: " + startKey + " to: " + endKey);
        return btreeImpl.rangeSearch(startKey, endKey);
    }
}