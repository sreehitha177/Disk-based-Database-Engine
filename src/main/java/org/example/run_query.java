package org.example;


import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;
import org.example.Executor.*;
import org.example.Rows.Row;
import org.example.Rows.TitleNameRow;



public class run_query {
    public static void main(String[] args) {

        int count=0;
//        if (args.length != 3) {
//            System.out.println("Usage: java run_query <start_range> <end_range> <buffer_size>");
//            return;
//        }

        String startRange = "A";//args[0];
        String endRange = "Z";//args[1];
        int bufferSize = 100;//Integer.parseInt(args[2]);

        BufferManager bufferManager = new BufferManagerImplementation(bufferSize);
        utilities_new.setBufferManager(bufferManager);




        try {
            // 1. Setup scan for Movies
            ScanOperator moviesScan = new ScanOperator(bufferManager, "movies.data");

            // 2. Setup scan for WorkedOn
            ScanOperator workedOnScan = new ScanOperator(bufferManager, "workedon.data");

            // 3. SelectionOperator: filter category = "director"
            SelectionOperator selection = new SelectionOperator(workedOnScan, "director");

            // 4. ProjectionOperator: project movieId and personId and materialize
            ProjectionOperator projection = new ProjectionOperator(selection, bufferManager);

            // 5. Setup scan for People
            ScanOperator peopleScan = new ScanOperator(bufferManager, "people.data");

            //Add Range Selection on top of movies scan
            RangeSelectionOperator moviesInRange = new RangeSelectionOperator(moviesScan, startRange, endRange);
            System.out.println("Count after range selection:"+ moviesInRange.countRows());
//             6. BNL Join: Movies ⨝ WorkedOnTemp
            BNLJOperator join1 = new BNLJOperator(moviesInRange, projection, bufferManager, bufferSize);
            System.out.println("Count after first join:"+ join1.countRows());

            // 7. BNL Join: (Movies⨝WorkedOnTemp) ⨝ People
            BNLJOperator join2 = new BNLJOperator(join1, peopleScan, bufferManager, bufferSize);
            System.out.println("Count after second join:"+ join2.countRows());


            // 8. Final Projection: output (title, name)
            FinalProjectionOperator finalProjection = new FinalProjectionOperator(join2);
            System.out.println("Count after final projection:"+finalProjection.countRows());

            // 9. Execute the query
//            finalProjection.open();
//            Row result;
//
//            while ((result = finalProjection.next()) != null) {
//                if (result instanceof TitleNameRow) {
//                    TitleNameRow tnr = (TitleNameRow) result;
//                    String title = new String(tnr.getTitle()).trim();
//                    String name = new String(tnr.getName()).trim();
//                    System.out.println(title + "," + name); // Output in CSV format
//                    count++;
//                }
//            }
//            finalProjection.close();
//            System.out.println("Total number of rows: "+ count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
