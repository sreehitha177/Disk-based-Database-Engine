package org.example;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BNLJOperatorTest {
    public static void main(String[] args) {
        // Setup mock buffer manager and pages
        BufferManager bufferManager = new BufferManagerImplementation(100);
        String moviesFile = "movies.data";
        String tempFile = "workedon_temp.data";

        // Create and populate one page for each file
        PageImplementation moviesPage = (PageImplementation) bufferManager.createPage(moviesFile);
        PageImplementation TempPage = (PageImplementation) bufferManager.createPage(tempFile);

        // Insert movie rows
        moviesPage.insertRow(new DataRow(pad("tt0000001", 9), pad("Movie A", 30)));
        moviesPage.insertRow(new DataRow(pad("tt0000002", 9), pad("Movie B", 30)));
        moviesPage.insertRow(new DataRow(pad("tt0000002", 9), pad("Movie C", 30)));

        // Insert workedon rows
        TempPage.insertRow(new TempRow(pad("tt0000001", 9), pad("nm0000001", 10)));
        TempPage.insertRow(new TempRow(pad("tt0000002", 9), pad("nm0000002", 10)));
        TempPage.insertRow(new TempRow(pad("tt0000002", 9), pad("nm0000003", 10)));

        // Create ScanOperators with 1 total page each
        Operator left = new ScanOperator(bufferManager, moviesFile, 1);
        Operator right = new ScanOperator(bufferManager, tempFile, 1);

        // Create and execute BNLJ
        BNLJOperator bnlj = new BNLJOperator(left, right, bufferManager, 6);
        bnlj.open();

        System.out.println("Join results:");
        Row result;
//        while ((result = bnlj.next()) != null) {
//            byte[] joined = result.getBytes();
//
//            String movieId = new String(joined, 0, 9).trim();
//            String title = new String(joined, 9, 30).trim();
//            String personId = new String(joined, 39, 10).trim();
////            String category = new String(joined, 49, 20).trim(); // workedon has 10+20 after offset
//
//            System.out.println("movieId: " + movieId + ", title: " + title + ", personId: " + personId);
//        }

        while ((result = bnlj.next()) != null) {
            if (result instanceof JoinedRow) {
                JoinedRow jr=(JoinedRow)result;
                Row outer = jr.getOuter();
                Row inner = jr.getInner();

                if (outer instanceof DataRow && inner instanceof TempRow) {
                    DataRow dataRow=(DataRow)outer;
                    TempRow tempRow=(TempRow)inner;
                    String movieId = new String(dataRow.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                    String title = new String(dataRow.getTitle(), StandardCharsets.ISO_8859_1).trim();
                    String personId = new String(tempRow.getPersonId(), StandardCharsets.ISO_8859_1).trim();

                    System.out.println("movieId: " + movieId + ", title: " + title + ", personId: " + personId);
                } else {
                    System.out.println("Unexpected row types: outer = " + outer.getClass() + ", inner = " + inner.getClass());
                }
            }
        }

        bnlj.close();
    }

    private static byte[] pad(String s, int len) {
        return Arrays.copyOf(s.getBytes(), len);
    }
}
