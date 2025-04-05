//package org.example;
//
////BufferManager
//public abstract class BufferManager {
//    //Configurable size of the buffer cache
//    final int bufferSize;
//
//    public BufferManager(int bufferSize) {
//        this.bufferSize = bufferSize;
//    }
//
//    /**
//     * Fetches a page from memory if available; otherwise, loads it from disk.
//     * The page is immediately pinned.
//     * @param pageId The ID of the page to fetch.
//     * @return The Page object whose content is stored in a frame of the buffer pool manager.
//     */
//    public abstract Page getPage(int pageId);
//
//    /**
//     * Creates a new page.
//     * The page is immediately pinned.
//     * @return The Page object whose content is stored in a frame of the buffer pool manager.
//     */
//    public abstract Page createPage();
//
//    /**
//     * Marks a page as dirty, indicating it needs to be written to disk before eviction.
//     * @param pageId The ID of the page to mark as dirty.
//     */
//    public abstract void markDirty(int pageId);
//
//    /**
//     * Unpins a page in the buffer pool, allowing it to be evicted if necessary.
//     * @param pageId The ID of the page to unpin.
//     */
//    public abstract void unpinPage(int pageId);
//
//}


package org.example;

//public abstract class BufferManager {
//    // Add file-specific methods
//    final int bufferSize;
//    public BufferManager(int bufferSize) {
//        this.bufferSize = bufferSize;
//    }
//    public abstract Page getPage(String fileName, int pageId);
//    public abstract Page createPage(String fileName);
//    public abstract void markDirty(String fileName, int pageId);
//    public abstract void unpinPage(String fileName, int pageId);
//    public abstract void force(String fileName);
//}
public interface BufferManager {
    Page getPage(String filePath, int pageId);
    Page createPage(String filePath);
    void markDirty(String filePath, int pageId);
    void unpinPage(String filePath, int pageId);
    void force(String filePath);
//    void flushAllPages(String filePath);
}