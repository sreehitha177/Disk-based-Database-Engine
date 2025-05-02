package org.example;

public class ScanOperatorTest {
    public static void main(String[] args) {
        BufferManager bufferManager = new BufferManagerImplementation(100); // Ensure this is implemented
        String filePath = "workedon.data";  // You can also try "movies.data", etc.
        int totalPages = 1; // Set according to your test file

        ScanOperator scanOperator = new ScanOperator(bufferManager, filePath, totalPages);
        scanOperator.open();

        Row row;
        int count = 0;
        while ((row = scanOperator.next()) != null) {
            count++;
            // Output is already inside the ScanOperator's debug prints
        }

        System.out.println("Total rows scanned: " + count);
        scanOperator.close();
    }
}
