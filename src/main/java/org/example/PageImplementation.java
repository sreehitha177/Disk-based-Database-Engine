package org.example;

import java.util.ArrayList;
import java.util.List;

public class PageImplementation implements Page {
    private final int pageId;
    private final List<Row> rows;
    private final int maxRows;

    public PageImplementation(int pageId, int maxRows) {
        this.pageId = pageId;
        this.maxRows = maxRows;
        this.rows = new ArrayList<>();
    }

    @Override
    public Row getRow(int rowId) {
        if (rowId >= 0 && rowId < rows.size()) {
            return rows.get(rowId);
        }
        return null;
    }

    @Override
    public int insertRow(Row row) {
        if (isFull())
            return -1;
        rows.add(row);
        return rows.size() - 1;
    }

    @Override
    public boolean isFull() {
        return rows.size() >= maxRows;
    }

    @Override
    public int getPid() {
        return pageId;
    }

    //  Add this method to allow controlled access to the rows
    public List<Row> getRows() {
        return rows;
    }
}
