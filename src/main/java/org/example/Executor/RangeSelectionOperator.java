package org.example.Executor;

import org.example.Rows.DataRow;
import org.example.Rows.Row;

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
                String movieId =  new String(dataRow.getMovieId());
//                System.out.println(title +"\t" + movieId);

                if (title.compareTo(startRange) >= 0 && title.compareTo(endRange) <= 0) {
                    return dataRow;
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

    @Override
    public void close() {
        child.close();
    }
}
