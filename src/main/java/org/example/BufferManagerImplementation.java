package org.example;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

//BufferManagerImplementation
public class BufferManagerImplementation extends BufferManager{

    private final Map<String, Map<Integer, PageImplementation>> filePageTables; // Maps fileId -> (pageId -> Page)
    private final Map<String, LinkedHashMap<Integer, PageImplementation>> fileLruCaches; // Maps fileId -> LRU Cache
    private final Map<String, String> filePaths; // Maps fileId -> filePath
    private final Map<String, Integer> fileCurrentPageIds; // Maps fileId -> current max pageId
    private final int pageSize = 4096; // 4KB page size

    public BufferManagerImplementation(int bufferSize) {
        this(bufferSize, null);
    }

    public BufferManagerImplementation(int bufferSize, String filePath) {
        super(bufferSize);
        this.filePageTables = new HashMap<>();
        this.fileLruCaches = new HashMap<>();
        this.filePaths = new HashMap<>();
        this.fileCurrentPageIds = new HashMap<>();
        
        if (filePath != null && !filePath.equals(" ")) {
            registerFile("default", filePath);
        } else {
            try {
                File tempFile = File.createTempFile("buffer_manager_", ".bin");
                registerFile("default", tempFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file", e);
            }
        }
    }
    
    /**
     * Registers a new file with the buffer manager
     * @param fileId The identifier for the file
     * @param filePath The path to the file
     */
    public void registerFile(String fileId, String filePath) {
        System.out.println("Registering file: " + fileId + " at path: " + filePath);
        
        if (!filePageTables.containsKey(fileId)) {
            filePageTables.put(fileId, new HashMap<>());
            fileLruCaches.put(fileId, new LinkedHashMap<>(bufferSize, 0.75f, true));
            filePaths.put(fileId, filePath);
            fileCurrentPageIds.put(fileId, 0);
            createFileIfNotExists(fileId);
            System.out.println("File registered successfully: " + fileId);
        } else {
            System.out.println("File already registered: " + fileId);
        }
    }

    /**
     * Gets the current max page ID for a file
     * @param fileId The file identifier
     * @return The current max page ID
     */
    public int getCurrentPageId(String fileId) {
        return fileCurrentPageIds.getOrDefault(fileId, 0);
    }

    @Override
    public synchronized Page getPage(int pageId) {
        // For backward compatibility, use the default file
        return getPage("default", pageId);
    }
    
    /**
     * Fetches a page from memory if available; otherwise, loads it from disk.
     * The page is immediately pinned.
     * @param fileId The ID of the file containing the page
     * @param pageId The ID of the page to fetch
     * @return The Page object whose content is stored in a frame of the buffer pool manager
     */
    public synchronized Page getPage(String fileId, int pageId) {
        System.out.println("Getting page " + pageId + " from file " + fileId);
        
        // Check if the file is registered
        if (!filePageTables.containsKey(fileId)) {
            System.out.println("Error: File " + fileId + " not registered");
            return null;
        }
        
        Map<Integer, PageImplementation> pageTable = filePageTables.get(fileId);
        LinkedHashMap<Integer, PageImplementation> lruCache = fileLruCaches.get(fileId);
        
        if (pageTable.containsKey(pageId)) {
            PageImplementation page = pageTable.get(pageId);
            Utilities.pinAndUpdateLRU(page, lruCache);
            System.out.println("Page " + pageId + " found in buffer");
            return page;
        }

        // Count total pages across all files
        int totalPages = 0;
        for (Map<Integer, PageImplementation> pt : filePageTables.values()) {
            totalPages += pt.size();
        }
        
        if (totalPages >= bufferSize && !hasUnpinnedPages()) {
            System.out.println("Buffer is full and all pages are pinned! Cannot fetch or create a page.");
            return null;
        }

        PageImplementation page = loadPageFromDisk(fileId, pageId);
        addToBuffer(fileId, page);
        System.out.println("Page " + pageId + " loaded from disk");
        return page;
    }

    @Override
    public synchronized Page createPage() {
        // For backward compatibility, use the default file
        return createPage("default");
    }
    
    /**
     * Creates a new page in the specified file.
     * The page is immediately pinned.
     * @param fileId The ID of the file to create the page in
     * @return The Page object whose content is stored in a frame of the buffer pool manager
     */
    public synchronized Page createPage(String fileId) {
        System.out.println("Creating new page in file " + fileId);
        
        // Check if the file is registered
        if (!filePageTables.containsKey(fileId)) {
            System.out.println("Error: File " + fileId + " not registered");
            return null;
        }
        
        // Count total pages across all files
        int totalPages = 0;
        for (Map<Integer, PageImplementation> pt : filePageTables.values()) {
            totalPages += pt.size();
        }
        
        if (totalPages >= bufferSize && !hasUnpinnedPages()) {
            System.out.println("Buffer is full and all pages are pinned! Cannot fetch or create a page.");
            return null;
        }

        int newPageId = fileCurrentPageIds.get(fileId);
        fileCurrentPageIds.put(fileId, newPageId + 1);
        
        PageImplementation newPage = new PageImplementation(newPageId, pageSize);
        addToBuffer(fileId, newPage);
        System.out.println("Created new page " + newPageId + " in file " + fileId);
        return newPage;
    }

    private void addToBuffer(String fileId, PageImplementation page) {
        // Count total pages across all files
        int totalPages = 0;
        for (Map<Integer, PageImplementation> pt : filePageTables.values()) {
            totalPages += pt.size();
        }
        
        if (totalPages >= bufferSize) {
            evictPage(); // Evict if buffer is full
        }

        Map<Integer, PageImplementation> pageTable = filePageTables.get(fileId);
        LinkedHashMap<Integer, PageImplementation> lruCache = fileLruCaches.get(fileId);
        
        pageTable.put(page.getPid(), page);
        Utilities.pinAndUpdateLRU(page, lruCache);
    }

    @Override
    public synchronized void markDirty(int pageId) {
        // For backward compatibility, use the default file
        markDirty("default", pageId);
    }
    
    /**
     * Marks a page as dirty, indicating it needs to be written to disk before eviction.
     * @param fileId The ID of the file containing the page
     * @param pageId The ID of the page to mark as dirty
     */
    public synchronized void markDirty(String fileId, int pageId) {
        if (filePageTables.containsKey(fileId)) {
            Map<Integer, PageImplementation> pageTable = filePageTables.get(fileId);
            if (pageTable.containsKey(pageId)) {
                pageTable.get(pageId).markDirty();
                System.out.println("Marked page " + pageId + " in file " + fileId + " as dirty");
            }
        }
    }

    @Override
    public synchronized void unpinPage(int pageId) {
        // For backward compatibility, use the default file
        unpinPage("default", pageId);
    }
    
    /**
     * Unpins a page in the buffer pool, allowing it to be evicted if necessary.
     * @param fileId The ID of the file containing the page
     * @param pageId The ID of the page to unpin
     */
    public synchronized void unpinPage(String fileId, int pageId) {
        if (filePageTables.containsKey(fileId)) {
            Map<Integer, PageImplementation> pageTable = filePageTables.get(fileId);
            if (pageTable.containsKey(pageId)) {
                PageImplementation page = pageTable.get(pageId);
                page.unpin();
                System.out.println("Unpinned page " + pageId + " in file " + fileId);
            }
        }
    }
    
    /**
     * Forces all dirty pages to be written to disk
     */
    public synchronized void force() {
        System.out.println("Forcing all dirty pages to disk");
        
        for (String fileId : filePageTables.keySet()) {
            Map<Integer, PageImplementation> pageTable = filePageTables.get(fileId);
            for (PageImplementation page : pageTable.values()) {
                if (page.isDirty()) {
                    writePageToDisk(fileId, page);
                    System.out.println("Wrote dirty page " + page.getPid() + " from file " + fileId + " to disk");
                }
            }
        }
    }

    private boolean hasUnpinnedPages() {
        for (Map<Integer, PageImplementation> pageTable : filePageTables.values()) {
            for (PageImplementation page : pageTable.values()) {
                if (page.getPinCount() == 0) {
                    return true; // There is an unpinned page that can be evicted
                }
            }
        }
        return false;
    }

    private void evictPage() {
        System.out.println("Attempting to evict a page");
        
        // Try to find an unpinned page in any file
        for (String fileId : fileLruCaches.keySet()) {
            LinkedHashMap<Integer, PageImplementation> lruCache = fileLruCaches.get(fileId);
            Map<Integer, PageImplementation> pageTable = filePageTables.get(fileId);
            
            Iterator<Map.Entry<Integer, PageImplementation>> it = lruCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, PageImplementation> entry = it.next();
                PageImplementation page = entry.getValue();
                if (page.getPinCount() == 0) {
                    if (page.isDirty()) {
                        writePageToDisk(fileId, page);
                    }
                    pageTable.remove(page.getPid());
                    it.remove();
                    System.out.println("Evicted page " + page.getPid() + " from file " + fileId);
                    return;
                }
            }
        }
        
        System.err.println("All pages are pinned, no eviction possible!");
    }

    private PageImplementation loadPageFromDisk(String fileId, int pageId) {
        String filePath = filePaths.get(fileId);
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek((long) pageId * pageSize);
            byte[] data = new byte[pageSize];
            file.readFully(data);
            if (isEmpty(data)) throw new IOException();
            return new PageImplementation(pageId, data);
        } catch (IOException e) {
            return new PageImplementation(pageId, pageSize); // New empty page if not found
        }
    }

    private boolean isEmpty(byte[] data) {
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
    }

    private void writePageToDisk(String fileId, PageImplementation page) {
        String filePath = filePaths.get(fileId);
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek((long) page.getPid() * pageSize);
            file.write(page.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFileIfNotExists(String fileId) {
        String filePath = filePaths.get(fileId);
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile(); // Creates the file if it doesn't exist
                System.out.println("Created new file: " + filePath);
            }
            // We don't want to clear the file if it already exists, as it might contain valid data
            if (file.length() == 0) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    // No need to initialize anything for an empty file
                }
                System.out.println("Initialized empty file: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
