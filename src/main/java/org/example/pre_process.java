package org.example;

import org.example.BufferManagement.BufferManager;
import org.example.BufferManagement.BufferManagerImplementation;

import java.io.*;

public class pre_process {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java pre_process <movies_file> <workedon_file> <people_file>");
            return;
        }

        String moviesFile = args[0];
        String workedOnFile = args[1];
        String peopleFile = args[2];

//        String moviesFile = "/Users/sreehithanarayana/Desktop/645_Project/test100000.movies.tsv";
//        String workedOnFile = "/Users/sreehithanarayana/Desktop/645_Project/test100000.workedon.tsv";
//        String peopleFile = "/Users/sreehithanarayana/Desktop/645_Project/test100000.people.tsv";


        moviesFile = cleanTSVFile(moviesFile, "cleaned_movies.tsv");
        workedOnFile = cleanTSVFile(workedOnFile, "cleaned_workedon.tsv");
        peopleFile = cleanTSVFile(peopleFile, "cleaned_people.tsv");


        // Initialize buffer manager and utilities
        BufferManager bufferManager = new BufferManagerImplementation(100); // buffer size can be passed if you want
        utilities_new.setBufferManager(bufferManager);

        //Loading datasets
        System.out.println("Loading Movies table...");
        utilities_new.loadDataset(moviesFile);

        System.out.println("Loading WorkedOn table...");
        utilities_new.loadWorkedOnDataset(workedOnFile);

        System.out.println("Loading People table...");
        utilities_new.loadPeopleDataset(peopleFile);

        System.out.println("Preprocessing completed successfully.");
    }


    //Cleaning dataset
    private static String  cleanTSVFile(String inputPath, String outputPath) {
        System.out.println("Cleaning file: " + inputPath);
        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputPath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\t", -1);
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].replace("\"", "\\\"");
                }
                writer.write(String.join("\t", columns));
                writer.newLine();
            }
            System.out.println("Cleaned file written to: " + outputPath);
            return outputPath;
        } catch (IOException e) {
            System.err.println("Error processing file: " + inputPath);
            e.printStackTrace();
        }

        return inputPath;
    }
}
