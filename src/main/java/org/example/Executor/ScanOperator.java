package org.example.Executor;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.Page;
import org.example.BufferManagement.PageImplementation;
import org.example.Rows.Row;

public class ScanOperator implements Operator {
    private final BufferManager bufferManager;
    private final String filePath;
    private int currentPageId;
    private Page currentPage;
    private int currentSlot;
    private boolean isOpen;
//    private final int totalPages;  // total number of pages

    public ScanOperator(BufferManager bufferManager, String filePath) {
        this.bufferManager = bufferManager;
        this.filePath = filePath;
//        this.totalPages = totalPages;
        this.isOpen = false;
    }

    public void open() {
        this.currentPageId = 0;
        this.currentSlot = 0;
        this.isOpen = true;
        this.currentPage = bufferManager.getPage(filePath, currentPageId);
    }

    public int countRows() {
        open();
        int count = 0;
        while (next() != null) {
            count++;
        }
        close();
        return count;
    }




    public Row next() {
        if (!isOpen) return null;
        while (true) {
            if (currentPage == null) {
                return null; // No more pages to scan
            }

            Row row=null;
            if (filePath.contains("movies.data")) {
                row = ((PageImplementation) currentPage).getDataRowBySlot(currentSlot);
            } else if (filePath.contains("workedon.data")) {
                row = ((PageImplementation) currentPage).getWorkedOnRow(currentSlot);
            } else if (filePath.contains("people.data")) {
                row = ((PageImplementation) currentPage).getPeopleRow(currentSlot);
            } else if (filePath.contains("workedon_temp.data")) {
                row = ((PageImplementation) currentPage).getTempRow(currentSlot);
            }else {
                throw new RuntimeException("Unknown filePath: " + filePath);
            }

            if (row != null) {
                currentSlot++;
                return row;
            } else{
                bufferManager.unpinPage(filePath, currentPageId);
                currentPageId++;
                currentPage = bufferManager.getPage(filePath, currentPageId);
                currentSlot = 0;
            }
        }
    }

//    public Row next() {
//
//        while (currentPage != null) {
//            Row row = currentPage.getRow(currentSlot++);
//            if (row != null) return row;
//
//            // Move to next page
//            bufferManager.unpinPage(filePath, currentPageId);
//            currentPageId++;
//            currentSlot = 0;
//            currentPage = bufferManager.getPage(filePath, currentPageId);
//        }
//        return null; // End of file
//    }

    public void close() {
        if (currentPage != null) {
            bufferManager.unpinPage(filePath, currentPageId);
        }
        this.isOpen = false;
    }
}
