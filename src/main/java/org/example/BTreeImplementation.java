//package org.example;
//
//import java.util.List;
//
//public class BTreeImplementation<K extends Comparable<K>> {
//    private BTreeNode root;
//    private final int maxEntries;
//
//    public BTreeImplementation(int maxEntries) {
//        this.maxEntries = maxEntries;
//        this.root = new BTreeLeafNode<>(0, maxEntries); // Start with an empty root node
//    }
//
//    public void insert(K key, Rid rid) {
//        BTreeNode currentNode = root;
//
//        if (currentNode.isLeaf()) {
//            BTreeLeafNode<K> leafNode = (BTreeLeafNode<K>) currentNode;
//            leafNode.insert(key, rid);
//
//            if (leafNode.isFull()) {
//                BTreeLeafNode<K> newLeafNode = leafNode.split();
//                BTreeNonLeafNode<K> newRoot = new BTreeNonLeafNode<>(maxEntries);
//                newRoot.insert(newLeafNode.getEntries().firstKey(), newLeafNode);
//                newRoot.getChildren().add(0, leafNode);
//                this.root = newRoot;
//            }
//        } else {
//            insertNonLeaf(key, rid, null, null);
//        }
//    }
//
//
////    public void insert(K key, Rid rid) {
////        System.out.println("Attempting to insert key: " + key);
////
////        if (root == null) {
////            root = new BTreeLeafNode<>();
////        }
////
////        BTreeLeafNode<K> leaf = findLeafNode(key);
////        leaf.insert(key, rid);
////
////        // If leaf overflows, split and propagate upwards
////        if (leaf.isOverfull()) {
////            splitLeafNode(leaf);
////        }
////    }
//
//
//    private void insertNonLeaf(K key, Rid rid, BTreeNode parent, BTreeNonLeafNode<K> parentNode) {
//        BTreeNode currentNode = root;
//        while (!currentNode.isLeaf()) {
//            BTreeNonLeafNode<K> nonLeafNode = (BTreeNonLeafNode<K>) currentNode;
//            BTreeNode childNode = nonLeafNode.getChild(key);
//
//            if (childNode.isFull()) {
//                System.out.println(childNode);
////                BTreeNonLeafNode<K> nonleafchild = ((BTreeNonLeafNode<K>) childNode);
////                BTreeNonLeafNode<K> newNonLeafNode = nonleafchild.split();
////                BTreeNonLeafNode<K> newNonLeafNode = ((BTreeNonLeafNode<K>) childNode).split();
//                BTreeNode newNode;
//                if (childNode instanceof BTreeNonLeafNode) {
//                    // If the child is a non-leaf node, split it as a non-leaf
//                    BTreeNonLeafNode<K> nonLeafChild = (BTreeNonLeafNode<K>) childNode;
//                    newNode = nonLeafChild.split();
//                } else if (childNode instanceof BTreeLeafNode) {
//                    // If the child is a leaf node, split it as a leaf
//                    BTreeLeafNode<K> leafChild = (BTreeLeafNode<K>) childNode;
//                    newNode = leafChild.split();
//                } else {
//                    // Should never reach here, but just in case
//                    System.err.println("Unexpected node type: " + childNode.getClass().getName());
//                    return;
//                }
//                nonLeafNode.insert(key, newNode);
//                insertIntoParent(nonLeafNode, newNode);
//                return;
//            }
//            currentNode = childNode;
//        }
//        ((BTreeLeafNode<K>) currentNode).insert(key, rid);
//    }
//
//
//        // Find the parent of the old node
////        BTreeNode<K> parentNode = findParentNode(root, oldNode);
////
////        if (parentNode != null && parentNode instanceof BTreeNonLeafNode) {
////            BTreeNonLeafNode<K> nonLeafNode = (BTreeNonLeafNode<K>) parentNode;
////
////            // Insert the newNode into the parent node
////            K keyToInsert = newNode instanceof BTreeLeafNode
////                    ? ((BTreeLeafNode<K>) newNode).getEntries().firstKey()  // For leaf node, use first key
////                    : ((BTreeNonLeafNode<K>) newNode).getKeys().get(0);    // For non-leaf node, use the first key
////
////            nonLeafNode.insert(keyToInsert, newNode);
////
////            // If the parent node is full, split it
////            if (nonLeafNode.isFull()) {
////                BTreeNonLeafNode<K> newParentNode = nonLeafNode.split();
////                insertIntoParent(nonLeafNode, newParentNode);
////            }
////        }
//        // A generic insert into parent method that can handle both leaf and non-leaf nodes.
//    private void insertIntoParent(BTreeNode<K> oldNode, BTreeNode<K> newNode) {
//        if (root == oldNode) {
//            BTreeNonLeafNode<K> newRoot = new BTreeNonLeafNode<>(maxEntries);
//
//            newRoot.insert(newNode instanceof BTreeLeafNode
//                    ? ((BTreeLeafNode<K>) newNode).getEntries().firstKey()
//                    : ((BTreeNonLeafNode<K>) newNode).getKeys().get(0), newNode);
//
//            newRoot.getChildren().add(0, oldNode);
//            this.root = newRoot;
//            return;
//        }
//    }
//
//
//
//    private BTreeNode findParentNode(BTreeNode currentNode, BTreeNode childNode) {
//        // Traversal logic to find the parent node of the given childNode
//        if (currentNode.isLeaf()) {
//            return null; // Leaf nodes do not have children, so no parent can be found here.
//        }
//
//        BTreeNonLeafNode<K> nonLeafNode = (BTreeNonLeafNode<K>) currentNode;
//
//        // Iterate through the children of the current node
//        for (BTreeNode<K> child : nonLeafNode.getChildren()) {
//            if (child == childNode) {
//                return nonLeafNode; // Found the parent!
//            }
//
//            // Recursively check in the subtree
//            BTreeNode<K> potentialParent = findParentNode(child, childNode);
//            if (potentialParent != null) {
//                return potentialParent; // If found in recursion, return immediately.
//            }
//        }
//        return null; // You need to implement this logic as needed
//    }
//
////    public List<Rid> search(K key) {
////        BTreeNode currentNode = root;
////        if (currentNode.isLeaf()) {
////            BTreeLeafNode<K> leafNode = (BTreeLeafNode<K>) currentNode;
////            return leafNode.search(key);
////        }
////        return null; // Handle non-leaf search logic here
////    }
////    public List<Rid> search(K key) {
////        BTreeNode currentNode = root;
////        while (!currentNode.isLeaf()) {
////            BTreeNonLeafNode<K> nonLeafNode = (BTreeNonLeafNode<K>) currentNode;
////            currentNode = nonLeafNode.getChild(key);
////        }
////        return ((BTreeLeafNode<K>) currentNode).search(key);
////    }
//
//
//    public List<Rid> search(K key) {
//        System.out.println("Starting search for key: " + key);
//
//        BTreeNode currentNode = root;
//
//        // Traverse down the tree until a leaf node is reached
//        while (!currentNode.isLeaf()) {
//            BTreeNonLeafNode<K> nonLeafNode = (BTreeNonLeafNode<K>) currentNode;
//            System.out.println("Current node is a non-leaf node. Searching for child containing key: " + key);
//
//            currentNode = nonLeafNode.getChild(key);
//
//            System.out.println("Moving to child node...");
//        }
//
//        System.out.println("Reached leaf node. Searching for key: " + key);
//        List<Rid> result = ((BTreeLeafNode<K>) currentNode).search(key);
//
//        if (result.isEmpty()) {
//            System.out.println("Key " + key + " not found in the leaf node.");
//        } else {
//            System.out.println("Key " + key + " found with values: " + result);
//        }
//
//        return result;
//    }
//
//}



package org.example;

import java.util.Iterator;

public class BTreeImplementation<K extends Comparable<K>, V> implements BTree<K, V> {
    private final int order;
    private final String indexFile;
    private BTreeNode<K, V> root;

    public BTreeImplementation(int order, String indexFile) {
        this.order = order;
        this.indexFile = indexFile;
        this.root = new BTreeLeafNode<>(this);
    }

    @Override
    public void insert(K key, Rid rid) {
        BTreeNode.SplitResult<K, V> split = root.insert(key, rid);
        if (split != null) {
            BTreeNonLeafNode<K, V> newRoot = new BTreeNonLeafNode<>(this);
            newRoot.addKey(split.splitKey);
            newRoot.addChild(root);
            newRoot.addChild(split.rightNode);
            root = newRoot;
        }
    }

    @Override
    public Iterator<Rid> search(K key) {
        return root.search(key);
    }

    @Override
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        return root.rangeSearch(startKey, endKey);
    }

    public int getOrder() {
        return order;
    }
}