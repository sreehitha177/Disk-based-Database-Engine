package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SystemCatalog manages metadata about tables and indexes in the database.
 * It serves as a central registry for accessing information about database objects.
 */
public class SystemCatalog {
    private Map<String, TableInfo> tables;
    private Map<String, IndexInfo> indexes;
    
    public SystemCatalog() {
        System.out.println("Initializing System Catalog");
        this.tables = new HashMap<>();
        this.indexes = new HashMap<>();
    }
    
    /**
     * Registers a new table in the catalog
     * @param tableName The name of the table
     * @param fileId The file ID where the table is stored
     * @param schema The schema of the table
     */
    public void registerTable(String tableName, String fileId, Schema schema) {
        System.out.println("Registering table: " + tableName + " stored in file: " + fileId);
        tables.put(tableName, new TableInfo(fileId, schema));
    }
    
    /**
     * Registers a new index in the catalog
     * @param indexName The name of the index
     * @param tableName The name of the table being indexed
     * @param attributeName The name of the attribute being indexed
     * @param fileId The file ID where the index is stored
     */
    public void registerIndex(String indexName, String tableName, String attributeName, String fileId) {
        System.out.println("Registering index: " + indexName + " on table: " + tableName + 
                          " for attribute: " + attributeName + " stored in file: " + fileId);
        indexes.put(indexName, new IndexInfo(tableName, attributeName, fileId));
    }
    
    /**
     * Gets information about a table
     * @param tableName The name of the table
     * @return The TableInfo object, or null if not found
     */
    public TableInfo getTableInfo(String tableName) {
        return tables.get(tableName);
    }
    
    /**
     * Gets information about an index
     * @param indexName The name of the index
     * @return The IndexInfo object, or null if not found
     */
    public IndexInfo getIndexInfo(String indexName) {
        return indexes.get(indexName);
    }
    
    /**
     * Gets all indexes for a specific table and attribute
     * @param tableName The name of the table
     * @param attributeName The name of the attribute
     * @return A list of IndexInfo objects
     */
    public List<IndexInfo> getIndexesForAttribute(String tableName, String attributeName) {
        List<IndexInfo> result = new ArrayList<>();
        
        for (IndexInfo info : indexes.values()) {
            if (info.getTableName().equals(tableName) && 
                info.getAttributeName().equals(attributeName)) {
                result.add(info);
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a table exists in the catalog
     * @param tableName The name of the table
     * @return True if the table exists, false otherwise
     */
    public boolean tableExists(String tableName) {
        return tables.containsKey(tableName);
    }
    
    /**
     * Checks if an index exists in the catalog
     * @param indexName The name of the index
     * @return True if the index exists, false otherwise
     */
    public boolean indexExists(String indexName) {
        return indexes.containsKey(indexName);
    }
}

/**
 * Stores information about a table in the database
 */
class TableInfo {
    private String fileId;
    private Schema schema;
    
    public TableInfo(String fileId, Schema schema) {
        this.fileId = fileId;
        this.schema = schema;
    }
    
    public String getFileId() {
        return fileId;
    }
    
    public Schema getSchema() {
        return schema;
    }
}

/**
 * Stores information about an index in the database
 */
class IndexInfo {
    private String tableName;
    private String attributeName;
    private String fileId;
    
    public IndexInfo(String tableName, String attributeName, String fileId) {
        this.tableName = tableName;
        this.attributeName = attributeName;
        this.fileId = fileId;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public String getFileId() {
        return fileId;
    }
}

/**
 * Represents the schema of a table, consisting of attributes with types and sizes
 */
class Schema {
    private List<Attribute> attributes;
    
    public Schema() {
        this.attributes = new ArrayList<>();
    }
    
    public void addAttribute(String name, DataType type, int size) {
        attributes.add(new Attribute(name, type, size));
    }
    
    public List<Attribute> getAttributes() {
        return attributes;
    }
    
    public Attribute getAttribute(String name) {
        for (Attribute attr : attributes) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }
    
    public boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }
}

/**
 * Represents a column in a table with a name, type, and size
 */
class Attribute {
    private String name;
    private DataType type;
    private int size;
    
    public Attribute(String name, DataType type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }
    
    public String getName() {
        return name;
    }
    
    public DataType getType() {
        return type;
    }
    
    public int getSize() {
        return size;
    }
}

/**
 * Enumeration of supported data types in the database
 */
enum DataType {
    CHAR,
    INTEGER,
    FLOAT,
    BOOLEAN
}