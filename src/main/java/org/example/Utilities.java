package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utilities {
    private static BufferManager bufferManager;

    public static void setBufferManager(BufferManager bm) {
        bufferManager = bm;
    }

    public static void loadDataset(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        } else {
            System.out.println("File found, proceeding with reading...");
        }
    
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            String line = br.readLine();  // Read the first line (header) and discard it
            System.out.println("Skipping header: " + line);
    
            int currentPageId = 0;  // Start with the first page
            Page currentPage = bufferManager.getPage(currentPageId);
            if (currentPage == null) {
                System.err.println("Error: Failed to load or create page " + currentPageId);
                return;
            }
    
            int totalRows = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\t");
                if (data.length < 3) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }
    
                String movieId = data[0];
                String title = data[2];
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
        // Initialize the BufferManagerImplementation with a buffer size of 100 pages
        bufferManager = new BufferManagerImplementation(1024, "C:\\Users\\Priya\\Desktop\\database.bin");

        // Path to the title.basics.tsv file  ----CHANGE THIS TO YOUR DATASET FILE PATH
        String filepath = "C:\\Users\\Priya\\Desktop\\UMASS Sem 2\\645\\Lab 1\\645_Project-master\\src\\main\\resources\\data\\title.basics.tsv";
        loadDataset(filepath);
    }
}