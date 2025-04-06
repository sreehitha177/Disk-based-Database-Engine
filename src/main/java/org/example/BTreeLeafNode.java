//package org.example;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.TreeMap;
//
//public class BTreeLeafNode<K> implements BTreeNode {
//    private int index;
//    private TreeMap<K, List<Rid> > entries;
//    private int nextPageIndex;
//
//
//
//    public void setEntries(TreeMap<K, List<Rid>> entries) {
//        this.entries = entries;
//    }
//
//    private final int maxEntries;
//
//    public BTreeLeafNode(int index, int maxEntries) {
//        this.index = index;
//        this.maxEntries = maxEntries;
//        this.entries = new TreeMap<>();
//        this.nextPageIndex = -1;
//    }
//
//
//    @Override
//    public boolean isLeaf() {
//        return true;
//    }
//
//
//    public int getPageId() {
//        // Instead of a direct page ID, return the first record's page ID (if exists)
//        if (!entries.isEmpty()) {
//            List<Rid> firstRids = entries.firstEntry().getValue();
//            if (!firstRids.isEmpty()) {
//                return firstRids.get(0).getPid(); // Extract `pid` from first Rid
//            }
//        }
//        return -1; // No valid page ID if empty
//    }
//
//    @Override
//    public boolean isFull() {
//        return entries.size() >= maxEntries;
//    }
//
//    public void insert(K key, Rid rid) {
//        System.out.println("Inserting key: " + key + " with Rid: " + rid);
//
//        if (!entries.containsKey(key)) {
//            System.out.println("Key " + key + " not found, creating new entry.");
////            entries.put(key, new ArrayList<>());
//        } else {
//            System.out.println("Key " + key + " already exists, appending Rid.");
//        }
//
//        entries.computeIfAbsent(key, k -> {
//            System.out.println("Creating new list for key: " + key);
//            return new ArrayList<>();
//        }).add(rid);
//
//        System.out.println("Current state of entries: " + entries);
//    }
//
//    public List<Rid> search(K key) {
//
//        List<Rid> result = entries.getOrDefault(key, Collections.emptyList());
//
//        if (result.isEmpty()) {
//            System.out.println("Search Key " + key + " not found in leaf node.");
//        } else {
//            System.out.println("Search Key " + key + " found with RIDs: " + result);
//        }
//
//        return result;
//    }
//
//
//
//    public int getNextPageIndex() {
//        return nextPageIndex;
//    }
//
//    public void setNextPageIndex(int nextPageIndex) {
//        this.nextPageIndex = nextPageIndex;
//    }
//    // Split function for leaf node
////    public BTreeLeafNode<K> split() {
////        int midIndex = entries.size() / 2;
////        List<K> keys = new ArrayList<>(entries.keySet());
////        BTreeLeafNode<K> newLeafNode = new BTreeLeafNode<>(index + 1, maxEntries);
////
////        // Move the second half of the keys and entries to the new node
////        for (int i = midIndex; i < keys.size(); i++) {
////            K key = keys.get(i);
////            newLeafNode.entries.put(key, entries.remove(key));
////        }
////
////        newLeafNode.setNextPageIndex(this.nextPageIndex);
////        this.nextPageIndex = newLeafNode.index;
////
////        // Return the key of the middle element (the first key in the second half)
////        return newLeafNode;
////    }
//
//    public BTreeLeafNode<K> split() {
//        System.out.println("Splitting leaf node with index: " + index);
//        int midIndex = entries.size() / 2;
//        List<K> keys = new ArrayList<>(entries.keySet());
//
//        System.out.println("Current keys before split: " + keys);
//        System.out.println("Mid index: " + midIndex);
//
//        BTreeLeafNode<K> newLeafNode = new BTreeLeafNode<>(index + 1, maxEntries);
//
//        // Move the second half of the keys and entries to the new node
//        for (int i = midIndex; i < keys.size(); i++) {
//            K key = keys.get(i);
//            newLeafNode.entries.put(key, entries.remove(key));
//            System.out.println("Moving key: " + key + " to new leaf node with index: " + newLeafNode.index);
//        }
//
//        newLeafNode.setNextPageIndex(this.nextPageIndex);
//        this.nextPageIndex = newLeafNode.index;
//
//        System.out.println("New leaf node created with index: " + newLeafNode.index);
//        System.out.println("Keys in original leaf node after split: " + entries.keySet());
//        System.out.println("Keys in new leaf node: " + newLeafNode.entries.keySet());
//        System.out.println("Updated nextPageIndex: " + this.nextPageIndex);
//
//        // Return the new leaf node
//        return newLeafNode;
//    }
//
//
//
//
//    public TreeMap<K, List<Rid>> getEntries() {
//        return entries;
//    }
//
//
//}



package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BTreeLeafNode<K extends Comparable<K>, V> extends BTreeNode<K, V> {
    private List<Rid> values;
    private BTreeLeafNode<K, V> next;
    private final BTreeImplementation<K, V> tree;

    public BTreeLeafNode(BTreeImplementation<K, V> tree) {
        this.tree = tree;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    @Override
    public SplitResult<K, V> insert(K key, Rid rid) {
        int index = Collections.binarySearch(keys, key);
        if (index < 0) index = -index - 1;
        keys.add(index, key);
        values.add(index, rid);
        if (keys.size() >= tree.getOrder()) {
            return splitLeaf();
        }
        return null;
    }

    @Override
    public Iterator<Rid> search(K key) {
        BTreeLeafNode<K, V> node = this;
        while (node != null) {
            int index = Collections.binarySearch(node.keys, key);
            if (index >= 0) {
                return Collections.singletonList(node.values.get(index)).iterator();
            }
            node = node.next;
        }
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        List<Rid> results = new ArrayList<>();
        BTreeLeafNode<K, V> node = this;

        // Find starting node
        while (node != null && (node.keys.isEmpty() || node.keys.get(node.keys.size() - 1).compareTo(startKey) < 0)) {
            node = node.next;
        }

        // Collect results
        while (node != null) {
            for (int i = 0; i < node.keys.size(); i++) {
                if (node.keys.get(i).compareTo(startKey) >= 0 && node.keys.get(i).compareTo(endKey) <= 0) {
                    results.add(node.values.get(i));
                }
            }
            node = node.next;
        }

        return results.iterator();
    }

    private SplitResult<K, V> splitLeaf() {
        int midIndex = keys.size() / 2;
        BTreeLeafNode<K, V> rightNode = new BTreeLeafNode<>(tree);

        rightNode.keys = new ArrayList<>(keys.subList(midIndex, keys.size()));
        rightNode.values = new ArrayList<>(values.subList(midIndex, values.size()));

        rightNode.next = this.next;
        this.next = rightNode;

        keys.subList(midIndex, keys.size()).clear();
        values.subList(midIndex, values.size()).clear();

        return new SplitResult<>(rightNode.keys.get(0), rightNode);
    }
}