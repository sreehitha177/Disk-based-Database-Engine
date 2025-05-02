package org.example;

public class RangeSelectionOperatorTest {
    public static void main(String[] args) {
        BufferManager bufferManager = new BufferManagerImplementation(100);
        String filePath = "movies.data";
        int totalPages = 1; // Set this based on how many pages movies.data occupies

        // ScanOperator reads raw rows
        ScanOperator scan = new ScanOperator(bufferManager, filePath, totalPages);

        // RangeSelectionOperator filters rows by title (e.g., A-Z)
        RangeSelectionOperator rangeOp = new RangeSelectionOperator(scan, "A", "Bm");

        System.out.println("Testing RangeSelectionOperator with title range: A to Z...");
        rangeOp.open();

        int count = 0;
        while (true) {
            Row row = rangeOp.next();
            if (row == null) break;

            if (row instanceof DataRow) {
                DataRow dr = (DataRow) row;
                String movieId = new String(dr.getMovieId()).trim();
                String title = new String(dr.getTitle()).trim();
                System.out.println("Matched: " + movieId + " | " + title);
                count++;
            }
        }

        rangeOp.close();
        System.out.println("Total matched rows: " + count);
    }
}
