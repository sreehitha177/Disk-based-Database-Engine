package org.example;

import java.util.ArrayList;
import java.util.List;

public class PageImplementation implements Page {
    private final int pageId;
    private final List<Row> rows;
    private final int maxRows;

    //Constructor to initialize Page
    public PageImplementation(int pageId, int maxRows) {
        this.pageId = pageId;
        this.maxRows = maxRows;
        this.rows = new ArrayList<>();
    }

    //Retrieves a row from it's rowId
    @Override
    public Row getRow(int rowId) {
        if (rowId >= 0 && rowId < rows.size()) {
            return rows.get(rowId);
        }
        return null;
    }

    //Inserts a new row into the page
    @Override
    public int insertRow(Row row) {
        if (isFull())
            return -1;
        rows.add(row);
        return rows.size() - 1;
    }

    //Checks if the page reached it's maximum capacityh
    @Override
    public boolean isFull() {
        return rows.size() >= maxRows;
    }

    //Returns the pageId
    @Override
    public int getPid() {
        return pageId;
    }

    // Provides access to the rows in the page
    public List<Row> getRows() {
        return rows;
    }
}
