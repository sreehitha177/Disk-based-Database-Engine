package org.example;

import java.io.*;
import java.util.*;

public class BufferManagerImplementation extends BufferManager {
    private final Map<Integer, Page> pageCache;
    private int currentPageId;
    private final Map<Integer, Integer> pinCount;
    private final Set<Integer> dirtyPages;
    private final Map<Integer, PageImplementation> pageTable;
    private final File storageFile;
    final Deque<Integer> lruQueue;

    public BufferManagerImplementation(int bufferSize, String filePath) {
        super(bufferSize);
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

    @Override
    public Page getPage(int pageId) {
        if (pageTable.containsKey(pageId)) {
            lruQueue.remove(pageId);
            lruQueue.addLast(pageId);
            pinCount.put(pageId, pinCount.getOrDefault(pageId, 0) + 1);
            return pageTable.get(pageId);
        }
        if (!isPageOnDisk(pageId)) {
            throw new IllegalArgumentException("Page " + pageId + " does not exist.");
        }
        return loadPageFromDisk(pageId);
    }

    @Override
    public Page createPage() {
        if (pageTable.size() >= bufferSize) {
            evictPage();
        }
        PageImplementation newPage = new PageImplementation(currentPageId, 100);
        pageTable.put(currentPageId, newPage);
        lruQueue.addLast(currentPageId);
        pinCount.put(currentPageId, 1);
        return newPage;
    }

    @Override
    public void markDirty(int pageId) {
        dirtyPages.add(pageId);
    }

    @Override
    public void unpinPage(int pageId) {
        pinCount.put(pageId, Math.max(0, pinCount.getOrDefault(pageId, 0) - 1));
    }

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

    private void writePageToDisk(PageImplementation page) {
        try (RandomAccessFile file = new RandomAccessFile(storageFile, "rw")) {
            file.seek(page.getPid() * 4096L);
            file.write(serializePage(page));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPageOnDisk(int pageId) {
        return storageFile.exists();
    }

    private byte[] serializePage(PageImplementation page) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(page.getRows());
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

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

    // Getter methods for test accessibility
    public Set<Integer> getDirtyPages() {
        return dirtyPages;
    }

    public Map<Integer, Integer> getPinCount() {
        return pinCount;
    }
}