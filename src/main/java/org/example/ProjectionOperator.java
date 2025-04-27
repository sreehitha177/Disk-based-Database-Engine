package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectionOperator implements Operator{
    private final SelectionOperator child;  // Child is the selection on WorkedOn
    private final BufferManager bufferManager;
    private final String tempFilePath = "workedon_temp.data"; // You can hardcode the temp file name
    private boolean materialized;
    private ScanOperator tempScan; // To scan after materialization
    private int tempPageCount;     // Number of pages in temp file

    public ProjectionOperator(SelectionOperator child, BufferManager bufferManager) {
        this.child = child;
        this.bufferManager = bufferManager;
        this.materialized = false;
    }

    public void open() {
        child.open();
    }

    public Row next() {
        if (!materialized) {
            materialize();
            tempScan.open();
        }
        return tempScan.next();
    }

    public void close() {
        child.close();
        if (tempScan != null) {
            tempScan.close();
        }
        // Optionally delete the temp file to clean up
        File tempFile = new File(tempFilePath);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    private void materialize() {
        try {
            Page currentPage = bufferManager.createPage(tempFilePath);
            int currentPageId = currentPage.getPid();
            tempPageCount = 1;

            while (true) {
                Row row = child.next();
                if (row == null) break;

                if (row instanceof WorkedOnRow) {
                    WorkedOnRow workedOnRow = (WorkedOnRow) row;

                    // Create a projected row containing only movieId and personId
                    byte[] movieId = workedOnRow.getMovieId();
                    byte[] personId = workedOnRow.getPersonId();

//                    LeafRow projectedRow = new LeafRow(movieId, new Rid(0, 0)); // Dummy Rid, not important for join matching
                    TempRow projectedRow = new TempRow(movieId, personId);

                    if (currentPage.isFull()) {
                        bufferManager.unpinPage(tempFilePath, currentPageId);
                        currentPage = bufferManager.createPage(tempFilePath);
                        currentPageId = currentPage.getPid();
                        tempPageCount++;
                    }
                    currentPage.insertRow(projectedRow);
                    bufferManager.markDirty(tempFilePath, currentPageId);
                }
            }

            bufferManager.unpinPage(tempFilePath, currentPageId);
            bufferManager.force(tempFilePath);

            // Now setup tempScan to read from the materialized temp file
            tempScan = new ScanOperator(bufferManager, tempFilePath, tempPageCount);
            materialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
