package org.example.Executor;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.Page;
import org.example.BufferManagement.PageImplementation;
import org.example.Rows.Row;
import org.example.Rows.TempRow;
import org.example.Rows.WorkedOnRow;

import java.io.File;

public class ProjectionOperator implements Operator {
    private final SelectionOperator child;
    private final BufferManager bufferManager;
    private final String tempFilePath = "workedon_temp.data";
    private boolean materialized;
    private ScanOperator tempScan;
    private int tempPageCount;

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

        File tempFile = new File(tempFilePath);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }


    //Materializing the temporary table
    private void materialize() {
        try {
            Page currentPage = bufferManager.createPage(tempFilePath);
            currentPage.getData()[0] = PageImplementation.TEMP_PAGE;
            int currentPageId = currentPage.getPid();
            tempPageCount = 1;

            while (true) {
                Row row = child.next();
                if (row == null) break;

                if (row instanceof WorkedOnRow) {
                    WorkedOnRow workedOnRow = (WorkedOnRow) row;
//                    System.out.println("Row is instance of WorkedOnRow with movieId: " + new String(workedOnRow.getMovieId()).trim());

                    // Create a projected row containing only movieId and personId
                    byte[] movieId = workedOnRow.getMovieId();
                    byte[] personId = workedOnRow.getPersonId();

//                    LeafRow projectedRow = new LeafRow(movieId, new Rid(0, 0)); // Dummy Rid, not important for join matching
                    TempRow projectedRow = new TempRow(movieId, personId);
//                    System.out.println("TempRow created with movieId: " +new String(projectedRow.getMovieId()).trim());

                    if (currentPage.isFull()) {
                        bufferManager.unpinPage(tempFilePath, currentPageId);
                        currentPage = bufferManager.createPage(tempFilePath);
                        currentPage.getData()[0] = PageImplementation.TEMP_PAGE;
                        currentPageId = currentPage.getPid();
                        tempPageCount++;
                    }
//                    System.out.println("Materializing: " + new String(projectedRow.getMovieId()).trim() + "," + new String(projectedRow.getPersonId()).trim());
                    currentPage.insertRow(projectedRow);
                    bufferManager.markDirty(tempFilePath, currentPageId);
                }
            }

            bufferManager.unpinPage(tempFilePath, currentPageId);
            bufferManager.force(tempFilePath);

            //Reading tempScan from the materialized temp file
            tempScan = new ScanOperator(bufferManager, tempFilePath);
            materialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //To count total number of rows
    public int countRows() {
        open();
        int count = 0;
        while (next() != null) {
            count++;
        }
        close();
        return count;
    }
}
