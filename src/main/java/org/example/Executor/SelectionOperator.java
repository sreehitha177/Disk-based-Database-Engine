package org.example.Executor;

import org.example.Rows.Row;
import org.example.Rows.WorkedOnRow;

public class SelectionOperator implements Operator {
    private final ScanOperator child;
    private final String selectionValue; //Filter value for the election operator

    public SelectionOperator(ScanOperator child, String selectionValue) {
        this.child = child;
        this.selectionValue = selectionValue;
    }

    public void open() {
        child.open();
    }

    public Row next() {
        while (true) {
            Row row = child.next();
            if (row == null) {
                return null; // End of child
            }

            //As SelectionOperator is only for WorkedOn table for this lab
            if (row instanceof WorkedOnRow) {
                WorkedOnRow workedOnRow = (WorkedOnRow) row;
                String category = new String(workedOnRow.getCategory()).trim();
//                System.out.println("Category candidate: " + category);

                if (category.equals(selectionValue)) {
                    return workedOnRow;
                }
            }
        }
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

    public void close() {
        child.close();
    }
}
