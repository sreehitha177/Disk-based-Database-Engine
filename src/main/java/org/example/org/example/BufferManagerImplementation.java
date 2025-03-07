package org.example;
import java.io.*;
import java.util.*;

public class BufferManagerImplementation extends BufferManager {
    private final Map<Integer, Page> pageCache;  // A cache to store pages
    private int currentPageId;  // Track the current page ID
    private final  Map<Integer, Integer>pinCount;
    private final Set<Integer>dirtyPages;
    private final Map<Integer, PageImplementation> pageTable;
    private final File storageFile;
    final Deque<Integer>lruQueue;


    public BufferManagerImplementation(int bufferSize, String filePath) {
        super(bufferSize);
        this.lruQueue = new LinkedList<>();
        this.pageCache = new HashMap<>();
        this.currentPageId = 0;  // Start with page ID 0
        this.pinCount=new HashMap<>();
        this.dirtyPages=new HashSet<>();
        this.pageTable = new HashMap<>();
        this.storageFile = new File(filePath);


        try {
            // Create file if it does not exist
            if (!storageFile.exists()) {
                storageFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create storage file: " + filePath);
        }
    }


    @Override
    public Page getPage(int pageId) {
        if (pageTable.containsKey(pageId)) {
            lruQueue.remove(pageId);
            lruQueue.addLast(pageId);
            pinCount.put(pageId, pinCount.getOrDefault(pageId, 0) + 1);
            System.out.println("Accessed page: "+pageId+", Updated LRU queue: "+lruQueue);
            return pageTable.get(pageId);
        }


        // If page is not in memory, try loading it from disk
        Page page = loadPageFromDisk(pageId);
        if (page == null) {
            // If the page does not exist on disk, create a new page
            page = createPage();
            System.out.println("Page " + pageId + " not found. Creating a new one.");
        }
        return page;
    }

    @Override
    public Page createPage() {
        if (pageTable.size() >= bufferSize) {
            evictPage();
        }
        PageImplementation newPage = new PageImplementation(currentPageId++, 100); // Assuming max 100 rows
        pageTable.put(currentPageId, newPage);
        lruQueue.addLast(currentPageId);
//        lruQueue.addLast(nextPageId);
        pinCount.put(currentPageId, 1);
//        nextPageId++;
        return newPage;
    }



    @Override
    public void markDirty(int pageId) {
        dirtyPages.add(pageId);
        System.out.println("Page " + pageId + " marked as dirty.");
    }




    }
}





