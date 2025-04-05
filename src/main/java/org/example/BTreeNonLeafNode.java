//package org.example;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class BTreeNonLeafNode<K extends Comparable<K>> implements BTreeNode {
////    private int index;
//    private List<K>keys;
//    private List<BTreeNode>children;
//    private final int maxKeys;
//
//    public BTreeNonLeafNode(int maxKeys) {
////        this.index = index;
//        this.keys = new ArrayList<>();
//        this.children = new ArrayList<>();
//        this.maxKeys = maxKeys;
//    }
//
//    @Override
//    public boolean isLeaf() {
//        return false;
//    }
//
////    @Override
////    public int getPageId() {
////        return 0;
////    }
//
//    @Override
//    public boolean isFull() {
//        return keys.size() >= maxKeys;
//    }
//
//    // Insert a key and child into the non-leaf node
//    public void insert(K key, BTreeNode child) {
////        int pos = Collections.binarySearch(keys, key);
////        if (pos < 0)
////            pos = -pos - 1; // Find position to insert
////        keys.add(pos, key);
////        children.add(pos + 1, child); // Insert the child pointer at the correct
//
//
//        int insertIndex = 0;
//        while (insertIndex < keys.size() && key.compareTo(keys.get(insertIndex)) > 0) {
//            insertIndex++;
//        }
//
//        System.out.println("Inserting key: " + key + " at index: " + insertIndex);
//        System.out.println("Current keys: " + keys);
//        System.out.println("Current children count: " + children.size());
//
//        // Ensure we do not exceed valid indexes
//        if (insertIndex > keys.size()) {
//            throw new IndexOutOfBoundsException("Invalid insert index: " + insertIndex);
//        }
//
//        keys.add(insertIndex, key);  // Inserting key at the correct position
////        System.out.println("Adding into key failing");
//        System.out.println(child);
//        children.add(insertIndex, child); // Adding child at the right index
////        System.out.println("Adding into children failing");
//    }
//
//    // Get the child node that corresponds to the given key
////    public BTreeNode getChild(K key) {
////        int pos = Collections.binarySearch(keys, key);
////        if (pos < 0) pos = -pos - 1; // Find the correct position
////        return children.get(pos);
////    }
//
//
////    public BTreeNode getChild(K key) {
////        if (children.isEmpty()) {
////            System.out.println("Error: Attempted to get child from an empty node!");
////            return null; // Or throw an exception based on your implementation
////        }
////
////        System.out.println(keys);
////        int pos = Collections.binarySearch(keys, key);
////        if (pos < 0) pos = -pos - 1; // Find the correct position
////
////        // Ensure `pos` is within bounds
////        if (pos >= children.size()) {
////            System.out.println("Warning: Computed child index out of bounds, adjusting to last child.");
////            pos = children.size() - 1;
////        }
////
////        System.out.println("Key: " + key + " - Redirecting to child at index: " + pos);
////        return children.get(pos);
////    }
//
//
//    public BTreeNode getChild(K key) {
//        if (children.isEmpty()) {
//            System.out.println("Error: Attempted to get child from an empty node!");
//            return null; // Or throw an exception if desired
//        }
//
//        System.out.println("Internal node keys: " + keys);
//
//        int pos = Collections.binarySearch(keys, key);
//        if (pos < 0) {
//            pos = -pos - 1; // Get the correct child position
//        } else {
//            pos = pos + 1;  // If key exists, move to the right child
//        }
//
//        // Ensure pos is within valid bounds
//        if (pos >= children.size()) {
//            System.out.println("Warning: Computed child index out of bounds, adjusting to last child.");
//            pos = children.size() - 1;
//        }
//
//        System.out.println("Key: " + key + " -> Redirecting to child at index: " + pos);
//        return children.get(pos);
//    }
//
//
//
//    // Split the node when it becomes full
//    public BTreeNonLeafNode<K> split() {
//        int mid = keys.size() / 2;
//        System.out.println( "Entering the split function");
//        // Create a new non-leaf node for the split
//        BTreeNonLeafNode<K> newNode = new BTreeNonLeafNode<>(maxKeys);
//
//        // Move the second half of the keys and children to the new node
//        newNode.keys.addAll(keys.subList(mid + 1, keys.size()));
//        newNode.children.addAll(children.subList(mid + 1, children.size()));
//
//        // Clear the keys and children that were moved to the new node
//        keys.subList(mid, keys.size()).clear();
//        children.subList(mid + 1, children.size()).clear();
//
//        // Return the new node
//        return newNode;
//    }
//
//    // Get the number of keys in the node
//    public int getKeyCount() {
//        return keys.size();
//    }
//
//    public List<K> getKeys() {
//        return keys;
//    }
//
//    public List<BTreeNode> getChildren() {
//        return children;
//    }
//}



package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BTreeNonLeafNode<K extends Comparable<K>, V> extends BTreeNode<K, V> {
    private List<BTreeNode<K, V>> children;
    private final BTreeImplementation<K, V> tree;

    public BTreeNonLeafNode(BTreeImplementation<K, V> tree) {
        this.tree = tree;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public void addKey(K key) {
        keys.add(key);
    }

    public void addChild(BTreeNode<K, V> child) {
        children.add(child);
    }

    @Override
    public SplitResult<K, V> insert(K key, Rid rid) {
        int index = Collections.binarySearch(keys, key);
        if (index < 0) index = -index - 1;
        SplitResult<K, V> split = children.get(index).insert(key, rid);
        if (split == null) return null;

        keys.add(index, split.splitKey);
        children.add(index + 1, split.rightNode);
        if (keys.size() >= tree.getOrder()) {
            return splitInternal();
        }
        return null;
    }

    @Override
    public Iterator<Rid> search(K key) {
        int index = Collections.binarySearch(keys, key);
        if (index < 0) index = -index - 1;
        return children.get(index).search(key);
    }

    @Override
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        int index = Collections.binarySearch(keys, startKey);
        if (index < 0) index = -index - 1;
        return children.get(index).rangeSearch(startKey, endKey);
    }

    private SplitResult<K, V> splitInternal() {
        int midIndex = keys.size() / 2;
        BTreeNonLeafNode<K, V> rightNode = new BTreeNonLeafNode<>(tree);

        rightNode.keys = new ArrayList<>(keys.subList(midIndex + 1, keys.size()));
        rightNode.children = new ArrayList<>(children.subList(midIndex + 1, children.size()));

        K splitKey = keys.get(midIndex);

        keys = new ArrayList<>(keys.subList(0, midIndex));
        children = new ArrayList<>(children.subList(0, midIndex + 1));

        return new SplitResult<>(splitKey, rightNode);
    }
}