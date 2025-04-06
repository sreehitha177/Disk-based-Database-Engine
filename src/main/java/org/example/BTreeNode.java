//package org.example;
//
//public interface BTreeNode<K> {
//    boolean isLeaf();
////    int getPageId();
//    boolean isFull();
//}


package org.example;

import java.util.Iterator;
import java.util.List;

public abstract class BTreeNode<K extends Comparable<K>, V> {
    protected List<K> keys;

    public abstract SplitResult<K, V> insert(K key, Rid rid);
    public abstract Iterator<Rid> search(K key);
    public abstract Iterator<Rid> rangeSearch(K startKey, K endKey);

    public static class SplitResult<K extends Comparable<K>, V> {
        public final K splitKey;
        public final BTreeNode<K, V> rightNode;

        public SplitResult(K splitKey, BTreeNode<K, V> rightNode) {
            this.splitKey = splitKey;
            this.rightNode = rightNode;
        }
    }
}