package org.example.Testing;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;
import org.example.Rows.Row;
import org.example.Executor.ScanOperator;
import org.example.Executor.SelectionOperator;
import org.example.Rows.WorkedOnRow;

public class SelectionOperatorTest {
    public static void main(String[] args) {
        String filePath = "workedon.data";
        int totalPages = 1; // Adjust if needed

        System.out.println("Testing SelectionOperator for category = 'director'...");
        BufferManager bufferManager = new BufferManagerImplementation(100);

        ScanOperator scan = new ScanOperator(bufferManager, filePath);
        SelectionOperator select = new SelectionOperator(scan, "director");

        select.open();
        int count = 0;

        Row row;
        while ((row = select.next()) != null) {
            count++;
            WorkedOnRow w = (WorkedOnRow) row;
            String movieId = new String(w.getMovieId()).trim();
            String personId = new String(w.getPersonId()).trim();
            String category = new String(w.getCategory()).trim();

            System.out.println("Matched: " + movieId + " | " + personId + " | " + category);
        }

        select.close();
        System.out.println("Total matched rows for category 'director': " + count);
    }
}
