package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Utilities
public class Utilities {
    private static BufferManagerImplementation bufferManager;
    private static SystemCatalog systemCatalog;
    private static int recordLimit = -1; // No limit by default

    //To set the buffer manager instance
    public static void setBufferManager(BufferManager bm) {
        if (bm instanceof BufferManagerImplementation) {
            bufferManager = (BufferManagerImplementation) bm;
        } else {
            throw new IllegalArgumentException("Buffer manager must be an instance of BufferManagerImplementation");
        }
    }
    
    //To set the system catalog instance
    public static void setSystemCatalog(SystemCatalog catalog) {
        systemCatalog = catalog;
    }
    
    //To set record limit
    public static void setRecordLimit(int limit) {
        recordLimit = limit;
        System.out.println("Record limit set to: " + (recordLimit == -1 ? "NO LIMIT" : recordLimit));
    }

    /**
     * Loads the dataset into the buffer manager and creates a table
     * @param filepath The path to the dataset file
     * @param tableName The name of the table to create
     * @param fileId The file ID to use for the table
     */
    public static void loadDataset(String filepath, String tableName, String fileId) {
        System.out.println("Loading dataset from: " + filepath + " into table: " + tableName);
        
        File file = new File(filepath);

        // Checking if the file exists
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        } else {
            System.out.println("File found, proceeding with reading...");
            System.out.println("Starting to read data file: " + filepath);
        }
        
        // Register the file with the buffer manager if it's not already registered
        if (bufferManager != null) {
            // Create a data file path
            String dataFilePath = fileId + ".bin";
            bufferManager.registerFile(fileId, dataFilePath);
            
            // Create a schema for the table
            Schema schema = new Schema();
            schema.addAttribute("movieId", DataType.CHAR, 9);
            schema.addAttribute("title", DataType.CHAR, 30);
            
            // Register the table in the system catalog
            if (systemCatalog != null) {
                systemCatalog.registerTable(tableName, fileId, schema);
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            // Discarding the header line
            String line = br.readLine();
            System.out.println("Skipping header: " + line);

            Page currentPage = bufferManager.createPage(fileId);
            int currentPageId = currentPage.getPid();

            int totalRows = 0;
            int finalRows = 0;
            
            while ((line = br.readLine()) != null) {
                // Check if we've reached the record limit
                if (recordLimit > 0 && finalRows >= recordLimit) {
                    System.out.println("Reached record limit of " + recordLimit + ", stopping data load.");
                    break;
                }
                
                String[] data = line.split("\t");
                // Checking if the data has at least three columns
                if (data.length < 3) {
                    System.out.println("Skipping malformed line: " + line);
                    finalRows++;
                    continue;
                }

                // Getting movie ID
                String movieId = data[0];
                // Getting movie title
                String title = data[2];
                // Truncating the title if it exceeds 30 characters
                if (title.length() > 30) {
                    title = title.substring(0, 30);
                }

                // Ensuring movieId does not exceed the allowed length
                if (movieId.getBytes(StandardCharsets.UTF_8).length > 9) {
                    System.out.println("Skipping row with oversized movieId: " + movieId);
                    finalRows++;
                    continue;
                }

                Row row = new Row(movieId.getBytes(StandardCharsets.UTF_8), title.getBytes(StandardCharsets.UTF_8));
                int rowId;

                // If the page is full, create a new page and insert the row there
                if (currentPage.isFull()) {
                    bufferManager.unpinPage(fileId, currentPageId);
                    currentPage = bufferManager.createPage(fileId);
                    currentPageId = currentPage.getPid();
                    rowId = currentPage.insertRow(row);
                    System.out.println("Page " + (currentPageId - 1) + " is full, moving to the next page.");
                }
                else{
                    rowId = currentPage.insertRow(row);
                }

                totalRows++;
                finalRows++;
                
                // Print progress periodically
                if (finalRows % 1000 == 0) {
                    System.out.println("Progress: processed " + finalRows + " rows");
                }
                
                System.out.println("Inserted Row with ID: " + rowId + " on Page ID: " + currentPageId);
            }

            bufferManager.unpinPage(fileId, currentPageId);
            System.out.println("Total rows processed: " + totalRows);
            System.out.println("Final rows processed: " + finalRows);
            System.out.println("Last Page ID: " + currentPageId);
            System.out.println("Completed loading data. Processed " + totalRows + " rows, inserted " + finalRows + " rows.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Builds a B+ tree index on a table attribute
     * @param tableName The name of the table
     * @param attributeName The name of the attribute to index
     * @param indexName The name of the index
     * @param indexFileId The file ID to use for the index
     */
    public static void buildIndex(String tableName, String attributeName, String indexName, String indexFileId) {
        System.out.println("Building index: " + indexName + " on table: " + tableName + 
                          " for attribute: " + attributeName);

        // Ensure the directory exists
        File indexFile = new File(indexFileId);
        File parentDir = indexFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!indexFileId.endsWith(".bin")) {
            indexFileId = indexFileId + ".bin";
        }

        // Check if file already exists
        if (new File(indexFileId).exists()) {
            System.out.println("Index file already exists: " + indexFileId);
            return;
        }
        
        if (bufferManager == null || systemCatalog == null) {
            System.out.println("Buffer manager or system catalog not initialized");
            return;
        }
        
        // Check if the table exists
        if (!systemCatalog.tableExists(tableName)) {
            System.out.println("Table does not exist: " + tableName);
            return;
        }
        
        // Get table info
        TableInfo tableInfo = systemCatalog.getTableInfo(tableName);
        String tableFileId = tableInfo.getFileId();
        
        // Check if the attribute exists
        Schema schema = tableInfo.getSchema();
        if (!schema.hasAttribute(attributeName)) {
            System.out.println("Attribute does not exist: " + attributeName);
            return;
        }
        
        // Register the index file with the buffer manager
        String indexFilePath = indexFileId + ".bin";
        bufferManager.registerFile(indexFileId, indexFilePath);
        
        // Register the index in the system catalog
        systemCatalog.registerIndex(indexName, tableName, attributeName, indexFileId);
        
        // Create the B+ tree
        BTreeBufferManagerImpl<String> bTree;
        if (attributeName.equals("movieId")) {
            bTree = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, 10, true);
        } else if (attributeName.equals("title")) {
            bTree = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, 5, true);
        } else {
            System.out.println("Unsupported attribute for indexing: " + attributeName);
            return;
        }
        
        // Scan all pages of the table and insert into the index
        int pageId = 0;
        boolean hasMorePages = true;
        int indexedRecords = 0;
        
        System.out.println("Starting table scan to build index");
        
        while (hasMorePages) {
            Page page = bufferManager.getPage(tableFileId, pageId);
            
            if (page == null) {
                hasMorePages = false;
                continue;
            }
            
            // Process all rows in the page
            int rowId = 0;
            Row row;
            
            while ((row = page.getRow(rowId)) != null) {
                // Check if we've reached the record limit
                if (recordLimit > 0 && indexedRecords >= recordLimit) {
                    System.out.println("Reached record limit of " + recordLimit + ", stopping index build.");
                    hasMorePages = false;
                    break;
                }
                
                // Extract the key based on the attribute
                String key;
                if (attributeName.equals("movieId")) {
                    key = new String(row.movieId, StandardCharsets.UTF_8).trim();
                } else if (attributeName.equals("title")) {
                    key = new String(row.title, StandardCharsets.UTF_8).trim();
                } else {
                    // Skip unsupported attributes
                    rowId++;
                    continue;
                }
                
                // Create a RID for this row
                Rid rid = new Rid(pageId, rowId);
                
                // Insert into the B+ tree
                bTree.insert(key, rid);
                
                System.out.println("Indexed key: " + key + " with RID: [" + rid.getPid() + "," + rid.getSid() + "]");
                
                rowId++;
                indexedRecords++;
                
                // Print progress periodically
                if (indexedRecords % 1000 == 0) {
                    System.out.println("Progress: indexed " + indexedRecords + " records");
                }
            }
            
            bufferManager.unpinPage(tableFileId, pageId);
            pageId++;
        }
        
        System.out.println("Index building complete for: " + indexName + ", indexed " + indexedRecords + " records");
    }
    
    /**
     * Tests a point query using an index
     * @param indexName The name of the index to use
     * @param searchKey The key to search for
     */
    public static void testPointQuery(String indexName, String searchKey) {
        System.out.println("Testing point query on index: " + indexName + " for key: " + searchKey);
        
        if (bufferManager == null || systemCatalog == null) {
            System.out.println("Buffer manager or system catalog not initialized");
            return;
        }
        
        // Check if the index exists
        if (!systemCatalog.indexExists(indexName)) {
            System.out.println("Index does not exist: " + indexName);
            return;
        }
        
        // Get index info
        IndexInfo indexInfo = systemCatalog.getIndexInfo(indexName);
        String indexFileId = indexInfo.getFileId();
        String tableName = indexInfo.getTableName();
        String attributeName = indexInfo.getAttributeName();
        
        // Get table info
        TableInfo tableInfo = systemCatalog.getTableInfo(tableName);
        String tableFileId = tableInfo.getFileId();
        
        // Create the B+ tree
        BTreeBufferManagerImpl<String> bTree;
        if (attributeName.equals("movieId")) {
            bTree = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, 10, true);
        } else if (attributeName.equals("title")) {
            bTree = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, 5, true);
        } else {
            System.out.println("Unsupported attribute for indexing: " + attributeName);
            return;
        }
        
        // Search for the key
        long startTime = System.currentTimeMillis();
        
        Iterator<Rid> ridIterator = bTree.search(searchKey);
        List<Row> results = new ArrayList<>();
        
        while (ridIterator.hasNext()) {
            Rid rid = ridIterator.next();
            Page page = bufferManager.getPage(tableFileId, rid.getPid());
            
            if (page != null) {
                Row row = page.getRow(rid.getSid());
                if (row != null) {
                    results.add(row);
                }
                bufferManager.unpinPage(tableFileId, rid.getPid());
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Print results
        System.out.println("Found " + results.size() + " results in " + (endTime - startTime) + "ms");
        
        for (Row row : results) {
            String movieId = new String(row.movieId, StandardCharsets.UTF_8).trim();
            String title = new String(row.title, StandardCharsets.UTF_8).trim();
            System.out.println("MovieID: " + movieId + ", Title: " + title);
        }
    }
    
    /**
     * Tests a range query using an index
     * @param indexName The name of the index to use
     * @param startKey The start key of the range
     * @param endKey The end key of the range
     */
    public static void testRangeQuery(String indexName, String startKey, String endKey) {
        System.out.println("Testing range query on index: " + indexName + 
                          " from key: " + startKey + " to key: " + endKey);
        
        if (bufferManager == null || systemCatalog == null) {
            System.out.println("Buffer manager or system catalog not initialized");
            return;
        }
        
        // Check if the index exists
        if (!systemCatalog.indexExists(indexName)) {
            System.out.println("Index does not exist: " + indexName);
            return;
        }
        
        // Get index info
        IndexInfo indexInfo = systemCatalog.getIndexInfo(indexName);
        String indexFileId = indexInfo.getFileId();
        String tableName = indexInfo.getTableName();
        String attributeName = indexInfo.getAttributeName();
        
        // Get table info
        TableInfo tableInfo = systemCatalog.getTableInfo(tableName);
        String tableFileId = tableInfo.getFileId();
        
        // Create the B+ tree
        BTreeBufferManagerImpl<String> bTree;
        if (attributeName.equals("movieId")) {
            bTree = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, 10, true);
        } else if (attributeName.equals("title")) {
            bTree = new BTreeBufferManagerImpl<>(bufferManager, indexFileId, 5, true);
        } else {
            System.out.println("Unsupported attribute for indexing: " + attributeName);
            return;
        }
        
        // Search for the range
        long startTime = System.currentTimeMillis();
        
        Iterator<Rid> ridIterator = bTree.rangeSearch(startKey, endKey);
        List<Row> results = new ArrayList<>();
        
        while (ridIterator.hasNext()) {
            Rid rid = ridIterator.next();
            Page page = bufferManager.getPage(tableFileId, rid.getPid());
            
            if (page != null) {
                Row row = page.getRow(rid.getSid());
                if (row != null) {
                    results.add(row);
                }
                bufferManager.unpinPage(tableFileId, rid.getPid());
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Print results
        System.out.println("Found " + results.size() + " results in " + (endTime - startTime) + "ms");
        
        for (Row row : results) {
            String movieId = new String(row.movieId, StandardCharsets.UTF_8).trim();
            String title = new String(row.title, StandardCharsets.UTF_8).trim();
            System.out.println("MovieID: " + movieId + ", Title: " + title);
        }
    }
    
    /**
     * Tests a direct scan of the table without using an index
     * @param tableName The name of the table to scan
     * @param attributeName The name of the attribute to match
     * @param startKey The start key of the range
     * @param endKey The end key of the range
     */
    public static void testDirectRangeScan(String tableName, String attributeName, String startKey, String endKey) {
        System.out.println("Testing direct range scan on table: " + tableName + 
                          " for attribute: " + attributeName + 
                          " from key: " + startKey + " to key: " + endKey);
        
        if (bufferManager == null || systemCatalog == null) {
            System.out.println("Buffer manager or system catalog not initialized");
            return;
        }
        
        // Check if the table exists
        if (!systemCatalog.tableExists(tableName)) {
            System.out.println("Table does not exist: " + tableName);
            return;
        }
        
        // Get table info
        TableInfo tableInfo = systemCatalog.getTableInfo(tableName);
        String tableFileId = tableInfo.getFileId();
        
        // Scan all pages of the table
        long startTime = System.currentTimeMillis();
        
        int pageId = 0;
        boolean hasMorePages = true;
        List<Row> results = new ArrayList<>();
        
        System.out.println("Starting table scan");
        
        while (hasMorePages) {
            Page page = bufferManager.getPage(tableFileId, pageId);
            
            if (page == null) {
                hasMorePages = false;
                continue;
            }
            
            // Process all rows in the page
            int rowId = 0;
            Row row;
            
            while ((row = page.getRow(rowId)) != null) {
                // Extract the key based on the attribute
                String key;
                if (attributeName.equals("movieId")) {
                    key = new String(row.movieId, StandardCharsets.UTF_8).trim();
                } else if (attributeName.equals("title")) {
                    key = new String(row.title, StandardCharsets.UTF_8).trim();
                } else {
                    // Skip unsupported attributes
                    rowId++;
                    continue;
                }
                
                // Check if the key is in the range
                if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                    results.add(row);
                }
                
                rowId++;
                
                // Print progress periodically for long scans
                if (results.size() > 0 && results.size() % 1000 == 0) {
                    System.out.println("Progress: found " + results.size() + " matching records");
                }
            }
            
            bufferManager.unpinPage(tableFileId, pageId);
            pageId++;
            
            // Print page scan progress periodically
            if (pageId % 100 == 0) {
                System.out.println("Progress: scanned " + pageId + " pages");
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Print results
        System.out.println("Found " + results.size() + " results in " + (endTime - startTime) + "ms");
        
        // Print only the first 20 results to avoid overwhelming the console
        int maxToShow = Math.min(results.size(), 20);
        for (int i = 0; i < maxToShow; i++) {
            Row row = results.get(i);
            String movieId = new String(row.movieId, StandardCharsets.UTF_8).trim();
            String title = new String(row.title, StandardCharsets.UTF_8).trim();
            System.out.println("MovieID: " + movieId + ", Title: " + title);
        }
        
        if (results.size() > 20) {
            System.out.println("... and " + (results.size() - 20) + " more results");
        }
    }
    
    /**
     * Runs performance tests comparing direct scan vs. index scan
     * @param tableName The name of the table
     * @param indexName The name of the index
     * @param attributeName The name of the attribute
     */
    public static void runPerformanceTests(String tableName, String indexName, String attributeName) {
        System.out.println("Running performance tests:");
        System.out.println("Table: " + tableName);
        System.out.println("Index: " + indexName);
        System.out.println("Attribute: " + attributeName);
        
        // Run tests for different selectivity ranges
        String[][] testRanges;
        
        if (attributeName.equals("movieId")) {
            testRanges = new String[][] {
                {"tt0000001", "tt0000010"},  // Very selective (~10 records)
                {"tt0000001", "tt0000100"},  // Somewhat selective (~100 records)
                {"tt0000001", "tt0001000"},  // Less selective (~1000 records)
                {"tt0000001", "tt0010000"}   // Barely selective (~10000 records)
            };
        } else if (attributeName.equals("title")) {
            testRanges = new String[][] {
                {"A", "B"},      // Movies starting with A
                {"A", "D"},      // Movies starting with A-C
                {"A", "M"},      // First half of alphabet
                {"A", "Z"}       // All movies
            };
        } else {
            System.out.println("Unsupported attribute for testing: " + attributeName);
            return;
        }
        
        System.out.println("\nSelectivity, Direct Scan Time (ms), Index Scan Time (ms), Speedup Ratio");
        
        for (String[] range : testRanges) {
            String startKey = range[0];
            String endKey = range[1];
            
            // Test direct scan
            long directStart = System.currentTimeMillis();
            testDirectRangeScan(tableName, attributeName, startKey, endKey);
            long directEnd = System.currentTimeMillis();
            long directTime = directEnd - directStart;
            
            // Test index scan
            long indexStart = System.currentTimeMillis();
            testRangeQuery(indexName, startKey, endKey);
            long indexEnd = System.currentTimeMillis();
            long indexTime = indexEnd - indexStart;
            
            // Calculate speedup
            double speedup = (double) directTime / indexTime;
            
            System.out.println(startKey + "-" + endKey + ", " + directTime + ", " + indexTime + ", " + String.format("%.2f", speedup));
        }
    }

    public static void pinAndUpdateLRU(PageImplementation page, LinkedHashMap<Integer, PageImplementation> lruCache) {
        page.pin();
        lruCache.remove(page.getPid()); // Remove if exists
        lruCache.put(page.getPid(), page);
    }

    public static PageImplementation getUnfilledPage(Map<Integer, PageImplementation> pageTable, LinkedHashMap<Integer, PageImplementation> lruCache) {
        for (PageImplementation page : pageTable.values()) {
            if (!page.isFull()) {
                pinAndUpdateLRU(page, lruCache);
                return page;
            }
        }
        return null;
    }

    private static final byte PADDING_BYTE = 0x20; // Use SPACE (0x20) instead of 0x7F

    public static byte[] truncateOrPadByteArray(byte[] value, int maxLength) {
        if (value.length > maxLength) {
            return Arrays.copyOf(value, maxLength);
        } else {
            byte[] padded = new byte[maxLength];
            System.arraycopy(value, 0, padded, 0, value.length);
            Arrays.fill(padded, value.length, maxLength, PADDING_BYTE); // Fill with SPACE
            return padded;
        }
    }

    public static byte[] removeTrailingBytes(byte[] input) {
        int endIndex = input.length;
        for (int i = input.length - 1; i >= 0; i--) {
            if (input[i] != PADDING_BYTE) {
                endIndex = i + 1;
                break;
            }
        }
        return Arrays.copyOf(input, endIndex);
    }

    public static void main(String[] args) {
        System.out.println("Starting database initialization...");
        
        // Initialize components
        bufferManager = new BufferManagerImplementation(2048);
        systemCatalog = new SystemCatalog();
        
        // Set up utilities
        setBufferManager(bufferManager);
        setSystemCatalog(systemCatalog);
        setRecordLimit(1000); // Limit to 1000 records for testing
        
        // Path to the title.basics.tsv file  ----CHANGE THIS TO YOUR ACTUAL DATASET FILE PATH
        String filepath = "C:\\Users\\varsh\\OneDrive\\Desktop\\title.basics.tsv";
        
        // Load data into the Movies table
        System.out.println("\n=== LOADING DATA ===");
        loadDataset(filepath, "Movies", "movies_data");
        
        // Build index on movieId
        System.out.println("\n=== BUILDING MOVIE ID INDEX ===");
        buildIndex("Movies", "movieId", "MovieIdIndex", "movieid_index");
        
        // Build index on title
        System.out.println("\n=== BUILDING TITLE INDEX ===");
        buildIndex("Movies", "title", "TitleIndex", "title_index");
        
        // Run test C3: Point query using movieId index
        System.out.println("\n=== TEST C3: POINT QUERY USING MOVIE ID INDEX ===");
        testPointQuery("MovieIdIndex", "tt0000001");
        
        // Run test C3: Point query using title index
        System.out.println("\n=== TEST C3: POINT QUERY USING TITLE INDEX ===");
        testPointQuery("TitleIndex", "The");
        
        // Run test C4: Range query using movieId index
        System.out.println("\n=== TEST C4: RANGE QUERY USING MOVIE ID INDEX ===");
        testRangeQuery("MovieIdIndex", "tt0000001", "tt0000010");
        
        // Run test C4: Range query using title index
        System.out.println("\n=== TEST C4: RANGE QUERY USING TITLE INDEX ===");
        testRangeQuery("TitleIndex", "A", "B");
        
        // Force writing all dirty pages to disk
        bufferManager.force();

        System.out.println("\nCleaning up temporary files...");
        File tempFile = new File("movies_data.bin");
        if (tempFile.exists()) {
            if (tempFile.delete()) {
                System.out.println("Deleted temporary file: movies_data.bin");
            } else {
                System.out.println("Failed to delete temporary file");
            }
        }
        
        System.out.println("\nAll tests completed successfully!");
    }
}
