package org.example;

import org.junit.Test;
import java.util.Iterator;
import static org.junit.Assert.*;

public class Btreetest {

    @Test
    public void testInsertAndSearch() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(3, "test.idx");
        bTree.insert(10, new Rid(1, 0));
        bTree.insert(20, new Rid(2, 0));
        bTree.insert(30, new Rid(3, 0));
        bTree.insert(40, new Rid(4, 0));

        int k = 40;
        Iterator<Rid> result = bTree.search(k);
        if (!result.hasNext()) {
            System.out.println("Test Failed: Key " + k + " not found!");
        } else {
            System.out.println("Test Passed: Key " + k + " found!");
        }
    }

    @Test
    public void testBasicInsertAndSearch() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(3, "test.idx");
        Rid rid1 = new Rid(1, 1);
        Rid rid2 = new Rid(1, 2);

        bTree.insert(10, rid1);
        bTree.insert(20, rid2);
        bTree.insert(5, new Rid(1, 3));

        Iterator<Rid> result1 = bTree.search(10);
        assertTrue(result1.hasNext());
        assertEquals(rid1, result1.next());

        Iterator<Rid> result2 = bTree.search(20);
        assertTrue(result2.hasNext());
        assertEquals(rid2, result2.next());

        Iterator<Rid> result3 = bTree.search(99);
        assertFalse(result3.hasNext());
    }

    @Test
    public void testMultipleValuesSameKey() {
        BTreeImplementation<String, Rid> bTree = new BTreeImplementation<>(4, "test.idx");
        Rid rid1 = new Rid(1, 1);
        Rid rid2 = new Rid(1, 2);
        Rid rid3 = new Rid(2, 1);

        bTree.insert("apple", rid1);
        bTree.insert("apple", rid2);
        bTree.insert("banana", rid3);
        bTree.insert("apple", new Rid(2, 2));

        Iterator<Rid> appleResults = bTree.search("apple");
        assertTrue(appleResults.hasNext());

        Iterator<Rid> bananaResults = bTree.search("banana");
        assertTrue(bananaResults.hasNext());
        assertEquals(rid3, bananaResults.next());
    }

    @Test
    public void testLeafNodeSplitting() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(3, "test.idx");
        bTree.insert(10, new Rid(1, 1));
        bTree.insert(20, new Rid(1, 2));
        bTree.insert(30, new Rid(1, 3));
        bTree.insert(40, new Rid(1, 4));

        assertTrue(bTree.search(10).hasNext());
        assertTrue(bTree.search(20).hasNext());
        assertTrue(bTree.search(30).hasNext());
        assertTrue(bTree.search(40).hasNext());

        bTree.insert(5, new Rid(1, 5));
        bTree.insert(15, new Rid(1, 6));
        bTree.insert(25, new Rid(1, 7));

        int count = 0;
        for (int i : new int[]{5, 10, 15, 20, 25, 30, 40}) {
            if (bTree.search(i).hasNext()) count++;
        }
        assertEquals(7, count);
    }

    @Test
    public void testNonLeafNodeSplitting() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(2, "test.idx");
        bTree.insert(10, new Rid(1, 1));
        bTree.insert(20, new Rid(1, 2));
        bTree.insert(30, new Rid(1, 3));
        bTree.insert(40, new Rid(1, 4));
        bTree.insert(5, new Rid(1, 5));
        bTree.insert(15, new Rid(1, 6));
        bTree.insert(25, new Rid(1, 7));
        bTree.insert(35, new Rid(1, 8));

        for (int i = 0; i < 8; i++) {
            int key = 5 + i * 5;
            assertTrue("Key " + key + " not found", bTree.search(key).hasNext());
        }
    }

    @Test
    public void testRangeSearch() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(4, "test.idx");

        for (int i = 0; i < 20; i++) {
            bTree.insert(i, new Rid(1, i));
        }

        Iterator<Rid> rangeResults = bTree.rangeSearch(5, 10);
        int count = 0;
        while (rangeResults.hasNext()) {
            rangeResults.next();
            count++;
        }
        assertEquals(6, count);

        Iterator<Rid> emptyRange = bTree.rangeSearch(50, 60);
        assertFalse(emptyRange.hasNext());
    }

//    @Test
//    public void testConsecutiveDuplicateKeys() {
//        BTreeImplementation<String, Rid> bTree = new BTreeImplementation<>(4, "test.idx");
//
//        for (int i = 0; i < 10; i++) {
//            bTree.insert("duplicate", new Rid(1, i));
//        }
//
//        Iterator<Rid> results = bTree.search("duplicate");
//        assertTrue(results.hasNext());
//        assertEquals(new Rid(1, 9), results.next());
//        assertFalse(bTree.search("non-existent").hasNext());
//    }

    @Test
    public void testEmptyTree() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(3, "test.idx");
        assertFalse(bTree.search(10).hasNext());
        Iterator<Rid> rangeResults = bTree.rangeSearch(1, 100);
        assertFalse(rangeResults.hasNext());
    }

    @Test
    public void testLargeDataset() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(50, "test.idx");
        int numEntries = 1000;

        for (int i = 0; i < numEntries; i++) {
            bTree.insert(i, new Rid(1, i));
        }

        for (int i = 0; i < numEntries; i++) {
            assertTrue(bTree.search(i).hasNext());
        }

        assertFalse(bTree.search(-1).hasNext());
        assertFalse(bTree.search(numEntries + 1).hasNext());
    }

    @Test
    public void testEdgeCases() {
        BTreeImplementation<Integer, Rid> bTree = new BTreeImplementation<>(2, "test.idx");
        bTree.insert(Integer.MIN_VALUE, new Rid(1, 1));
        bTree.insert(Integer.MAX_VALUE, new Rid(1, 2));

        assertTrue(bTree.search(Integer.MIN_VALUE).hasNext());
        assertTrue(bTree.search(Integer.MAX_VALUE).hasNext());

        assertThrows(NullPointerException.class, () -> {
            bTree.insert(null, new Rid(1, 3));
        });
    }
}