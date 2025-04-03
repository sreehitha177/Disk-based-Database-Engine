package org.example;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;


public class Btreetest{

    @Test
    public void testInsertAndSearch() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(3); // maxEntries = 3

        bTree.insert(10, new Rid(1, 0));
        bTree.insert(20, new Rid(2, 0));
        bTree.insert(30, new Rid(3, 0));
        bTree.insert(40, new Rid(4, 0));

        int k=40;
        List<Rid> result = bTree.search(k);
        if (result.isEmpty()) {
            System.out.println("Test Failed: Key "+k+" not found!");
        } else {
            System.out.println("Test Passed: Key "+k+" found!");
        }
    }

    @Test
    public void testBasicInsertAndSearch() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(3);
        Rid rid1 = new Rid(1, 1);
        Rid rid2 = new Rid(1, 2);

        // Insert some key-value pairs
        bTree.insert(10, rid1);
        bTree.insert(20, rid2);
        bTree.insert(5, new Rid(1, 3));

        // Search for existing keys
        List<Rid> result1 = bTree.search(10);
        assertEquals(1, result1.size());
        assertEquals(rid1, result1.get(0));

        List<Rid> result2 = bTree.search(20);
        assertEquals(1, result2.size());
        assertEquals(rid2, result2.get(0));

        // Search for non-existent key
        List<Rid> result3 = bTree.search(99);
        assertTrue(result3.isEmpty());
    }


    @Test
    public void testMultipleValuesSameKey() {
        BTreeImplementation<String> bTree = new BTreeImplementation<>(4);
        Rid rid1 = new Rid(1, 1);
        Rid rid2 = new Rid(1, 2);
        Rid rid3 = new Rid(2, 1);

        // Insert multiple values for same key
        bTree.insert("apple", rid1);
        bTree.insert("apple", rid2);
        bTree.insert("banana", rid3);
        bTree.insert("apple", new Rid(2, 2));

        // Verify all values for "apple" are returned
        List<Rid> results = bTree.search("apple");
        assertEquals(3, results.size());
        assertTrue(results.contains(rid1));
        assertTrue(results.contains(rid2));

        // Verify single value for "banana"
        List<Rid> bananaResults = bTree.search("banana");
        assertEquals(1, bananaResults.size());
        assertEquals(rid3, bananaResults.get(0));
    }

    @Test
    public void testLeafNodeSplitting2() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(3);
        bTree.insert(10, new Rid(1, 1));
        bTree.insert(20, new Rid(1, 2));
        bTree.insert(30, new Rid(1, 3));
        bTree.insert(40, new Rid(1, 4)); // Causes first split
        bTree.insert(5, new Rid(1, 5));
        bTree.insert(15, new Rid(1, 6));
        bTree.insert(25, new Rid(1, 7)); // Should work now

        assertFalse(bTree.search(25).isEmpty());
    }
    @Test
    public void testLeafNodeSplitting() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(3); // Small capacity to force splits

        // Insert enough elements to cause a split
        bTree.insert(10, new Rid(1, 1));
        bTree.insert(20, new Rid(1, 2));
        bTree.insert(30, new Rid(1, 3));
        bTree.insert(40, new Rid(1, 4)); // Should cause a split

        // Verify all values can still be found
        assertFalse(bTree.search(10).isEmpty());
        assertFalse(bTree.search(20).isEmpty());
        assertFalse(bTree.search(30).isEmpty());
        assertFalse(bTree.search(40).isEmpty());

        // Insert more to cause another split
        bTree.insert(5, new Rid(1, 5));
        bTree.insert(15, new Rid(1, 6));
        bTree.insert(25, new Rid(1, 7)); // Should cause another split

        // Verify all values
        assertEquals(7, bTree.search(10).size() + bTree.search(20).size() +
                bTree.search(30).size() + bTree.search(40).size() +
                bTree.search(5).size() + bTree.search(15).size() +
                bTree.search(25).size());
    }

    @Test
    public void testNonLeafNodeSplitting() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(2); // Very small capacity

        // Insert enough elements to cause multiple splits
        bTree.insert(10, new Rid(1, 1));
        bTree.insert(20, new Rid(1, 2));
        bTree.insert(30, new Rid(1, 3)); // Causes leaf split
        bTree.insert(40, new Rid(1, 4)); // Causes non-leaf split
        bTree.insert(5, new Rid(1, 5));
        bTree.insert(15, new Rid(1, 6));
        bTree.insert(25, new Rid(1, 7));
        bTree.insert(35, new Rid(1, 8)); // Should cause another non-leaf split
        System.out.println("Hellooo");
        // Verify all values
        for (int i = 0; i < 8; i++) {
            int key = 5 + i * 5;
            assertFalse("Key " + key + " not found", bTree.search(key).isEmpty());
        }
    }

//    @Test
//    public void testRangeSearch() {
//        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(4);
//
//        // Insert values
//        for (int i = 0; i < 20; i++) {
//            bTree.insert(i, new Rid(1, i));
//        }
//
//        // Test range search
//        Iterator<Rid> rangeResults = bTree.rangeSearch(5, 10);
//        int count = 0;
//        while (rangeResults.hasNext()) {
//            rangeResults.next();
//            count++;
//        }
//        assertEquals(6, count); // 5,6,7,8,9,10
//
//        // Test range with no results
//        Iterator<Rid> emptyRange = bTree.rangeSearch(50, 60);
//        assertFalse(emptyRange.hasNext());
//    }

    @Test
    public void testConsecutiveDuplicateKeys() {
        BTreeImplementation<String> bTree = new BTreeImplementation<>(4);

        // Insert many duplicates
        for (int i = 0; i < 10; i++) {
            bTree.insert("duplicate", new Rid(1, i));
        }

        // Verify all duplicates are stored
        List<Rid> results = bTree.search("duplicate");
        assertEquals(10, results.size());

        // Verify no false positives
        assertTrue(bTree.search("non-existent").isEmpty());
    }

//    @Test
//    public void testEmptyTree() {
//        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(3);
//
//        // Search on empty tree
//        assertTrue(bTree.search(10).isEmpty());
//
//        // Range search on empty tree
//        Iterator<Rid> rangeResults = bTree.rangeSearch(1, 100);
//        assertFalse(rangeResults.hasNext());
//    }

    @Test
    public void testLargeDataset() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(50);
        int numEntries = 1000;

        // Insert large number of entries
        for (int i = 0; i < numEntries; i++) {
            bTree.insert(i, new Rid(1, i));
        }

        // Verify all can be found
        for (int i = 0; i < numEntries; i++) {
            assertFalse(bTree.search(i).isEmpty());
        }

        // Verify non-existent keys
        assertTrue(bTree.search(-1).isEmpty());
        assertTrue(bTree.search(numEntries + 1).isEmpty());
    }

//    @Test
//    public void testMixedKeyTypes() {
//        BTreeImplementation<Comparable<?>> bTree = new BTreeImplementation<>(4);
//
//        // Insert different types
//        bTree.insert(10, new Rid(1, 1));
//        bTree.insert("apple", new Rid(1, 2));
//        bTree.insert(20, new Rid(1, 3));
//        bTree.insert("banana", new Rid(1, 4));
//
//        // Verify searches
//        assertFalse(bTree.search(10).isEmpty());
//        assertFalse(bTree.search("apple").isEmpty());
//        assertFalse(bTree.search(20).isEmpty());
//        assertFalse(bTree.search("banana").isEmpty());
//    }

    @Test
    public void testEdgeCases() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(2);

        // Insert minimum and maximum integer values
        bTree.insert(Integer.MIN_VALUE, new Rid(1, 1));
        bTree.insert(Integer.MAX_VALUE, new Rid(1, 2));

        // Verify
        assertFalse(bTree.search(Integer.MIN_VALUE).isEmpty());
        assertFalse(bTree.search(Integer.MAX_VALUE).isEmpty());

        // Insert null key (should throw exception)
        assertThrows(NullPointerException.class, () -> {
            bTree.insert(null, new Rid(1, 3));
        });
    }
}