package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BTreeNonLeafNode<K extends Comparable<K>> implements BTreeNode {
//    private int index;
    private List<K>keys;
    private List<BTreeNode>children;
    private final int maxKeys;

    public BTreeNonLeafNode(int maxKeys) {
//        this.index = index;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.maxKeys = maxKeys;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

//    @Override
//    public int getPageId() {
//        return 0;
//    }

    @Override
    public boolean isFull() {
        return keys.size() >= maxKeys;
    }

    // Insert a key and child into the non-leaf node
    public void insert(K key, BTreeNode child) {
//        int pos = Collections.binarySearch(keys, key);
//        if (pos < 0)
//            pos = -pos - 1; // Find position to insert
//        keys.add(pos, key);
//        children.add(pos + 1, child); // Insert the child pointer at the correct


        int insertIndex = 0;
        while (insertIndex < keys.size() && key.compareTo(keys.get(insertIndex)) > 0) {
            insertIndex++;
        }

        System.out.println("Inserting key: " + key + " at index: " + insertIndex);
        System.out.println("Current keys: " + keys);
        System.out.println("Current children count: " + children.size());

        // Ensure we do not exceed valid indexes
        if (insertIndex > keys.size()) {
            throw new IndexOutOfBoundsException("Invalid insert index: " + insertIndex);
        }

        keys.add(insertIndex, key);  // Inserting key at the correct position
        children.add(insertIndex + 1, child); // Adding child at the right index
    }

    // Get the child node that corresponds to the given key
    public BTreeNode getChild(K key) {
        int pos = Collections.binarySearch(keys, key);
        if (pos < 0) pos = -pos - 1; // Find the correct position
        return children.get(pos);
    }

    // Split the node when it becomes full
    public BTreeNonLeafNode<K> split() {
        int mid = keys.size() / 2;

        // Create a new non-leaf node for the split
        BTreeNonLeafNode<K> newNode = new BTreeNonLeafNode<>(maxKeys);

        // Move the second half of the keys and children to the new node
        newNode.keys.addAll(keys.subList(mid + 1, keys.size()));
        newNode.children.addAll(children.subList(mid + 1, children.size()));

        // Clear the keys and children that were moved to the new node
        keys.subList(mid, keys.size()).clear();
        children.subList(mid + 1, children.size()).clear();

        // Return the new node
        return newNode;
    }

    // Get the number of keys in the node
    public int getKeyCount() {
        return keys.size();
    }

    public List<K> getKeys() {
        return keys;
    }

    public List<BTreeNode> getChildren() {
        return children;
    }
}
