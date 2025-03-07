package org.example;


import java.io.*;
import java.nio.charset.StandardCharsets;


public class Utilities {
    private static BufferManager bufferManager;


    public static void loadDataset(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        } else {
            System.out.println("File found, proceeding with reading...");
        }


        try (BufferedReader br = new BufferedReader(new FileReader(filepath, StandardCharsets.UTF_8))) {
            String line = br.readLine();  // Read the first line (header) and discard it
            System.out.println("Skipping header: " + line);


            int currentPageId = 0;  // Start with the first page
//            Page currentPage = bufferManager.getPage(currentPageId); // Retrieve page with id 0
            Page currentPage = bufferManager.getPage(currentPageId);
            if (currentPage == null) {
                System.err.println("Error: Failed to load or create page " + currentPageId);
                return; // Stop processing if we can't get a page
            }
            // Total number of rows processed
            int totalRows = 0;


            while ((line = br.readLine()) != null) {
                System.out.println("Raw Line: " + line);


                // Split by tab and ensure there are enough columns
                String[] data = line.split("\t");
                if (data.length < 3) { // Adjust based on the dataset structure
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }


                String movieId = data[0];
                String title = data[2];  // Assuming primaryTitle is the third column
                System.out.println("Read Movie: " + movieId + " - " + title);


                // Create Row object
                Row row = new Row(movieId.getBytes(StandardCharsets.UTF_8), title.getBytes(StandardCharsets.UTF_8));


                // Try inserting the row into the current page
                int rowId = currentPage.insertRow(row);


                if (rowId == -1) {
                    // If the page is full, increment pageId and create a new page
                    currentPageId++;  // Increment page ID to create a new page
                    currentPage = bufferManager.getPage(currentPageId);  // Fetch the new page with updated ID
                    rowId = currentPage.insertRow(row);  // Insert the row into the new page
                    System.out.println("Page " + currentPageId + " is full, moving to the next page.");
                }


                totalRows++;  // Count the number of rows inserted
                System.out.println("Inserted Row with ID: " + rowId + " on Page ID: " + currentPage.getPid());
            }


            System.out.println("Total rows processed: " + totalRows);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        // Initialize the BufferManagerImplementation with a buffer size of 10 pages
        bufferManager = new BufferManagerImplementation(1024, "/Users/sreehithanarayana/Desktop/database.bin");


        String filepath = "/Users/sreehithanarayana/Downloads/title.basics.tsv"; // Use your correct path
        loadDataset(filepath);
    }
}
