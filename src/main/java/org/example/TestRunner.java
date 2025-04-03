package org.example;

/**
 * Test class that runs the required C1-C4 tests for the assignment
 * with options to limit the number of records processed
 */
public class TestRunner {
    // Set this to limit the number of records to process for testing
    // Use -1 for no limit (process all records)
    private static final int RECORD_LIMIT = 1000;
    
    public static void main(String[] args) {
        System.out.println("Starting database test runner...");
        System.out.println("Record limit set to: " + (RECORD_LIMIT == -1 ? "NO LIMIT" : RECORD_LIMIT));
        
        // Create database manager with custom record limit
        DatabaseManager dbManager = new DatabaseManager(2048, RECORD_LIMIT);
        
        // Path to the title.basics.tsv file  ----CHANGE THIS TO YOUR ACTUAL DATASET FILE PATH
        String filepath = "C:\\Users\\varsh\\OneDrive\\Desktop\\title.basics.tsv"; // Update this with your actual path
        
        try {
            // Test C1: Build index on title attribute
            System.out.println("\n=== TEST C1: BUILD INDEX ON TITLE ===");
            long startTime = System.currentTimeMillis();
            dbManager.createTable(filepath, "Movies");
            dbManager.createIndex("Movies", "title", "TitleIndex");
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken for Test C1: " + (endTime - startTime) + "ms");
            
            // Test C2: Build index on movieId attribute
            System.out.println("\n=== TEST C2: BUILD INDEX ON MOVIE ID ===");
            startTime = System.currentTimeMillis();
            dbManager.createIndex("Movies", "movieId", "MovieIdIndex");
            endTime = System.currentTimeMillis();
            System.out.println("Time taken for Test C2: " + (endTime - startTime) + "ms");
            
            // Test C3: Point queries
            System.out.println("\n=== TEST C3: POINT QUERIES ===");
            System.out.println("=== Test C3.1: Point query using movieId index ===");
            startTime = System.currentTimeMillis();
            dbManager.pointQuery("MovieIdIndex", "tt0000001");
            endTime = System.currentTimeMillis();
            System.out.println("Time taken for Test C3.1: " + (endTime - startTime) + "ms");
            
            System.out.println("=== Test C3.2: Point query using title index ===");
            startTime = System.currentTimeMillis();
            // Choose a title that exists in your dataset
            dbManager.pointQuery("TitleIndex", "The");
            endTime = System.currentTimeMillis();
            System.out.println("Time taken for Test C3.2: " + (endTime - startTime) + "ms");
            
            // Test C4: Range queries
            System.out.println("\n=== TEST C4: RANGE QUERIES ===");
            System.out.println("=== Test C4.1: Range query using movieId index ===");
            startTime = System.currentTimeMillis();
            dbManager.rangeQuery("MovieIdIndex", "tt0000001", "tt0000010");
            endTime = System.currentTimeMillis();
            System.out.println("Time taken for Test C4.1: " + (endTime - startTime) + "ms");
            
            System.out.println("=== Test C4.2: Range query using title index ===");
            startTime = System.currentTimeMillis();
            dbManager.rangeQuery("TitleIndex", "A", "B");
            endTime = System.currentTimeMillis();
            System.out.println("Time taken for Test C4.2: " + (endTime - startTime) + "ms");
            
            // Performance tests (optional for limited record tests)
            if (RECORD_LIMIT == -1 || RECORD_LIMIT >= 10000) {
                System.out.println("\n=== PERFORMANCE TESTS ===");
                System.out.println("=== Test P1: Title index performance ===");
                dbManager.runPerformanceTests("Movies", "TitleIndex", "title");
                
                System.out.println("=== Test P2: MovieId index performance ===");
                dbManager.runPerformanceTests("Movies", "MovieIdIndex", "movieId");
            } else {
                System.out.println("\n=== SKIPPING PERFORMANCE TESTS DUE TO RECORD LIMIT ===");
            }
            
            // Flush all pages to disk
            dbManager.flushAllPages();
            
            System.out.println("\nAll tests completed successfully!");
        } catch (Exception e) {
            System.err.println("An error occurred during testing:");
            e.printStackTrace();
        }
    }
}