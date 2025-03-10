package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utilities {
    private static BufferManager bufferManager;

    //To set the buffer manager instance
    public static void setBufferManager(BufferManager bm) {
        bufferManager = bm;
    }

    //Loads the data set into the buffer manager
    public static void loadDataset(String filepath) {
        File file = new File(filepath);

        //Checking if the file exists
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        } else {
            System.out.println("File found, proceeding with reading...");
        }
    
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            //Discarding the header line
            String line = br.readLine();
            System.out.println("Skipping header: " + line);
    
            int currentPageId = 0;
            Page currentPage = bufferManager.getPage(currentPageId);
            if (currentPage == null) {
                System.err.println("Error: Failed to load or create page " + currentPageId);
                return;
            }
    
            int totalRows = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\t");
                //Checking if the data has atleast three columns
                if (data.length < 3) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }

                //Getting movie ID
                String movieId = data[0];
                //Getting movie title
                String title = data[2];
                //Truncating the title if it exceeds 30 characters
                if (title.length() > 30) {
                    title = title.substring(0, 30);
                }
    
                Row row = new Row(movieId.getBytes(StandardCharsets.UTF_8), title.getBytes(StandardCharsets.UTF_8));
                int rowId = currentPage.insertRow(row);
    
                if (rowId == -1) {
                    currentPageId++;
                    currentPage = bufferManager.getPage(currentPageId);
                    rowId = currentPage.insertRow(row);
                    System.out.println("Page " + currentPageId + " is full, moving to the next page.");
                }
    
                totalRows++;
                System.out.println("Inserted Row with ID: " + rowId + " on Page ID: " + currentPage.getPid());
            }
    
            System.out.println("Total rows processed: " + totalRows);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Initialize the BufferManagerImplementation with a buffer size of 1024 pages
        bufferManager = new BufferManagerImplementation(1024, "C:\\Users\\Priya\\Desktop\\database.bin");

        // Path to the title.basics.tsv file  ----CHANGE THIS TO YOUR ACTUAL DATASET FILE PATH
        String filepath = "C:\\Users\\Priya\\Desktop\\UMASS Sem 2\\645\\Lab 1\\645_Project-master\\src\\main\\resources\\data\\title.basics.tsv";
        loadDataset(filepath);
    }
}