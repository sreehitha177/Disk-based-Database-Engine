package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


//public class Utilities{
//    // Loads the buffer manager with the imdb dataset
//    public static void loadDataset(BufferManager bf, String filepath){
//        try (BufferedReader br = new BufferedReader(new FileReader("/Users/sreehithanarayana/Downloads/title.basics.tsv"))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] data = line.split("\t");
//                byte[] movieId = data[0].getBytes();
//                byte[] title = data[1].substring(0, Math.min(30, data[1].length())).getBytes();
//                Row row = new Row(movieId, title);
//
//                System.out.println("Read Movie: " + data[0] + " - " + data[1]); // Print the read data
//
//                Page p = bf.createPage();
//                if (p.insertRow(row) == -1) {
//                    bf.unpinPage(p.getPid());
//                    p = bf.createPage();
//                    p.insertRow(row);
//                }
//                bf.unpinPage(p.getPid());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}



public class Utilities{
    // Loads the buffer manager with the IMDB dataset

//    public static void loadDataset(BufferManager bf, String filepath) {
//        File file = new File(filepath);
//        if (!file.exists()) {
//            System.out.println("File not found: " + file.getAbsolutePath());
//            return;
//        } else {
//            System.out.println("File found, proceeding with reading...");
//        }
//
//        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
//            String line = br.readLine();  // Read the header line
//            System.out.println("Skipping header: " + line);
//
//            Page p = bf.createPage(); // Start with a new page
//
//            while ((line = br.readLine()) != null) {
//                String[] data = line.split("\t");
//                if (data.length < 3) {
//                    System.out.println("Skipping malformed line: " + line);
//                    continue;
//                }
//
//                byte[] movieId = data[0].getBytes();
//                byte[] title = data[2].substring(0, Math.min(30, data[2].length())).getBytes();
//                Row row = new Row(movieId, title);
//
//                if (p.insertRow(row) == -1) {  // If page is full, get a new page
//                    bf.unpinPage(p.getPid());
//                    p = bf.createPage();
//                    p.insertRow(row);
//                }
//
//                bf.unpinPage(p.getPid());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public static void loadDataset(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        } else {
            System.out.println("File found, proceeding with reading...");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine();  // Read the first line (header) and discard it
            System.out.println("Skipping header: " + line);

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //    public static void main(String[] args) {
//        String filepath;
//
//        if (args.length < 1) {
//            System.out.println("No file path provided, using default.");
//            filepath = "/Users/sreehithanarayana/Downloads/title.basics.tsv"; // Default path
//        } else {
//            filepath = args[0];
//        }
//
//        System.out.println("Using file path: " + filepath);
//
//        BufferManager bf = new BufferManager() {
//            @Override
//            Page getPage(int pageId) {
//                return null;
//            }
//
//            @Override
//            Page createPage() {
//                return null;
//            }
//
//            @Override
//            void markDirty(int pageId) {
//            }
//
//            @Override
//            void unpinPage(int pageId) {
//            }
//        };
//
//        loadDataset(bf, filepath);
//    }
    public static void main(String[] args) {
        String filepath = "/Users/sreehithanarayana/Downloads/title.basics.tsv"; // Use your correct path
        loadDataset(filepath);
    }



}

