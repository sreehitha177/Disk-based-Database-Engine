package org.example.BufferManagement;

// *** new import  ***
import org.example.metrics.PageCounter;

import org.example.utilities_new;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class BufferManagerImplementation implements BufferManager {

    /* ------------------------------------------------------------------ */
    private final Map<String, PageImplementation>       pageTables;
    private final LinkedHashMap<String, PageImplementation> lruCaches;
    private final int           bufferSize;
    private final Set<String>   dirtyPages;
    private final Map<String,Integer> filePathCount;

    private static final Logger log =
            Logger.getLogger(BufferManagerImplementation.class.getName());

    public BufferManagerImplementation(int bufferSize) {
        this.bufferSize    = bufferSize;
        this.pageTables    = new HashMap<>();
        this.lruCaches     = new LinkedHashMap<>(bufferSize,0.75f,true);
        this.dirtyPages    = new HashSet<>();
        this.filePathCount = new HashMap<>();
    }

    /* ==================================================================
                                  Public API
       ================================================================== */
    @Override
    public synchronized Page getPage(String filePath, int pageId) {
        String key = filePath + ":" + pageId;

        /* ---------- buffer hit --------------------------------------- */
        if (pageTables.containsKey(key)) {
            PageImplementation pg = pageTables.get(key);
            utilities_new.pinAndUpdateLRU(pg, lruCaches, key);
            return pg;
        }

        /* ---------- need a free frame -------------------------------- */
        if (getTotalPages() >= bufferSize && areAllPagesPinned()) {
            return null;                    // cannot bring the page in
        }

        /* === I/O instrumentation === */
        PageCounter.incRead();              // count 1 physical read

        /* ---------- read from disk ----------------------------------- */
        PageImplementation pg = loadPageFromDisk(pageId, filePath);
        if (pg != null) addToBuffer(pg, key, filePath);
        return pg;
    }

    @Override
    public synchronized Page createPage(String filePath) {
        if (getTotalPages() >= bufferSize && areAllPagesPinned()) return null;

        int newPageId = findNextPageId(filePath);
        PageImplementation pg = new PageImplementation(newPageId, 4096);
        addToBuffer(pg, filePath + ":" + newPageId, filePath);
        return pg;
    }

    @Override public synchronized void markDirty(String fp,int pid){
        dirtyPages.add(fp+":"+pid);
    }

    @Override public synchronized void unpinPage(String fp,int pid){
        PageImplementation pg = pageTables.get(fp+":"+pid);
        if (pg!=null) pg.unpin();
    }

    @Override
    public synchronized void force(String filePath) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath,"rw")){
            for (String key : dirtyPages){
                if (!key.startsWith(filePath + ":")) continue;

                int pageId = Integer.parseInt(key.split(":")[1]);
                Page pg    = pageTables.get(key);

                /* === I/O instrumentation === */
                PageCounter.incWrite();     // flush counts as a write

                raf.seek((long) pageId * 4096);
                raf.write(pg.getData());
            }
        } catch (IOException e){ e.printStackTrace(); }

        dirtyPages.removeIf(k -> k.startsWith(filePath + ":"));
    }

    /* ==================================================================
                                 Private helpers
       ================================================================== */
    private int  getTotalPages(){ return pageTables.size(); }
    private boolean areAllPagesPinned(){
        for (PageImplementation p : pageTables.values())
            if (p.getPinCount()==0) return false;
        return true;
    }

    private PageImplementation loadPageFromDisk(int pageId,String fp){
        try (RandomAccessFile f = new RandomAccessFile(fp,"r")){
            f.seek((long) pageId * 4096);
            byte[] data = new byte[4096];
            f.readFully(data);
            PageImplementation pg = new PageImplementation(pageId, data);
            pg.setFilePath(fp);
            return pg;
        } catch (IOException e){ return null; }
    }

    private void addToBuffer(PageImplementation pg,String key,String fp){
        if (getTotalPages() >= bufferSize) evictPage();
        pageTables.put(key, pg);
        utilities_new.pinAndUpdateLRU(pg, lruCaches, key);
    }

    private void evictPage(){
        Iterator<Map.Entry<String,PageImplementation>> it = lruCaches.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,PageImplementation> e = it.next();
            PageImplementation pg = e.getValue();
            if (pg.getPinCount() > 0) continue;           // keep pinned pages

            String key = pg.getFilePath() + ":" + pg.getPid();
            if (dirtyPages.contains(key)){
                writePageToDisk(pg, pg.getFilePath());
                dirtyPages.remove(key);
            }
            pageTables.remove(key);
            it.remove();
            return;
        }
        log.warning("All pages are pinned – eviction impossible");
    }

    private void writePageToDisk(Page pg,String fp){
        /* === I/O instrumentation === */
        PageCounter.incWrite();             // eviction write

        try (RandomAccessFile f = new RandomAccessFile(fp,"rw")){
            f.seek((long) pg.getPid() * 4096);
            f.write(pg.getData());
        } catch (IOException ignored){}
    }

    private int findNextPageId(String fp){
        int id = filePathCount.getOrDefault(fp,0);
        filePathCount.put(fp,id+1);
        return id;
    }
}