package org.example.Testing;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;
import org.example.BufferManagement.PageImplementation;
import org.example.Executor.BNLJOperator;
import org.example.Executor.FinalProjectionOperator;
import org.example.Executor.Operator;
import org.example.Executor.ScanOperator;
import org.example.Rows.*;

import java.nio.charset.StandardCharsets;

public class FinalProjectionTest {
    public static void main(String[] args) {
        BufferManager bufferManager = new BufferManagerImplementation(100);


        String moviesFile = "movies.data";
        String tempFile = "workedon_temp.data";
        String peopleFile = "people.data";

        PageImplementation moviesPage = (PageImplementation) bufferManager.createPage(moviesFile);
        PageImplementation TempPage = (PageImplementation) bufferManager.createPage(tempFile);
        PageImplementation PeoplePage = (PageImplementation) bufferManager.createPage(peopleFile);


        // Load movie table
//        bufferManager.loadRows("movies.data", List.of(
//                new DataRow(pad("tt0000001", 9), pad("Movie A", 30)),
//                new DataRow(pad("tt0000002", 9), pad("Movie B", 30))
//        ));

        moviesPage.insertRow(new DataRow(pad("tt0000001", 9), pad("Movie A", 30)));
        moviesPage.insertRow(new DataRow(pad("tt0000002", 9), pad("Movie B", 30)));
        moviesPage.insertRow(new DataRow(pad("tt0000002", 9), pad("Movie C", 30)));

        TempPage.insertRow(new TempRow(pad("tt0000001", 9), pad("nm0000001", 10)));
        TempPage.insertRow(new TempRow(pad("tt0000002", 9), pad("nm0000002", 10)));
        TempPage.insertRow(new TempRow(pad("tt0000002", 9), pad("nm0000003", 10)));

        PeoplePage.insertRow(new PeopleRow(pad("nm0000001", 10), pad("Person A", 105)));
        PeoplePage.insertRow(new PeopleRow(pad("nm0000002", 10), pad("Person B", 105)));
        PeoplePage.insertRow(new PeopleRow(pad("nm0000005", 10), pad("Person C", 105)));
        PeoplePage.insertRow(new PeopleRow(pad("nm0000004", 10), pad("Person D", 105)));





        // Load workedon_temp (movieId, personId)
//        bufferManager.loadRows("workedon_temp.data", List.of(
//                new TempRow(pad("tt0000001", 9), pad("nm0000001", 10)),
//                new TempRow(pad("tt0000002", 9), pad("nm0000002", 10))
//        ));
//
//        // Load people table (personId, name)
//        bufferManager.loadRows("people.data", List.of(
//                new PeopleRow(pad("nm0000001", 10), pad("Alice Johnson", 105)),
//                new PeopleRow(pad("nm0000002", 10), pad("Bob Smith", 105))
//        ));

        // First join: movies ⨝ workedon_temp
        Operator movieScan = new ScanOperator(bufferManager, "movies.data");
        Operator workedonScan = new ScanOperator(bufferManager, "workedon_temp.data");
        BNLJOperator join1 = new BNLJOperator(movieScan, workedonScan, bufferManager, 10);
        join1.open();
        System.out.println("Join1 Output (movies ⨝ workedon):");
        Row r1;
        while ((r1 = join1.next()) != null) {
            JoinedRow jr = (JoinedRow) r1;
            DataRow dr = (DataRow) jr.getOuter();
            TempRow tr = (TempRow) jr.getInner();
            System.out.println("movieId: " + new String(dr.getMovieId()).trim() +
                    ", title: " + new String(dr.getTitle()).trim() +
                    ", personId: " + new String(tr.getPersonId()).trim());
        }
        join1.close();

        // Second join: (movies ⨝ workedon_temp) ⨝ people
        Operator peopleScan = new ScanOperator(bufferManager, "people.data");
        BNLJOperator join2 = new BNLJOperator(join1, peopleScan, bufferManager, 10);
        join2.open();
        System.out.println("Join2 Output ((movies ⨝ workedon) ⨝ people):");
        Row r2;
        while ((r2 = join2.next()) != null) {
            JoinedRow jr = (JoinedRow) r2;
            JoinedRow left = (JoinedRow) jr.getOuter();  // join1 result
            DataRow dr = (DataRow) left.getOuter();
            TempRow tr = (TempRow) left.getInner();
            PeopleRow pr = (PeopleRow) jr.getInner();

            System.out.println("movieId: " + new String(dr.getMovieId()).trim() +
                    ", title: " + new String(dr.getTitle()).trim() +
                    ", personId: " + new String(tr.getPersonId()).trim() +
                    ", name: " + new String(pr.getName()).trim());
        }
        join2.close();

        // Final projection
        FinalProjectionOperator finalProj = new FinalProjectionOperator(join2);
        finalProj.open();

        System.out.println("Final Projection Output:");
        Row row;
        while ((row = finalProj.next()) != null) {
            if (row instanceof TitleNameRow) {
//                System.out.println("HELLOOO");
                TitleNameRow tr = (TitleNameRow)row;
                String title = new String(tr.getTitle(), StandardCharsets.ISO_8859_1).trim();
                String name = new String(tr.getName(), StandardCharsets.ISO_8859_1).trim();
                System.out.println("Title: " + title + ", Name: " + name);
            }
        }

        finalProj.close();
    }

    private static byte[] pad(String value, int length) {
        byte[] padded = new byte[length];
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, length));
        return padded;
    }
}
