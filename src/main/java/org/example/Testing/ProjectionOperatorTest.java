package org.example.Testing;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;
import org.example.Executor.ProjectionOperator;
import org.example.Executor.ScanOperator;
import org.example.Executor.SelectionOperator;
import org.example.Rows.Row;
import org.example.Rows.TempRow;

public class ProjectionOperatorTest {
    public static void main(String[] args) {
        String filePath = "workedon.data";
        int totalPages = 1; // Adjust if needed

        System.out.println("Testing ProjectionOperator for category = 'director'...");
        BufferManager bufferManager = new BufferManagerImplementation(100);

        // Step 1: Scan workedon.data
        ScanOperator scan = new ScanOperator(bufferManager, filePath);

        // Step 2: Select rows where category = "director"
        SelectionOperator selection = new SelectionOperator(scan, "director");

        // Step 3: Project only movieId and personId
        ProjectionOperator projection = new ProjectionOperator(selection, bufferManager);

        projection.open();
        int count = 0;

        Row row;
        while ((row = projection.next()) != null) {
            count++;
//            System.out.println("count: "+count);
            if (row instanceof TempRow) {
                TempRow temp = (TempRow) row;
                String movieId = new String(temp.getMovieId()).trim();
                String personId = new String(temp.getPersonId()).trim();
                System.out.println("Projected: " + movieId + " | " + personId);
            }
        }

        projection.close();
        System.out.println("Total projected rows: " + count);
    }
}
