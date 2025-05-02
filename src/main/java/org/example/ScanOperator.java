package org.example;

import java.nio.charset.StandardCharsets;
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

            Row row=null;
            if (filePath.contains("movies.data")) {
                row = ((PageImplementation) currentPage).getDataRowBySlot(currentSlot);
//                System.out.println("Scanning Page " + currentPageId + ", Slot " + currentSlot);
//                if (row instanceof DataRow) {
//                    DataRow dr = (DataRow) row;
//                    System.out.println("Scanned movieId: '" + new String(dr.getMovieId()).trim() +
//                            "', title: '" + new String(dr.getTitle()).trim() + "'");
//                }

            } else if (filePath.contains("workedon.data")) {
                row = ((PageImplementation) currentPage).getWorkedOnRow(currentSlot);
//                System.out.println("Scanning Page " + currentPageId + ", Slot " + currentSlot);
//                if (row instanceof WorkedOnRow) {
//                    WorkedOnRow wr = (WorkedOnRow) row;
//                    System.out.println("Scanned movieId: '" + new String(wr.getMovieId()).trim() +
//                            "', personId: '" + new String(wr.getPersonId()).trim() + "', category: '" + new String(wr.getCategory()).trim() + "'");
//                }
            } else if (filePath.contains("people.data")) {
                row = ((PageImplementation) currentPage).getPeopleRow(currentSlot);
//                if (row instanceof PeopleRow) {
//                    PeopleRow pr = (PeopleRow) row;
//                    System.out.println("Scanned personId: '" + new String(pr.getPersonId(), StandardCharsets.ISO_8859_1).trim() +
//                            "', name: '" + new String(pr.getName(), StandardCharsets.ISO_8859_1).trim() + "'");
//                }
//                else {
//                    System.out.println("Warning: Non-PeopleRow returned from people.data");
//                }
            } else if (filePath.contains("workedon_temp.data")) {
                row = ((PageImplementation) currentPage).getTempRow(currentSlot);
//                if (row instanceof TempRow) {
//                    TempRow tr = (TempRow) row;
//                    System.out.println("Scanned movieId: '" + new String(tr.getMovieId()).trim() +
//                            "', personId: '" + new String(tr.getPersonId()).trim() + "'");
//                }
            }else {
                throw new RuntimeException("Unknown filePath: " + filePath);
            }



            if (row != null) {
                currentSlot++;
                return row;
            } else {
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
