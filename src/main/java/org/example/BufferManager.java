package org.example;

public interface BufferManager {
    Page getPage(String filePath, int pageId);
    Page createPage(String filePath);
    void markDirty(String filePath, int pageId);
    void unpinPage(String filePath, int pageId);
    void force(String filePath);
//    void flushAllPages(String filePath);
}
