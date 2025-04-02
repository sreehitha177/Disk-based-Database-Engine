package org.example;

public interface BTreeNode<K> {
    boolean isLeaf();
//    int getPageId();
    boolean isFull();
}