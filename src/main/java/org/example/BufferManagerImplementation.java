package org.example;

//import com.example.utils.BufferUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

//BufferManagerImplementation
public class BufferManagerImplementation extends BufferManager{

    private final Map<Integer, PageImplementation> pageTable; // Maps pageId -> Page
    private final LinkedHashMap<Integer, PageImplementation> lruCache; // Maintains LRU order
    private final String filePath; // Database file
    private final int pageSize = 4096; // 4KB page size
    private int currentPageId = 0;

    public BufferManagerImplementation(int bufferSize) {
        this(bufferSize, null);
    }

    public BufferManagerImplementation(int bufferSize, String filePath) {
        super(bufferSize);
        this.pageTable = new HashMap<>();
        this.lruCache = new LinkedHashMap<>(bufferSize, 0.75f, true);
        if (filePath == null || filePath==" ") {
            try {
                File tempFile = File.createTempFile("buffer_manager_", ".bin");
                this.filePath = tempFile.getAbsolutePath(); // Assign temp file path
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file", e);
            }
        } else {
            this.filePath = filePath;
        }
        createFileIfNotExists();
    }

    @Override
    public synchronized Page getPage(int pageId) {
        if (pageTable.containsKey(pageId)) {
            PageImplementation page = pageTable.get(pageId);
            Utilities.pinAndUpdateLRU(page, lruCache); // ✅ Using Utility
            return page;
        }

        if (pageTable.size() >= bufferSize && hasUnpinnedPages()) {
            System.out.println("Buffer is full and all pages are pinned! Cannot fetch or create a page.");
            return null;  // ✅ Return null instead of throwing an error
        }

        PageImplementation page = loadPageFromDisk(pageId);
        addToBuffer(page);
        return page;
    }

    @Override
    public synchronized Page createPage() {

        // ✅ Before loading from disk, check if buffer is full and all pages are pinned
        if (pageTable.size() >= bufferSize && hasUnpinnedPages()) {
            System.out.println("Buffer is full and all pages are pinned! Cannot fetch or create a page.");
            return null;  // ✅ Return null instead of throwing an error
        }

        int newPageId = currentPageId++;
        PageImplementation newPage = new PageImplementation(newPageId, pageSize);
        addToBuffer(newPage);
        return newPage;
    }


    private void addToBuffer(PageImplementation page) {
        if (pageTable.size() >= bufferSize) {
            evictPage(); // ✅ Evict if buffer is full
        }

        pageTable.put(page.getPid(), page);
        Utilities.pinAndUpdateLRU(page,lruCache); // ✅ Use helper function
    }


    @Override
    public synchronized void markDirty(int pageId) {
        if (pageTable.containsKey(pageId)) {
            pageTable.get(pageId).markDirty();
        }
    }

    @Override
    public synchronized void unpinPage(int pageId) {
        if (pageTable.containsKey(pageId)) {
            PageImplementation page = pageTable.get(pageId);
            page.unpin();
        }
    }

    private boolean hasUnpinnedPages() {
        for (PageImplementation page : pageTable.values()) {
            if (page.getPinCount() == 0) {
                return false; // ✅ There is an unpinned page that can be evicted
            }
        }
        return true; // ❌ No unpinned pages, buffer is fully pinned
    }

    private void evictPage() {
        Iterator<Map.Entry<Integer, PageImplementation>> it = lruCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, PageImplementation> entry = it.next();
            PageImplementation page = entry.getValue();
            if (page.getPinCount() == 0) { // ✅ Evict only unpinned pages
                if (page.isDirty()) {
                    writePageToDisk(page);
                }
                pageTable.remove(page.getPid());
                it.remove();
                return;
            }
        }
        System.err.println(("All pages are pinned, no eviction possible!"));
    }

    private PageImplementation loadPageFromDisk(int pageId) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek((long) pageId * pageSize);
            byte[] data = new byte[pageSize];
            file.readFully(data);
            if(isEmpty(data)) throw new IOException();
            return new PageImplementation(pageId, data);
        } catch (IOException e) {
            return new PageImplementation(pageId, pageSize); // New empty page if not found
        }
    }

    // Check if all bytes are zero
    private boolean isEmpty(byte[] data) {
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
    }

    private void writePageToDisk(PageImplementation page) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek((long) page.getPid() * pageSize);
            file.write(page.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFileIfNotExists() {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile(); // ✅ Creates the file if it doesn’t exist
            }
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.setLength(0); // ✅ Clears the file content
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}