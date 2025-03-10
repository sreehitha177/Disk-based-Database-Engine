package org.example;

import java.io.*;
import java.util.*;

public class BufferManagerImplementation extends BufferManager {
    private final Map<Integer, Page> pageCache;          // Cache to store pages in memory
    private int currentPageId;          // To racks the current page ID
    private final Map<Integer, Integer> pinCount;           // To Track how many times a page is pinned
    private final Set<Integer> dirtyPages;          // To stores IDs of dirty pages
    private final Map<Integer, PageImplementation> pageTable;           // Maps page IDs to actual page objects
    private final File storageFile;         //File used for storage
    final Deque<Integer> lruQueue;          // Queue for LRU replacement

    //Constructor to initialize buffer manager
    public BufferManagerImplementation(int bufferSize, String filePath) {
        super(bufferSize);
        //Checking if bufferSize is greater than zero
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        
        this.lruQueue = new LinkedList<>();
        this.pageCache = new HashMap<>();
        this.currentPageId = 0;
        this.pinCount = new HashMap<>();
        this.dirtyPages = new HashSet<>();
        this.pageTable = new HashMap<>();
        this.storageFile = new File(filePath);

        try {
            if (!storageFile.exists()) {
                storageFile.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage file: " + filePath, e);
        }
    }

    //Retrieving a page from memory or from disk
    @Override
    public Page getPage(int pageId){
        //Checking if page is in memory
        if(pageTable.containsKey(pageId)){
            lruQueue.remove(pageId);
            lruQueue.addLast(pageId);
            pinCount.put(pageId, pinCount.getOrDefault(pageId, 0)+1);
            System.out.println("Accessed page: "+pageId+", Updated LRU queue: "+lruQueue);
            return pageTable.get(pageId);
        }
        //Loading the page from disk if it isn't in memory
        Page page = loadPageFromDisk(pageId);
        if (page == null) {
            page = createPage();
            System.out.println("Page " + pageId + " not found. Creating a new one.");
        }
        return page;
    }

    //Creating a new page in memory
    @Override
    public Page createPage() {
        if (pageTable.size() >= bufferSize) {
            evictPage();
        }
        PageImplementation newPage = new PageImplementation(currentPageId, 100);
        pageTable.put(currentPageId, newPage);
        lruQueue.addLast(currentPageId);
        pinCount.put(currentPageId, 1);
        currentPageId++;
        return newPage;
    }

    //Marks a page as dirty for it to be written back to disk
    @Override
    public void markDirty(int pageId) {
        dirtyPages.add(pageId);
    }

    //Unpins a page allowing it to be evicted
    @Override
    public void unpinPage(int pageId) {
        pinCount.put(pageId, Math.max(0, pinCount.getOrDefault(pageId, 0) - 1));
    }

    //Evicts a page if needed
    private void evictPage() {
        while (!lruQueue.isEmpty()) {
            int lruPageId = lruQueue.peekFirst();
            if (pinCount.getOrDefault(lruPageId, 0) == 0) {
                lruQueue.pollFirst();
                if (dirtyPages.contains(lruPageId)) {
                    writePageToDisk(pageTable.get(lruPageId));
                    dirtyPages.remove(lruPageId);
                }
                pageTable.remove(lruPageId);
                pinCount.remove(lruPageId);
                return;
            } else {
                lruQueue.removeFirst();
                lruQueue.addLast(lruPageId);
            }
        }
    }

    //Loads a page from the disk into memory
    private PageImplementation loadPageFromDisk(int pageId) {
        try (RandomAccessFile file = new RandomAccessFile(storageFile, "r")) {
            file.seek(pageId * 4096L);
            byte[] data = new byte[4096];
            int bytesRead = file.read(data);
            if (bytesRead == -1) return null;
            PageImplementation page = deserializePage(data, pageId);
            pageTable.put(pageId, page);
            lruQueue.addLast(pageId);
            pinCount.put(pageId, 1);
            return page;
        } catch (IOException e) {
            return null;
        }
    }

    //Writes a page back to disk from memory
    private void writePageToDisk(PageImplementation page) {
        try (RandomAccessFile file = new RandomAccessFile(storageFile, "rw")) {
            file.seek(page.getPid() * 4096L);
            file.write(serializePage(page));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Checks if a page is on disk
    private boolean isPageOnDisk(int pageId) {
        return storageFile.exists();
    }

    //Serializes a page into byte array
    private byte[] serializePage(PageImplementation page) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(page.getRows());
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    //Deserializes a byte array into a page object
    private PageImplementation deserializePage(byte[] data, int pageId) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            List<Row> rows = (List<Row>) in.readObject();
            PageImplementation page = new PageImplementation(pageId, 100);
            rows.forEach(page::insertRow);
            return page;
        } catch (IOException | ClassNotFoundException e) {
            return new PageImplementation(pageId, 100);
        }
    }

    //Returns dirty pages
    public Set<Integer> getDirtyPages() {
        return dirtyPages;
    }

    //Returns the pin count map
    public Map<Integer, Integer> getPinCount() {
        return pinCount;
    }
}