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