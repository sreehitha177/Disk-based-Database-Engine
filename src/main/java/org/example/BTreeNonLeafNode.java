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