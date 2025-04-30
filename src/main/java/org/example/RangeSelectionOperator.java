package org.example;

public class RangeSelectionOperator implements Operator {
    private final Operator child;
    private final String startRange;
    private final String endRange;

    public RangeSelectionOperator(Operator child, String startRange, String endRange) {
        this.child = child;
        this.startRange = startRange;
        this.endRange = endRange;
    }

    @Override
    public void open() {
        child.open();
    }

    @Override
    public Row next() {
        while (true) {
            Row row = child.next();
            if (row == null) {
                return null;
            }

            if (row instanceof DataRow) {
                DataRow dataRow = (DataRow) row;
                String title = new String(dataRow.getTitle()).trim();
                System.out.println("Title candidate: " + title);

                if (title.compareTo(startRange) >= 0 && title.compareTo(endRange) <= 0) {
                    return dataRow;
                }
            }
        }
    }

    @Override
    public void close() {
        child.close();
    }
}
