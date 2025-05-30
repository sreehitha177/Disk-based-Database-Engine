package org.example.BufferManagement;

import org.example.Rows.Row;

//Page
public interface Page {

    int PAGE_SIZE = 4096;
    /**
     * Fetches a row from the page by its row ID.
     * @param rowId The ID of the row to retrieve.
     * @return The Row object containing the requested data.
     */
    Row getRow(int rowId);

    /**
     * Inserts a new row into the page.
     * @param row The Row object containing the data to insert.
     * @return The row ID of the inserted row, or -1 if the page is full
     */
    int insertRow(Row row);

    /**
     * Check if the page is full.
     * @return true if the page is full, false otherwise
     */
    boolean isFull();

    /**
     * Returns the page id
     * @return page id of this page
     */
    int getPid();


    byte[] getData();
    void setData(byte[] data);
    void markDirty();
}