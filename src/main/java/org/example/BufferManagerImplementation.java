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
