package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class BTreeLeafNode<K> implements BTreeNode {
    private int index;
    private TreeMap<K, List<Rid> > entries;
    private int nextPageIndex;



    public void setEntries(TreeMap<K, List<Rid>> entries) {
        this.entries = entries;
    }

    private final int maxEntries;

    public BTreeLeafNode(int index, int maxEntries) {
        this.index = index;
        this.maxEntries = maxEntries;
        this.entries = new TreeMap<>();
        this.nextPageIndex = -1;
    }


    @Override
    public boolean isLeaf() {
        return true;
    }


    public int getPageId() {
        // Instead of a direct page ID, return the first record's page ID (if exists)
        if (!entries.isEmpty()) {
            List<Rid> firstRids = entries.firstEntry().getValue();
            if (!firstRids.isEmpty()) {
                return firstRids.get(0).getPid(); // Extract `pid` from first Rid
            }
        }
        return -1; // No valid page ID if empty
    }

    @Override
    public boolean isFull() {
        return entries.size() >= maxEntries;
    }

    public void insert(K key, Rid rid) {
        System.out.println("Inserting key: " + key + " with Rid: " + rid);

        if (!entries.containsKey(key)) {
            System.out.println("Key " + key + " not found, creating new entry.");
        } else {
            System.out.println("Key " + key + " already exists, appending Rid.");
        }

        entries.computeIfAbsent(key, k -> {
            System.out.println("Creating new list for key: " + key);
            return new ArrayList<>();
        }).add(rid);

        System.out.println("Current state of entries: " + entries);
    }

    public List<Rid> search(K key) {
        return entries.getOrDefault(key, Collections.emptyList());
    }


    public int getNextPageIndex() {
        return nextPageIndex;
    }

    public void setNextPageIndex(int nextPageIndex) {
        this.nextPageIndex = nextPageIndex;
    }
    // Split function for leaf node
    public BTreeLeafNode<K> split() {
        int midIndex = entries.size() / 2;
        List<K> keys = new ArrayList<>(entries.keySet());
        BTreeLeafNode<K> newLeafNode = new BTreeLeafNode<>(index + 1, maxEntries);

        // Move the second half of the keys and entries to the new node
        for (int i = midIndex; i < keys.size(); i++) {
            K key = keys.get(i);
            newLeafNode.entries.put(key, entries.remove(key));
        }

        newLeafNode.setNextPageIndex(this.nextPageIndex);
        this.nextPageIndex = newLeafNode.index;

        // Return the key of the middle element (the first key in the second half)
        return newLeafNode;
    }



    public TreeMap<K, List<Rid>> getEntries() {
        return entries;
    }


}
