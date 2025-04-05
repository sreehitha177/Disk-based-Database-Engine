//package org.example;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.util.*;
//
////public class BufferManagerImplementation extends BufferManager {
////    private final Map<String, Map<Integer, PageImplementation>> filePageTables;
////    private final Map<String, LinkedHashMap<Integer, PageImplementation>> fileLruCaches;
////    private final int bufferSize;
////
////    public BufferManagerImplementation(int bufferSize) {
////        super(bufferSize);
////        this.bufferSize = bufferSize;
////        this.filePageTables = new HashMap<>();
////        this.fileLruCaches = new HashMap<>();
////    }
//
//public class BufferManagerImplementation extends BufferManager {
//    private final Map<String, Map<Integer, PageImplementation>> filePageTables;
//    private final Map<String, LinkedHashMap<Integer, PageImplementation>> fileLruCaches;
//    private final Map<String, String> filePaths; // Tracks physical file locations
//    private final int bufferSize;
//    private final int pageSize = 4096;
//    private int currentPageId = 0;
//
//    public BufferManagerImplementation(int bufferSize) {
//        this(bufferSize, Collections.emptyMap());
//    }
//
//    public BufferManagerImplementation(int bufferSize, Map<String, String> initialFiles) {
//        super(bufferSize);
//        this.bufferSize = bufferSize;
//        this.filePageTables = new HashMap<>();
//        this.fileLruCaches = new HashMap<>();
//        this.filePaths = new HashMap<>(); // Initialize here
//
//        // Initialize default files if none provided
//        if (initialFiles.isEmpty()) {
//            initializeDefaultFiles();
//        } else {
//            initialFiles.forEach((fileName, path) ->
//                    initializeFile(fileName, path));
//        }
//    }
//
//    private void initializeDefaultFiles() {
//        try {
//            filePaths.put("movies.data", createTempFile("movies_data_", ".bin"));
//            filePaths.put("movies.title.idx", createTempFile("movies_title_idx_", ".bin"));
//            filePaths.put("movies.movield.idx", createTempFile("movies_id_idx_", ".bin"));
//
//            // Initialize structures for each file
//            filePaths.keySet().forEach(fileName -> {
//                filePageTables.put(fileName, new HashMap<>());
//                fileLruCaches.put(fileName, createLruCache());
//            });
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to initialize default files", e);
//        }
//    }
//
//    private String createTempFile(String prefix, String suffix) throws IOException {
//        File tempFile = File.createTempFile(prefix, suffix);
//        tempFile.deleteOnExit();
//        return tempFile.getAbsolutePath();
//    }
//
//    private LinkedHashMap<Integer, PageImplementation> createLruCache() {
//        return new LinkedHashMap<>(bufferSize, 0.75f, true) {
//            @Override
//            protected boolean removeEldestEntry(
//                    Map.Entry<Integer, PageImplementation> eldest) {
//                return size() > bufferSize;
//            }
//        };
//    }
//
//    private void initializeFile(String fileName, String path) {
//        filePaths.put(fileName, path);
//        filePageTables.put(fileName, new HashMap<>());
//        fileLruCaches.put(fileName, createLruCache());
//
//        try {
//            new File(path).createNewFile();
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to create file: " + path, e);
//        }
//    }
//
////    private void initializeFile(String fileName, String filePath) {
////        // Create empty page table and LRU cache for this file
////        filePageTables.put(fileName, new HashMap<>());
////        fileLruCaches.put(fileName, new LinkedHashMap<>(bufferSize, 0.75f, true));
////
////        // Create physical file if it doesn't exist
////        try {
////            File file = new File(filePath);
////            if (!file.exists()) {
////                file.createNewFile();
////            }
////        } catch (IOException e) {
////            throw new RuntimeException("Failed to initialize file: " + filePath, e);
////        }
////    }
//
//    @Override
//    public Page getPage(String fileName, int pageId) {
//        Map<Integer, PageImplementation> pageTable = filePageTables.computeIfAbsent(fileName, k -> new HashMap<>());
//        LinkedHashMap<Integer, PageImplementation> lruCache = fileLruCaches.computeIfAbsent(fileName, k -> new LinkedHashMap<>(bufferSize, 0.75f, true));
//
//        if (pageTable.containsKey(pageId)) {
//            PageImplementation page = pageTable.get(pageId);
//            utilities_new.pinAndUpdateLRU(page, lruCache);
//            return page;
//        }
//
//        if (getTotalPages() >= bufferSize && !hasUnpinnedPages()) {
//            System.out.println("Buffer is full and all pages are pinned! Cannot fetch or create a page.");
//            return null;
//        }
//
//        PageImplementation page = loadPageFromDisk(fileName, pageId);
//        addToBuffer(fileName, page);
//        return page;
//    }
//
//    @Override
//    public Page createPage(String fileName) {
//        if (getTotalPages() >= bufferSize && !hasUnpinnedPages()) {
//            System.out.println("Buffer is full and all pages are pinned! Cannot fetch or create a page.");
//            return null;
//        }
//
//        Map<Integer, PageImplementation> pageTable = filePageTables.computeIfAbsent(fileName, k -> new HashMap<>());
//        int newPageId = currentPageId++;
//        PageImplementation newPage = new PageImplementation(newPageId, Page.PAGE_SIZE);
//        addToBuffer(fileName, newPage);
//        return newPage;
//    }
//
//    @Override
//    public void markDirty(String fileName, int pageId) {
//        Map<Integer, PageImplementation> pageTable = filePageTables.get(fileName);
//        if (pageTable != null && pageTable.containsKey(pageId)) {
//            pageTable.get(pageId).markDirty();
//        }
//    }
//
//    @Override
//    public void unpinPage(String fileName, int pageId) {
//        Map<Integer, PageImplementation> pageTable = filePageTables.get(fileName);
//        if (pageTable != null && pageTable.containsKey(pageId)) {
//            pageTable.get(pageId).unpin();
//        }
//    }
//
//    @Override
//    public void force(String fileName) {
//        Map<Integer, PageImplementation> pageTable = filePageTables.get(fileName);
//        if (pageTable != null) {
//            for (PageImplementation page : pageTable.values()) {
//                if (page.isDirty()) {
//                    writePageToDisk(fileName, page);
//                    page.markDirty();
//                }
//            }
//        }
//    }
//
//    // Helper methods
//    private int getTotalPages() {
//        return filePageTables.values().stream().mapToInt(Map::size).sum();
//    }
//
//    private boolean hasUnpinnedPages() {
//        return filePageTables.values().stream()
//                .flatMap(m -> m.values().stream())
//                .anyMatch(page -> page.getPinCount() == 0);
//    }
//
//    private int getNextPageId(String fileName) {
//        return filePageTables.getOrDefault(fileName, Collections.emptyMap()).size();
//    }
//
//    private void addToBuffer(String fileName, PageImplementation page) {
//        Map<Integer, PageImplementation> pageTable = filePageTables.computeIfAbsent(fileName, k -> new HashMap<>());
//        LinkedHashMap<Integer, PageImplementation> lruCache = fileLruCaches.computeIfAbsent(fileName, k -> new LinkedHashMap<>(bufferSize, 0.75f, true));
//
//        if (getTotalPages() >= bufferSize) {
//            evictPage();
//        }
//
//        pageTable.put(page.getPid(), page);
//        utilities_new.pinAndUpdateLRU(page, lruCache);
//    }
//
//    private void evictPage() {
//        for (LinkedHashMap<Integer, PageImplementation> lruCache : fileLruCaches.values()) {
//            Iterator<Map.Entry<Integer, PageImplementation>> it = lruCache.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<Integer, PageImplementation> entry = it.next();
//                PageImplementation page = entry.getValue();
//                if (page.getPinCount() == 0) {
//                    if (page.isDirty()) {
//                        writePageToDisk(getFileNameForPage(page.getPid()), page);
//                    }
//                    filePageTables.get(getFileNameForPage(page.getPid())).remove(page.getPid());
//                    it.remove();
//                    return;
//                }
//            }
//        }
//        System.err.println("All pages are pinned, no eviction possible!");
//    }
//
//    private String getFileNameForPage(int pageId) {
//        return filePageTables.entrySet().stream()
//                .filter(e -> e.getValue().containsKey(pageId))
//                .map(Map.Entry::getKey)
//                .findFirst()
//                .orElse(null);
//    }
//
//    private PageImplementation loadPageFromDisk(String fileName, int pageId) {
//        try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
//            file.seek((long) pageId * Page.PAGE_SIZE);
//            byte[] data = new byte[Page.PAGE_SIZE];
//            file.readFully(data);
//            return new PageImplementation(pageId, data);
//        } catch (IOException e) {
//            return new PageImplementation(pageId, Page.PAGE_SIZE);
//        }
//    }
//
//    private void writePageToDisk(String fileName, PageImplementation page) {
//        try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
//            file.seek((long) page.getPid() * Page.PAGE_SIZE);
//            file.write(page.getData());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}



package org.example;


//import com.example.utils.BufferUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class BufferManagerImplementation implements BufferManager {
    private  Map<String, PageImplementation> pageTables;
    private  LinkedHashMap<String, PageImplementation> lruCaches;
    private final int bufferSize;
    private final Set<String> dirtyPages;
    private final Map<String, Integer> filePathCount;

    private static final Logger log = Logger.getLogger(BufferManagerImplementation.class.getName());


    public BufferManagerImplementation(int bufferSize) {
        super();
        this.bufferSize = bufferSize;
        this.pageTables = new HashMap<>();
        this.lruCaches = new LinkedHashMap<>(bufferSize, 0.75f, true);
        this.dirtyPages = new HashSet<>();
        this.filePathCount = new HashMap<>();
    }

    @Override
    public synchronized Page getPage(String filePath, int pageId) {
        String key = filePath+":"+pageId;
        if (pageTables.containsKey(key)) {
            PageImplementation page = pageTables.get(key);
            utilities_new.pinAndUpdateLRU(page,lruCaches,key);
            return page;
        }

//        if (getTotalPages() >= bufferSize && areAllPagesPinned()) {
//            return null;
//        }

        PageImplementation page = loadPageFromDisk(pageId, filePath);
        if(page!=null)
            addToBuffer(page,key,filePath);
        return page;
    }

    @Override
    public synchronized Page createPage(String filePath) {
        if (getTotalPages() >= bufferSize && areAllPagesPinned()) {
            return null;
        }

        int newPageId = findNextPageId(filePath);
        PageImplementation newPage = new PageImplementation(newPageId, 4096); // Default to data page

        addToBuffer(newPage,filePath+":"+newPageId,filePath);
        return newPage;

    }

    @Override
    public synchronized void markDirty(String filePath, int pageId) {
        dirtyPages.add(filePath + ":" + pageId);
    }

    @Override
    public synchronized void unpinPage(String filePath, int pageId) {
        String key = filePath+":"+pageId;
        if (pageTables.containsKey(key)) {
            PageImplementation page = pageTables.get(key);
            if (page != null) {
                page.unpin();
            }
        }
    }

//    @Override
//    public void force(String filePath) {
//        flushAllPages(filePath);
//    }

    @Override
    public synchronized void force(String filePath) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            for (String key : dirtyPages) {
                if (key.startsWith(filePath + ":")) {
                    int pageId = Integer.parseInt(key.split(":")[1]);
                    Page page = pageTables.get(key);
                    file.seek((long) pageId * 4096);
                    file.write(page.getData());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dirtyPages.removeIf(key -> key.startsWith(filePath + ":"));
    }

    private int getTotalPages() {
        return pageTables.size();
    }

    private boolean areAllPagesPinned() {
        for (PageImplementation page : pageTables.values()) {
            if (page.getPinCount() == 0) {
                return false; // There is an unpinned page that can be evicted
            }
        }
        return true; // No unpinned pages, buffer is fully pinned
    }

    private PageImplementation loadPageFromDisk(int pageId, String filePath) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek((long) pageId * 4096);
            byte[] data = new byte[4096];
            file.readFully(data);
            return new PageImplementation(pageId, data);
        } catch (IOException e) {
            return new PageImplementation(pageId, 4096);
        }
    }

    private void addToBuffer(PageImplementation page, String key, String filePath) {
        if (getTotalPages() >= bufferSize) {
            evictPage(filePath);
        }

        pageTables.put(key, page);
        utilities_new.pinAndUpdateLRU(page,lruCaches,key); // ✅ Use helper function
    }

    private void evictPage(String filePath) {
        Iterator<Map.Entry<String, PageImplementation>> it = lruCaches.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PageImplementation> entry = it.next();
            PageImplementation page = entry.getValue();
            String key = filePath + ":" +page.getPid();
            if (page.getPinCount() == 0) { // ✅ Evict only unpinned pages
                if (dirtyPages.contains(key)) {
                    writePageToDisk(page, filePath);
                    dirtyPages.remove(key);
                }
                pageTables.remove(key);
                it.remove();
                return;
            }
        }
        log.warning(("All pages are pinned, no eviction possible!"));
    }

    private void writePageToDisk(Page page, String filePath) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek((long) page.getPid() * 4096);
            file.write(page.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int findNextPageId(String filePath) {
        int maxId = filePathCount.getOrDefault(filePath,0);
        filePathCount.put(filePath,maxId+1);
        return maxId;
    }
}