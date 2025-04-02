package org.example;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Database manager class that brings together all components
 * and provides a unified interface for database operations
 */
public class DatabaseManager {
    private BufferManagerImplementation bufferManager;
    private SystemCatalog systemCatalog;
    private int recordLimit;
    
    /**
     * Creates a new DatabaseManager with the specified buffer size
     * @param bufferSize The size of the buffer (in pages)
     */
    public DatabaseManager(int bufferSize) {
        this(bufferSize, -1); // No record limit by default
    }
    
    /**
     * Creates a new DatabaseManager with the specified buffer size and record limit
     * @param bufferSize The size of the buffer (in pages)
     * @param recordLimit Maximum number of records to process (-1 for no limit)
     */
    public DatabaseManager(int bufferSize, int recordLimit) {
        System.out.println("Initializing DatabaseManager with buffer size: " + bufferSize +
                          (recordLimit > 0 ? " and record limit: " + recordLimit : ""));
        this.bufferManager = new BufferManagerImplementation(bufferSize);
        this.systemCatalog = new SystemCatalog();
        this.recordLimit = recordLimit;
        
        // Initialize utilities
        Utilities.setBufferManager(bufferManager);
        Utilities.setSystemCatalog(systemCatalog);
        Utilities.setRecordLimit(recordLimit);
    }
    
    /**
     * Creates a new table from a TSV file
     * @param filepath The path to the TSV file
     * @param tableName The name of the table to create
     * @return True if successful, false otherwise
     */
    public boolean createTable(String filepath, String tableName) {
        System.out.println("Creating table: " + tableName + " from file: " + filepath);
        
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + filepath);
            return false;
        }
        
        // Create a data file ID
        String fileId = tableName.toLowerCase() + "_data";
        
        // Load the dataset
        Utilities.loadDataset(filepath, tableName, fileId);
        
        return systemCatalog.tableExists(tableName);
    }
    
    /**
     * Creates an index on a table attribute
     * @param tableName The name of the table
     * @param attributeName The name of the attribute to index
     * @param indexName The name of the index
     * @return True if successful, false otherwise
     */
    public boolean createIndex(String tableName, String attributeName, String indexName) {
        System.out.println("Creating index: " + indexName + " on table: " + tableName + 
                          " for attribute: " + attributeName);
        
        if (!systemCatalog.tableExists(tableName)) {
            System.out.println("Table does not exist: " + tableName);
            return false;
        }
        
        // Create an index file ID
        String fileId = indexName.toLowerCase() + "_index";
        
        // Build the index
        Utilities.buildIndex(tableName, attributeName, indexName, fileId);
        
        return systemCatalog.indexExists(indexName);
    }
    
    /**
     * Gets a BTree for a specific index
     * @param indexName The name of the index
     * @return A BTree instance, or null if the index doesn't exist
     */
    public BTree<String, Rid> getIndex(String indexName) {
        if (!systemCatalog.indexExists(indexName)) {
            System.out.println("Index does not exist: " + indexName);
            return null;
        }
        
        IndexInfo indexInfo = systemCatalog.getIndexInfo(indexName);
        String indexFileId = indexInfo.getFileId();
        String attributeName = indexInfo.getAttributeName();
        
        int order;
        if (attributeName.equals("movieId")) {
            order = 10;
        } else if (attributeName.equals("title")) {
            order = 5;
        } else {
            System.out.println("Unsupported attribute: " + attributeName);
            return null;
        }
        
        return new BTreeAdapter<>(bufferManager, indexFileId, order, true);
    }
    
    /**
     * Retrieves a row from a table using a RID
     * @param tableName The name of the table
     * @param rid The RID of the row
     * @return The Row object, or null if not found
     */
    public Row getRow(String tableName, Rid rid) {
        if (!systemCatalog.tableExists(tableName)) {
            System.out.println("Table does not exist: " + tableName);
            return null;
        }
        
        TableInfo tableInfo = systemCatalog.getTableInfo(tableName);
        String fileId = tableInfo.getFileId();
        
        Page page = bufferManager.getPage(fileId, rid.getPid());
        if (page == null) {
            return null;
        }
        
        Row row = page.getRow(rid.getSid());
        bufferManager.unpinPage(fileId, rid.getPid());
        
        return row;
    }
    
    /**
     * Performs a point query on an index
     * @param indexName The name of the index
     * @param key The key to search for
     */
    public void pointQuery(String indexName, String key) {
        System.out.println("Performing point query on index: " + indexName + " for key: " + key);
        Utilities.testPointQuery(indexName, key);
    }
    
    /**
     * Performs a range query on an index
     * @param indexName The name of the index
     * @param startKey The start key of the range
     * @param endKey The end key of the range
     */
    public void rangeQuery(String indexName, String startKey, String endKey) {
        System.out.println("Performing range query on index: " + indexName + 
                          " from key: " + startKey + " to key: " + endKey);
        Utilities.testRangeQuery(indexName, startKey, endKey);
    }
    
    /**
     * Runs performance tests on an index
     * @param tableName The name of the table
     * @param indexName The name of the index
     * @param attributeName The name of the attribute
     */
    public void runPerformanceTests(String tableName, String indexName, String attributeName) {
        System.out.println("Running performance tests for index: " + indexName);
        Utilities.runPerformanceTests(tableName, indexName, attributeName);
    }
    
    /**
     * Forces all dirty pages to disk
     */
    public void flushAllPages() {
        System.out.println("Flushing all dirty pages to disk");
        bufferManager.force();
    }
    
    /**
     * Prints a row in a human-readable format
     * @param row The row to print
     */
    public static void printRow(Row row) {
        if (row == null) {
            System.out.println("Row is null");
            return;
        }
        
        String movieId = new String(row.movieId, StandardCharsets.UTF_8).trim();
        String title = new String(row.title, StandardCharsets.UTF_8).trim();
        
        System.out.println("MovieID: " + movieId + ", Title: " + title);
    }
    
    /**
     * Main method that demonstrates database operations
     */
    public static void main(String[] args) {
        System.out.println("Starting database manager...");
        
        // Create database manager
        DatabaseManager dbManager = new DatabaseManager(2048);
        
        // Path to the title.basics.tsv file  ----CHANGE THIS TO YOUR ACTUAL DATASET FILE PATH
        String filepath = "C:/path/to/title.basics.tsv";
        
        // Create Movies table
        System.out.println("\n=== CREATING MOVIES TABLE ===");
        dbManager.createTable(filepath, "Movies");
        
        // Create indexes
        System.out.println("\n=== CREATING INDEXES ===");
        dbManager.createIndex("Movies", "movieId", "MovieIdIndex");
        dbManager.createIndex("Movies", "title", "TitleIndex");
        
        // Run tests
        System.out.println("\n=== RUNNING TEST C3: POINT QUERIES ===");
        dbManager.pointQuery("MovieIdIndex", "tt0000001");
        dbManager.pointQuery("TitleIndex", "The Birth of a Nation");
        
        System.out.println("\n=== RUNNING TEST C4: RANGE QUERIES ===");
        dbManager.rangeQuery("MovieIdIndex", "tt0000001", "tt0000010");
        dbManager.rangeQuery("TitleIndex", "A", "B");
        
        System.out.println("\n=== RUNNING PERFORMANCE TESTS ===");
        dbManager.runPerformanceTests("Movies", "TitleIndex", "title");
        dbManager.runPerformanceTests("Movies", "MovieIdIndex", "movieId");
        
        // Flush all pages to disk
        dbManager.flushAllPages();
        
        System.out.println("\nAll operations completed successfully!");
    }
}