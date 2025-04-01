package org.example;

import org.junit.Test;

import java.util.List;


public class Btreetest{

    @Test
    public void testInsertAndSearch() {
        BTreeImplementation<Integer> bTree = new BTreeImplementation<>(3); // maxEntries = 3

        bTree.insert(10, new Rid(1, 0));
        bTree.insert(20, new Rid(2, 0));
        bTree.insert(30, new Rid(3, 0));

        List<Rid> result = bTree.search(20);
        if (result.isEmpty()) {
            System.out.println("Test Failed: Key 20 not found!");
        } else {
            System.out.println("Test Passed: Key 20 found!");
        }
    }
}