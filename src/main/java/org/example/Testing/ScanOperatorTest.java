package org.example.Testing;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;
import org.example.Executor.ScanOperator;

public class ScanOperatorTest {
    public static void main(String[] args) {
        BufferManager bufferManager = new BufferManagerImplementation(100); // Ensure this is implemented
        String filePath = "movies.data";  // You can also try "movies.data", etc.
        int totalPages = 10; // Set according to your test file

        ScanOperator scanOperator = new ScanOperator(bufferManager, filePath);
//        scanOperator.open();

//        Row row;
//        int count = 0;
//        while ((row = scanOperator.next()) != null) {
//            count++;
//            // Output is already inside the ScanOperator's debug prints
//        }

        System.out.println("Total rows scanned: " + scanOperator.countRows());
//        scanOperator.close();
    }
}
