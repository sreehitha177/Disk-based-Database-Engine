package org.example;

public class DataFileVerifier {
    public static void main(String[] args) {
        int moviePages = 5;    // Adjust based on how many pages were created
        int workedOnPages = 5;
        int peoplePages = 5;

        System.out.println("✅ Verifying movies.data:");
        verifyScan("movies.data", moviePages);

        System.out.println("\n✅ Verifying workedon.data:");
        verifyScan("workedon.data", workedOnPages);

        System.out.println("\n✅ Verifying people.data:");
        verifyScan("people.data", peoplePages);
    }

    public static void verifyScan(String filePath, int totalPages) {
        BufferManager bufferManager = new BufferManagerImplementation(100); // Ensure this is implemented

        ScanOperator scanOp = new ScanOperator(bufferManager, filePath, totalPages);
        scanOp.open();

        int count = 0;
        Row row;
        while ((row = scanOp.next()) != null) {
            count++;

            if (filePath.contains("movies.data") && row instanceof DataRow) {
                DataRow dr = (DataRow) row;
                String movieId = new String(dr.getMovieId()).trim();
                String title = new String(dr.getTitle()).trim();
                System.out.println("Movie: " + movieId + " | " + title);
            } else if (filePath.contains("workedon.data") && row instanceof WorkedOnRow) {
                WorkedOnRow wr = (WorkedOnRow) row;
                String movieId = new String(wr.getMovieId()).trim();
                String personId = new String(wr.getPersonId()).trim();
                String category = new String(wr.getCategory()).trim();
                System.out.println("WorkedOn: " + movieId + " | " + personId + " | " + category);
            } else if (filePath.contains("people.data") && row instanceof PeopleRow) {
                PeopleRow pr = (PeopleRow) row;
                String personId = new String(pr.getPersonId()).trim();
                String name = new String(pr.getName()).trim();
                System.out.println("People: " + personId + " | " + name);
            }
        }

        scanOp.close();
        System.out.println("✔️ Total rows scanned from " + filePath + ": " + count);
    }
}
