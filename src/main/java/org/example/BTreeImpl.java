//package org.example;
//
//
//import java.util.*;
//
//public class BTreeImpl<K extends Comparable<K>, V> implements BTree<K, V> {
//    private final int size; // B+ tree order (max children per node)
//    private Node<K, V> root;
//    private final String indexFile;
//
//    public BTreeImpl(int size, String indexFile) {
//        this.size = size;
//        this.indexFile = indexFile;
//        this.root = new LeafNode<>();
//    }
//
//    @Override
//    public void insert(K key, Rid rid) {
//        SplitResult<K, V> split = root.insert(key, rid);
//        if (split != null) {
//            InternalNode<K, V> newRoot = new InternalNode<>();
//            newRoot.keys.add(split.splitKey);
//            newRoot.children.add(root);
//            newRoot.children.add(split.rightNode);
//            root = newRoot;
//        }
//    }
//
//    @Override
//    public Iterator<Rid> search(K key) {
//        return root.search(key);
//    }
//
//    @Override
//    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
//        return root.rangeSearch(startKey, endKey);
//    }
//
//    // Abstract Node class
//    abstract class Node<K extends Comparable<K>, V> {
//        List<K> keys = new ArrayList<>();
//        abstract SplitResult<K, V> insert(K key, Rid rid);
//        abstract Iterator<Rid> search(K key);
//        abstract Iterator<Rid> rangeSearch(K startKey, K endKey);
//    }
//
//    // Internal Node
//    class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
//        List<Node<K, V>> children = new ArrayList<>();
//
//        @Override
//        SplitResult<K, V> insert(K key, Rid rid) {
//            int index = Collections.binarySearch(keys, key);
//            if (index < 0) index = -index - 1;
//            SplitResult<K, V> split = children.get(index).insert(key, rid);
//            if (split == null) return null;
//            keys.add(index, split.splitKey);
//            children.add(index + 1, split.rightNode);
//            if (keys.size() >= size) return splitInternal();
//            return null;
//        }
//
//        @Override
//        Iterator<Rid> search(K key) {
//            int index = Collections.binarySearch(keys, key);
//            if (index < 0) index = -index - 1;
//            return children.get(index).search(key);
//        }
//
//        @Override
//        Iterator<Rid> rangeSearch(K startKey, K endKey) {
//            int index = Collections.binarySearch(keys, startKey);
//            if (index < 0) index = -index - 1;
//            return children.get(index).rangeSearch(startKey, endKey);
//        }
//
//        private SplitResult<K, V> splitInternal() {
//            int midIndex = keys.size() / 2;
//            InternalNode<K, V> rightNode = new InternalNode<>();
//
//            rightNode.keys = new ArrayList<>(keys.subList(midIndex + 1, keys.size()));
//            rightNode.children = new ArrayList<>(children.subList(midIndex + 1, children.size()));
//
//            K splitKey = keys.get(midIndex);
//
//            keys = new ArrayList<>(keys.subList(0, midIndex));
//            children = new ArrayList<>(children.subList(0, midIndex + 1));
//
//            return new SplitResult<>(splitKey, rightNode);
//        }
//    }
//
//    // Leaf Node
//    class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
//        List<Rid> values = new ArrayList<>();
//        LeafNode<K, V> next;
//
//        @Override
//        SplitResult<K, V> insert(K key, Rid rid) {
//            int index = Collections.binarySearch(keys, key);
//            if (index < 0) index = -index - 1;
//            keys.add(index, key);
//            values.add(index, rid);
//            if (keys.size() >= size) return splitLeaf();
//            return null;
//        }
//
//        @Override
//        Iterator<Rid> search(K key) {
//            LeafNode<K, V> node = this;
//            while (node != null) {
//                int index = Collections.binarySearch(node.keys, key);
//                if (index >= 0) {
//                    return Collections.singletonList(node.values.get(index)).iterator();
//                }
//                node = node.next; // Move to next leaf node if split
//            }
//            return Collections.emptyIterator();
//        }
//
//        @Override
//        Iterator<Rid> rangeSearch(K startKey, K endKey) {
//            List<Rid> results = new ArrayList<>();
//            LeafNode<K, V> node = this;
//
//            // Find starting node
//            while (node != null && (node.keys.isEmpty() || node.keys.get(node.keys.size() - 1).compareTo(startKey) < 0)) {
//                node = node.next;
//            }
//
//            // Collect results
//            while (node != null) {
//                for (int i = 0; i < node.keys.size(); i++) {
//                    if (node.keys.get(i).compareTo(startKey) >= 0 && node.keys.get(i).compareTo(endKey) <= 0) {
//                        results.add(node.values.get(i));
//                    }
//                }
//                node = node.next;
//            }
//
//            return results.iterator();
//        }
//
//        private SplitResult<K, V> splitLeaf() {
//            int midIndex = keys.size() / 2;
//            LeafNode<K, V> rightNode = new LeafNode<>();
//
//            rightNode.keys = new ArrayList<>(keys.subList(midIndex, keys.size()));
//            rightNode.values = new ArrayList<>(values.subList(midIndex, values.size()));
//
//            rightNode.next = this.next;
//            this.next = rightNode;
//
//            keys.subList(midIndex, keys.size()).clear();
//            values.subList(midIndex, values.size()).clear();
//
//            return new SplitResult<>(rightNode.keys.get(0), rightNode);
//        }
//    }
//
//    // Split result helper class
//    class SplitResult<K extends Comparable<K>, V> {
//        K splitKey;
//        Node<K, V> rightNode;
//
//        SplitResult(K splitKey, Node<K, V> rightNode) {
//            this.splitKey = splitKey;
//            this.rightNode = rightNode;
//        }
//    }
//}
