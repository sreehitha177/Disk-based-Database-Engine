package org.example;


import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;
import org.example.Executor.*;
import org.example.Rows.Row;
import org.example.Rows.TitleNameRow;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;


public class run_query {
    public static void main(String[] args) {

        int count=0;
        if (args.length != 3) {
            System.out.println("Usage: java run_query <start_range> <end_range> <buffer_size>");
            return;
        }

        String startRange = args[0];
        String endRange = args[1];
        int bufferSize = Integer.parseInt(args[2]);

        BufferManager bufferManager = new BufferManagerImplementation(bufferSize);
        utilities_new.setBufferManager(bufferManager);


        try {
            //Scanning Movies table
            ScanOperator moviesScan = new ScanOperator(bufferManager, "movies.data");

            //Scanning workedon table
            ScanOperator workedOnScan = new ScanOperator(bufferManager, "workedon.data");

            //Selecting rows with category = "director"
            SelectionOperator selection = new SelectionOperator(workedOnScan, "director");

            //Prrojecting movieId and personId and materialize
            ProjectionOperator projection = new ProjectionOperator(selection, bufferManager);

            //Scanning people table
            ScanOperator peopleScan = new ScanOperator(bufferManager, "people.data");

            //Range selection for movie titles
            RangeSelectionOperator moviesInRange = new RangeSelectionOperator(moviesScan, startRange, endRange);
//            System.out.println("Count after range selection:"+ moviesInRange.countRows());

            //BNL Join: Movies ⨝ WorkedOnTemp
            BNLJOperator join1 = new BNLJOperator(moviesInRange, projection, bufferManager, bufferSize);
//            System.out.println("Count after first join:"+ join1.countRows());

            //BNL Join: (Movies⨝WorkedOnTemp) ⨝ People
            BNLJOperator join2 = new BNLJOperator(join1, peopleScan, bufferManager, bufferSize);
//            System.out.println("Count after second join:"+ join2.countRows());


            //Final Projection for the output (title, name)
            FinalProjectionOperator finalProjection = new FinalProjectionOperator(join2);
//            System.out.println("Count after final projection:"+finalProjection.countRows());


            finalProjection.open();  // Required before calling next()

            int cnt = 0;
            //Writing output to a text file in csv format
            try (PrintWriter writer = new PrintWriter("output.txt", "UTF-8")) {
                Row row;
                while ((row = finalProjection.next()) != null) {
                    writer.println(row.toString());
                    cnt++;
                }
                System.out.println("Total rows written to file: " + cnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finalProjection.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
