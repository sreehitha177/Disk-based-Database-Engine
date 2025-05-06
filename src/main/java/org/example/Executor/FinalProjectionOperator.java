package org.example.Executor;

import org.example.Rows.JoinedRow;
import org.example.Rows.PeopleRow;
import org.example.Rows.Row;
import org.example.Rows.TitleNameRow;

public class FinalProjectionOperator implements Operator {
    private final BNLJOperator child;

    public FinalProjectionOperator(BNLJOperator child) {
        this.child = child;
    }

    public void open() {
        child.open();
    }

    public Row next() {
        Row joinedRow = child.next();
        if (joinedRow == null) {
            return null;
        }

        if (joinedRow instanceof JoinedRow) {
            JoinedRow jr = (JoinedRow) joinedRow;

            byte[] outerData = jr.getOuterData();
            byte[] innerData = jr.getInnerData();

            // Assume outer is DataRow (movieId + title)
            // Assume inner is TempRow (movieId + personId) or PeopleRow (personId + name)

            // --- Extract title from outerData ---
            // Skip movieId (9 bytes), title is next 30 bytes
            int titleOffset = 9;
            byte[] titleBytes = new byte[30];
            System.arraycopy(outerData, titleOffset, titleBytes, 0, 30);

            // --- Extract name from innerData ---
            // If innerData is TempRow, it would be 9 + 10
            // If innerData is PeopleRow, it would be 10 + 105
            byte[] nameBytes;
//            if (innerData.length >= 115) {
//                // inner is PeopleRow (personId(10) + name(105))
//                int nameOffset = 10;
//                nameBytes = new byte[105];
//                System.arraycopy(innerData, nameOffset, nameBytes, 0, 105);
//            } else {
//                // This case shouldn't happen for final join (workedon ⨝ people)
//                nameBytes = new byte[0];
//            }
            Row inner = jr.getInner();
            if (inner instanceof PeopleRow) {
                nameBytes = ((PeopleRow) inner).getName();
            } else {
                nameBytes = new byte[0];
            }

            System.out.println(new String(titleBytes).trim() + "\t" + new String(nameBytes).trim());
            return new TitleNameRow(titleBytes, nameBytes);
        }

        return null;
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
