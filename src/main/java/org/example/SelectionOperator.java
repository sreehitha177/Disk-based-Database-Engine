package org.example;

public class SelectionOperator implements Operator{
    private final ScanOperator child;
    private final String selectionValue; // "director"

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
            // If not a match, keep looping
        }
    }

    public void close() {
        child.close();
    }
}
