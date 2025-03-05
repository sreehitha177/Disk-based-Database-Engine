import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public abstract class BufferManager {
    // Configurable size of buffer cache
    final int bufferSize;
    
    // Page size constant
    private static final int PAGE_SIZE = 4 * 1024;
    
    // Page table to map page IDs to pages
    protected Map<Integer, Object> pageTable;
    
    // Queue to track access order for LRU eviction
    protected Queue<Integer> accessOrderQueue;
    
    // File to store pages
    private RandomAccessFile dataFile;
    
    // Counter to generate new page IDs
    private int nextPageId = 0;
    
    public BufferManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.pageTable = new HashMap<>();
        this.accessOrderQueue = new LinkedList<>();
        
        // Open the data file for reading and writing
        try {
            this.dataFile = new RandomAccessFile("database.bin", "rw");
        } catch (IOException e) {
            throw new RuntimeException("Could not open database file", e);
        }
    }
    
 
    public Object getPage(int pageId) {
        // Check if page is already in the buffer
        Object page = pageTable.get(pageId);
        
        if (page != null) {
            // Update access order
            accessOrderQueue.remove(pageId);
            accessOrderQueue.offer(pageId);
            return page;
        }
        
        // Check if buffer is full
        if (pageTable.size() >= bufferSize) {
            // Evict least recently used page that is not pinned
            evictLeastRecentlyUsedPage();
        }
        
        // Load page from disk
        try {
            // Check if page exists in file
            if (pageId * PAGE_SIZE >= dataFile.length()) {
                throw new IllegalArgumentException("Page does not exist: " + pageId);
            }
            
            // Load page content from disk
            byte[] pageData = new byte[PAGE_SIZE];
            dataFile.seek(pageId * PAGE_SIZE);
            int bytesRead = dataFile.read(pageData);
            
            if (bytesRead < PAGE_SIZE) {
                throw new IOException("Could not read full page");
            }
            
            // Create page object
            page = createPageObject(pageId, pageData);
            
            // Add to page table and access order queue
            pageTable.put(pageId, page);
            accessOrderQueue.offer(pageId);
            
            return page;
            
        } catch (IOException e) {
            throw new RuntimeException("Error fetching page " + pageId, e);
        }
    }
    
    /**
     * Creates a page object with the given page ID and data.
     * To be implemented by subclasses to provide specific page type.
     * 
     * @param pageId The ID of the page
     * @param pageData The raw byte data of the page
     * @return A page object
     */
    protected Object createPageObject(int pageId, byte[] pageData){
        // Define max number of rows per page
    int maxRows = 37; // Adjust based on actual row size and PAGE_SIZE constraints

    // Create an empty page object
    PageImplementation page = new PageImplementation(pageId, maxRows);

    // Deserialize the byte array into rows 
    int rowSize = 110; // Define row size based on expected schema 
    for (int offset = 0; offset + rowSize <= pageData.length; offset += rowSize) {
        byte[] movieId = Arrays.copyOfRange(pageData, offset, offset + 10); // Example: first 10 bytes for movieId
        byte[] title = Arrays.copyOfRange(pageData, offset + 10, offset + rowSize); // Next 100 bytes for title

        Row row = new Row(movieId, title);
        page.insertRow(row);
    }

    return page;
    }
    
    /**
     * Checks if a page is pinned.
     * To be implemented by subclasses to provide specific pinning logic.
     * 
     * @param page The page to check
     * @return True if the page is pinned, false otherwise
     */
    protected abstract boolean isPagePinned(Object page);
    
    /**
     * Checks if a page is dirty.
     * To be implemented by subclasses to provide specific dirty page logic.
     * 
     * @param page The page to check
     * @return True if the page is dirty, false otherwise
     */
    protected abstract boolean isPageDirty(Object page);
    
    /**
     * Writes a page's data back to disk.
     * To be implemented by subclasses to provide specific write logic.
     * 
     * @param pageId The ID of the page
     * @param page The page to write
     * @throws IOException If there's an error writing to disk
     */
    protected abstract void writePageToDisk(int pageId, Object page) throws IOException;
    

    /**
     * Evicts the least recently used page that is not pinned
     */
    private void evictLeastRecentlyUsedPage() {
        Iterator<Integer> iterator = accessOrderQueue.iterator();
        while (iterator.hasNext()) {
            Integer pageId = iterator.next();
            Object page = pageTable.get(pageId);
            
            // Skip if page is pinned
            if (isPagePinned(page)) continue;
            
            // Write back if dirty
            try {
                if (isPageDirty(page)) {
                    writePageToDisk(pageId, page);
                }
                
                // Remove from page table and access order queue
                pageTable.remove(pageId);
                iterator.remove();
                return;
            } catch (IOException e) {
                // Handle potential I/O errors during eviction
                e.printStackTrace();
            }
        }
        
        // If no page can be evicted, throw an exception
        throw new RuntimeException("Cannot evict page: all pages are pinned");
    }
    
    /**
     * @return The next available page ID
     */
    protected int getNextPageId() {
        return nextPageId++;
    }
}
