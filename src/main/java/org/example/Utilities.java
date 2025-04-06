//package org.example;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
////Utilities
//public class Utilities {
//    private static BufferManager bufferManager;
//
//    //To set the buffer manager instance
//    public static void setBufferManager(BufferManager bm) {
//        bufferManager = bm;
//    }
//
//    public static void loadDataset(String filepath) {
//        File file = new File(filepath);
//
//        // Checking if the file exists
//        if (!file.exists()) {
//            System.out.println("File not found: " + file.getAbsolutePath());
//            return;
//        } else {
//            System.out.println("File found, proceeding with reading...");
//        }
//
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
//            // Discarding the header line
//            String line = br.readLine();
//            System.out.println("Skipping header: " + line);
//
//            Page currentPage = bufferManager.createPage();
//            int currentPageId = currentPage.getPid();
//
//            int totalRows = 0;
//            int finalRows=0;
//            while ((line = br.readLine()) != null) {
//                String[] data = line.split("\t");
//                // Checking if the data has at least three columns
//                if (data.length < 3) {
//                    System.out.println("Skipping malformed line: " + line);
//                    finalRows++;
//                    continue;
//                }
//
//                // Getting movie ID
//                String movieId = data[0];
//                // Getting movie title
//                String title = data[2];
//                // Truncating the title if it exceeds 30 characters
//                if (title.length() > 30) {
//                    title = title.substring(0, 30);
//                }
//
//                // Ensuring movieId does not exceed the allowed length
//                if (movieId.getBytes(StandardCharsets.UTF_8).length > 9) {
//                    System.out.println("Skipping row with oversized movieId: " + movieId);
//                    finalRows++;
//                    continue;
//                }
//
//                Row row = new Row(movieId.getBytes(StandardCharsets.UTF_8), title.getBytes(StandardCharsets.UTF_8));
////                int rowId = currentPage.insertRow(row);
//
//                int rowId;
//
//                // If the page is full, create a new page and insert the row there
//                if (currentPage.isFull()) {
//                    bufferManager.unpinPage(currentPageId);
//                    currentPage = bufferManager.createPage();
//                    currentPageId = currentPage.getPid();
//                    rowId = currentPage.insertRow(row);
//                    System.out.println("Page " + (currentPageId - 1) + " is full, moving to the next page.");
//                }
//                else{
//                    rowId = currentPage.insertRow(row);
//                }
//
//                totalRows++;
//                finalRows++;
//                System.out.println("Inserted Row with ID: " + rowId + " on Page ID: " + currentPageId);
//            }
//
//            bufferManager.unpinPage(currentPageId);
//            System.out.println("Total rows processed: " + totalRows);
//            System.out.println("Final rows processed: " + finalRows);
//            System.out.println("Last Page ID: " + currentPageId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void pinAndUpdateLRU(PageImplementation page, LinkedHashMap<Integer, PageImplementation> lruCache) {
//        page.pin();
//        lruCache.remove(page.getPid()); // Remove if exists
//        lruCache.put(page.getPid(), page);
//    }
//
//    /**
//     * Finds an unfilled page in the buffer pool.
//     */
//    public static PageImplementation getUnfilledPage(Map<Integer, PageImplementation> pageTable, LinkedHashMap<Integer, PageImplementation> lruCache) {
//        for (PageImplementation page : pageTable.values()) {
//            if (!page.isFull()) {
//                pinAndUpdateLRU(page, lruCache);
//                return page;
//            }
//        }
//        return null;
//    }
//
//
//    private static final byte PADDING_BYTE = 0x20; // Use SPACE (0x20) instead of 0x7F
//
//    public static byte[] truncateOrPadByteArray(byte[] value, int maxLength) {
//        if (value.length > maxLength) {
//            return Arrays.copyOf(value, maxLength);
//        } else {
//            byte[] padded = new byte[maxLength];
//            System.arraycopy(value, 0, padded, 0, value.length);
//            Arrays.fill(padded, value.length, maxLength, PADDING_BYTE); // Fill with SPACE
//            return padded;
//        }
//    }
//
//    public static byte[] removeTrailingBytes(byte[] input) {
//        int endIndex = input.length;
//        for (int i = input.length - 1; i >= 0; i--) {
//            if (input[i] != PADDING_BYTE) {
//                endIndex = i + 1;
//                break;
//            }
//        }
//        return Arrays.copyOf(input, endIndex);
//    }
//
//
//
//    public static void main(String[] args) {
//        // Initialize the BufferManagerImplementation with a buffer size of 1024 pages
//        bufferManager = new BufferManagerImplementation(2048, "/Users/sreehithanarayana/Downloads/database.bin");
//
//        // Path to the title.basics.tsv file  ----CHANGE THIS TO YOUR ACTUAL DATASET FILE PATH
//        String filepath = "/Users/sreehithanarayana/Downloads/title.basics.tsv";
////        String filepath = "/Users/sreehithanarayana/Desktop/Hellothere.tsv";
////        String filepath = "/Users/sreehithanarayana/Downloads/test.tsv";
//
//        loadDataset(filepath);
//    }
//}