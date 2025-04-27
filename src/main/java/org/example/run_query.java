package org.example;

public class run_query {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java run_query <start_range> <end_range> <buffer_size>");
            return;
        }

        String startRange = args[0];
        String endRange = args[1];
        int bufferSize = Integer.parseInt(args[2]);

        BufferManager bufferManager = new BufferManagerImplementation(bufferSize);
        utilities_new.setBufferManager(bufferManager);

        // Define number of pages (assuming you know these)
        int totalMoviePages = 500;      // You should set real values
        int totalWorkedOnPages = 500;
        int totalPeoplePages = 500;

        try {
            // 1. Setup scan for Movies
            ScanOperator moviesScan = new ScanOperator(bufferManager, "movies.data", totalMoviePages);

            // 2. Setup scan for WorkedOn
            ScanOperator workedOnScan = new ScanOperator(bufferManager, "workedon.data", totalWorkedOnPages);

            // 3. SelectionOperator: filter category = "director"
            SelectionOperator selection = new SelectionOperator(workedOnScan, "director");

            // 4. ProjectionOperator: project movieId and personId and materialize
            ProjectionOperator projection = new ProjectionOperator(selection, bufferManager);

            // 5. Setup scan for People
            ScanOperator peopleScan = new ScanOperator(bufferManager, "people.data", totalPeoplePages);

            //Add Range Selection on top of movies scan
            RangeSelectionOperator moviesInRange = new RangeSelectionOperator(moviesScan, startRange, endRange);

            // 6. BNL Join: Movies ⨝ WorkedOnTemp
            BNLJOperator moviesWorkedOnJoin = new BNLJOperator(moviesInRange, projection, bufferManager, bufferSize);

            // 7. BNL Join: (Movies⨝WorkedOnTemp) ⨝ People
            BNLJOperator finalJoin = new BNLJOperator(moviesWorkedOnJoin, peopleScan, bufferManager, bufferSize);

            // 8. Final Projection: output (title, name)
            FinalProjectionOperator finalProjection = new FinalProjectionOperator(finalJoin);

            // 9. Execute the query
            finalProjection.open();
            Row result;
            while ((result = finalProjection.next()) != null) {
                if (result instanceof TitleNameRow) {
                    TitleNameRow tnr = (TitleNameRow) result;
                    String title = new String(tnr.getTitle()).trim();
                    String name = new String(tnr.getName()).trim();
                    System.out.println(title + "," + name); // Output in CSV format
                }
            }
            finalProjection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
