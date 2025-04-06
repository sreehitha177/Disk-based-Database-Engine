package org.example;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class TestCasesPerformance {
    // We'll use a single BufferManager instance.
    private static BufferManager bm;

    public static void main(String[] args) {
        // Initialize the BufferManager.
        bm = new BufferManagerImplementation(50);
        
        // Ensure that movies.data exists.
        File dataFile = new File("movies.data");
        if (!dataFile.exists()) {
            System.out.println("movies.data file does not exist. Please run Lab 1 loadDataset first.");
            return;
        }
        
        // Run Test P1: Title range queries.
        runPerformanceTestTitle();

        // Run Test P2: MovieId range queries.
        runPerformanceTestMovieId();

    }

    /**
     * Test P1:
     * Compare execution time of range query on title attribute via:
     * (1) Direct scan of movies.data.
     * (2) Using title index to retrieve RIDs, then accessing rows.
     */
    private static void runPerformanceTestTitle() {
        System.out.println("----- Running Performance Test P1 (Title Range Queries) -----");
        // Build title index first.
        BTree<String, Rid> titleIndex = buildTitleIndex();
        
        // For demonstration, we'll vary the end range of a query.
        // We'll fix a start key and change the end key.
        String startKey = "A"; // e.g., start from "A"
        // For a series of end keys: "B", "C", "D", ... "Z"
        for (char endChar = 'B'; endChar <= 'Z'; endChar++) {
            String endKey = "" + endChar;
            
            // Method 1: Direct scan.
            long startTime = System.nanoTime();
            int countDirect = directScanTitleRange(startKey, endKey);
            long durationDirect = System.nanoTime() - startTime;
            
            // Method 2: Use index.
            startTime = System.nanoTime();
            int countIndex = indexScanTitleRange(titleIndex, startKey, endKey);
            long durationIndex = System.nanoTime() - startTime;
            
            // Compute ratio.
            double ratio = durationDirect / (durationIndex + 1.0); // avoid division by zero
            
            // Print results (CSV format for easy plotting):
            System.out.println(endKey + "," + countDirect + "," + durationDirect + "," + durationIndex + "," + ratio);
        }
        System.out.println("----- Performance Test P1 Completed -----\n");
    }
    
    /**
     * Directly scan the movies.data file for DataRows whose title is between startKey and endKey.
     * Returns the count of matching rows.
     */
    private static int directScanTitleRange(String startKey, String endKey) {
        int count = 0;
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String title = new String(dRow.getTitle(), StandardCharsets.ISO_8859_1).trim();
                    // Check if title falls within the range [startKey, endKey] lexicographically.
                    if (title.compareTo(startKey) >= 0 && title.compareTo(endKey) <= 0) {
                        count++;
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return count;
    }
    
    /**
     * Use the title index to perform a range search for titles between startKey and endKey.
     * Returns the count of matching rows.
     */
    private static int indexScanTitleRange(BTree<String, Rid> titleIndex, String startKey, String endKey) {
        int count = 0;
        Iterator<Rid> iter = titleIndex.rangeSearch(startKey, endKey);
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }
    
    /**
     * Build a title index by scanning the movies.data file.
     */
    private static BTree<String, Rid> buildTitleIndex() {
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
        BTree<String, Rid> titleIndex = new BTreeImplementation<>(10, bm, "test.movies.title.idx");
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String title = new String(dRow.getTitle(), StandardCharsets.ISO_8859_1).trim();
                    Rid rid = new Rid(page.getPid(), rowId);
                    titleIndex.insert(title, rid);
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return titleIndex;
    }
    
    /**
     * Test P2:
     * Compare execution time of range queries on movieId attribute via:
     * (1) Direct scan.
     * (2) Using movieId index.
     */
    private static void runPerformanceTestMovieId() {
        System.out.println("----- Running Performance Test P2 (MovieId Range Queries) -----");
        // Build movieId index.
        BTree<Integer, Rid> movieIdIndex = buildMovieIdIndex();
        
        // For demonstration, we'll vary the end of the range.
        int startKey = 101;
        for (int endKey = 200; endKey <= 1000; endKey += 100) {
            // Method 1: Direct scan.
            long startTime = System.nanoTime();
            int countDirect = directScanMovieIdRange(startKey, endKey);
            long durationDirect = System.nanoTime() - startTime;
            
            // Method 2: Using index.
            startTime = System.nanoTime();
            int countIndex = indexScanMovieIdRange(movieIdIndex, startKey, endKey);
            long durationIndex = System.nanoTime() - startTime;
            
            double ratio = durationDirect / (durationIndex + 1.0);
            // Print in CSV format: endKey, countDirect, durationDirect, durationIndex, ratio.
            System.out.println(endKey + "," + countDirect + "," + durationDirect + "," + durationIndex + "," + ratio);
        }
        System.out.println("----- Performance Test P2 Completed -----\n");
    }
    
    /**
     * Directly scan movies.data for DataRows whose movieId (parsed as integer) is between startKey and endKey.
     */
    private static int directScanMovieIdRange(int startKey, int endKey) {
        int count = 0;
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String movieIdStr = new String(dRow.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                    if (!movieIdStr.isEmpty()) {
                        String numericPart = movieIdStr.replace("tt", "");
                        try {
                            int movieId = Integer.parseInt(numericPart);
                            if (movieId >= startKey && movieId <= endKey) {
                                count++;
                            }
                        } catch (NumberFormatException e) {
                            // Skip.
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return count;
    }
    
    /**
     * Use the movieId index to perform a range search for movieIds between startKey and endKey.
     */
    private static int indexScanMovieIdRange(BTree<Integer, Rid> movieIdIndex, int startKey, int endKey) {
        int count = 0;
        Iterator<Rid> iter = movieIdIndex.rangeSearch(startKey, endKey);
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }
    
    /**
     * Build a movieId index by scanning movies.data.
     */
    private static BTree<Integer, Rid> buildMovieIdIndex() {
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
        BTree<Integer, Rid> movieIdIndex = new BTreeImplementation<>(10, bm, "test.movies.movieid.idx");
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String movieIdStr = new String(dRow.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                    if (!movieIdStr.isEmpty()) {
                        String numericPart = movieIdStr.replace("tt", "");
                        try {
                            int movieId = Integer.parseInt(numericPart);
                            Rid rid = new Rid(page.getPid(), rowId);
                            movieIdIndex.insert(movieId, rid);
                        } catch (NumberFormatException e) {
                            // Skip.
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return movieIdIndex;
    }
    
}