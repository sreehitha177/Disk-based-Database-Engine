package org.example;

import java.util.ArrayList;
import java.util.List;

public class ScanOperator implements Operator {
    private final BufferManager bufferManager;
    private final String filePath;
    private int currentPageId;
    private Page currentPage;
    private int currentSlot;
    private boolean isOpen;
    private final int totalPages;  // total number of pages

    public ScanOperator(BufferManager bufferManager, String filePath, int totalPages) {
        this.bufferManager = bufferManager;
        this.filePath = filePath;
        this.totalPages = totalPages;
        this.isOpen = false;
    }

    public void open() {
        this.currentPageId = 0;
        this.currentSlot = 0;
        this.isOpen = true;
        this.currentPage = bufferManager.getPage(filePath, currentPageId);
    }

    public Row next() {
        if (!isOpen) return null;

        while (true) {
            if (currentPage == null) {
                return null; // No more pages to scan
            }

            Row row = currentPage.getRow(currentSlot);

            if (row != null) {
                currentSlot++;
                return row;
            } else {
                // Move to next page
                bufferManager.unpinPage(filePath, currentPageId);
                currentPageId++;
                if (currentPageId >= totalPages) {
                    return null; // No more pages
                }
                currentPage = bufferManager.getPage(filePath, currentPageId);
                currentSlot = 0;
            }
        }
    }

    public void close() {
        if (currentPage != null) {
            bufferManager.unpinPage(filePath, currentPageId);
        }
        this.isOpen = false;
    }
}
