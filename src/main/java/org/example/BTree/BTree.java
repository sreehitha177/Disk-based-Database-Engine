package org.example.BTree;


import org.example.Rows.Rid;

import java.util.Iterator;

public interface BTree<K extends Comparable<K>, V> {
    /**
     * Inserts a key-value pair into the B+ tree.
     *
     * @param key   The key to insert.
     * @param @rid The value associated with the key.
     */
    void insert(K key, Rid r);

    /**
     * Searches for a value by key in the B+ tree.
     *
     * @param key The key to search for.
     * @return A list of Rids with the required value of the search attribute.
     */
    Iterator<Rid> search(K key);

    /**
     * Performs a range query, retrieving all key-value pairs in the specified range.
     *
     * @param startKey The start of the range (inclusive).
     * @param endKey   The end of the range (inclusive).
     * @return A list of Rids with values of the search attribute that fall within the range.
     */
    Iterator<Rid> rangeSearch(K startKey, K endKey);
}
