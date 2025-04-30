package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class utilities_new {
    private static BufferManager bufferManager;
    private static BTree<String, Rid> titleIndex;
    private static BTree<Integer, Rid> movieIdIndex;
    private static final byte PADDING_BYTE = 0x20;
    
    // We use UTF-8 for reading the file (the file’s encoding)
    // and ISO-8859-1 for storing and later printing fixed-length records.
    private static final String STORAGE_ENCODING = "ISO-8859-1";

    // Set the buffer manager instance
    public static void setBufferManager(BufferManager bm) {
        bufferManager = bm;
        initializeIndexes();
    }

    private static void initializeIndexes() {
        titleIndex = new BTreeImplementation<>(10, bufferManager, "movies.title.idx");
        movieIdIndex = new BTreeImplementation<>(10, bufferManager, "movies.movieid.idx");
    }

    // Load dataset, build indexes, and limit to 10,000 rows
    public static void loadDataset(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }
        // Read file in UTF-8 so the strings are correct
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            br.readLine(); // Skip header
            Page currentPage = bufferManager.createPage("movies.data");
            int currentPageId = currentPage.getPid();
            int totalRows = 0;
            int processedRows = 0;
//            final int MAX_ROWS = 10000;
            
            // Variables to track the 100th row information
            int hundredthMovieId = -1;
            String hundredthTitle = "";
            int hundredthPageId = -1;
            int hundredthSlotId = -1;

            while (true) {
                String line = br.readLine();
                if (line == null) break;
                processedRows++;
                String[] data = line.split("\t");
                if (data.length < 3) {
                    System.out.println("Skipping malformed line: " + line);
                    totalRows++;
                    continue;
                }
                String movieIdStr = data[0];
                String title = data[2];
                if (title.length() > 30) {
                    title = title.substring(0, 30);
                }
                try {
                    String numericPart = movieIdStr.replace("tt", "");
                    int movieId = Integer.parseInt(numericPart);
                    if (movieIdStr.getBytes(StandardCharsets.ISO_8859_1).length > 9) {
                        System.out.println("Skipping row with oversized movieId: " + movieIdStr);
                        totalRows++;
                        continue;
                    }
                    // Convert the Strings to bytes using ISO-8859-1 for fixed-length storage
                    Row row = new DataRow(movieIdStr.getBytes(StandardCharsets.ISO_8859_1), 
                                          title.getBytes(StandardCharsets.ISO_8859_1));

                    System.out.println("Raw input: movieId=" + movieIdStr + ", title=" + title);
                    System.out.println("Bytes: movieId=" + Arrays.toString(movieIdStr.getBytes(StandardCharsets.ISO_8859_1)));
                    System.out.println("       title=" + Arrays.toString(title.getBytes(StandardCharsets.ISO_8859_1)));

                    int slotId;
                    if (currentPage.isFull()) {
                        bufferManager.unpinPage("movies.data", currentPageId);
                        currentPage = bufferManager.createPage("movies.data");
                        currentPageId = currentPage.getPid();
//                        System.out.println("Loading row: movieId=" + movieIdStr + ", title='" + title + "'");
//                        slotId = currentPage.insertRow(row);
//                        bufferManager.markDirty("movies.data", currentPageId);
//                    } else {
//                        System.out.println("Loading row: movieId=" + movieIdStr + ", title='" + title + "'");
//                        slotId = currentPage.insertRow(row);
//                        bufferManager.markDirty("movies.data", currentPageId);
                    }
                    System.out.println("Loading row: movieId=" + movieIdStr + ", title='" + title + "'");
                    slotId = currentPage.insertRow(row);
                    System.out.println("Inserted row size: " + row.getSize() + ", slot=" + slotId + ", page=" + currentPageId);

                    bufferManager.markDirty("movies.data", currentPageId);



                    totalRows++;
                    System.out.println("Inserted Row with ID: " + slotId + " on Page ID: " + currentPageId);
                    Rid rid = new Rid(currentPageId, slotId);
//                    titleIndex.insert(title, rid);
//                    movieIdIndex.insert(movieId, rid);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid movie ID: " + movieIdStr);
                }
            }
            bufferManager.unpinPage("movies.data", currentPageId);
            bufferManager.force("movies.data");
//            bufferManager.force("movies.title.idx");
//            bufferManager.force("movies.movieid.idx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public static void loadWorkedOnDataset(String filepath) {
//        File file = new File(filepath);
//        if (!file.exists()) {
//            System.out.println("File not found: " + file.getAbsolutePath());
//            return;
//        }
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
//            br.readLine(); // Skip header
//            Page currentPage = bufferManager.createPage("workedon.data");
//            int currentPageId = currentPage.getPid();
//
//            while (true) {
//                String line = br.readLine();
//                if (line == null) break;
//                String[] data = line.split("\t");
//                if (data.length < 4 || data[0].isEmpty() || data[2].isEmpty() || data[3].isEmpty()) {
//                    System.out.println("Skipping malformed line: " + line);
//                    continue;
//                }
//                String movieIdStr = data[0];
//                String personIdStr = data[2];
//                String categoryStr = data[3];
//                System.out.println("Parsed: " + movieIdStr + ", " + personIdStr + ", " + categoryStr);
//
//                try {
//                    Row row = new WorkedOnRow(
//                            movieIdStr.getBytes(StandardCharsets.ISO_8859_1),
//                            personIdStr.getBytes(StandardCharsets.ISO_8859_1),
//                            categoryStr.getBytes(StandardCharsets.ISO_8859_1));
//
//                    int slotId;
//                    if (currentPage.isFull()) {
//                        bufferManager.unpinPage("workedon.data", currentPageId);
//                        currentPage = bufferManager.createPage("workedon.data");
//                        currentPageId = currentPage.getPid();
//                    }
//                    slotId = currentPage.insertRow(row);
//                    System.out.println("Inserted Row with ID: " + slotId + " on Page ID: " + currentPageId);
//                    bufferManager.markDirty("workedon.data", currentPageId);
//                } catch (Exception e) {
//                    System.out.println("Skipping invalid row: " + line);
//                }
//            }
//            bufferManager.unpinPage("workedon.data", currentPageId);
//            bufferManager.force("workedon.data");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }




    public static void loadWorkedOnDataset(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            br.readLine(); // Skip header
            Page currentPage = bufferManager.createPage("workedon.data");
            int currentPageId = currentPage.getPid();

            while (true) {
                String line = br.readLine();
                if (line == null) break;

                String[] data = line.split("\t");

                if (data.length < 4 || data[0].isEmpty() || data[2].isEmpty() || data[3].isEmpty()) {
                    System.out.println("Skipping malformed WorkedOn line: " + line);
                    continue;
                }

                String movieIdStr = data[0];   // tconst
                String personIdStr = data[2];  // nconst
                String categoryStr = data[3];  // category

                System.out.println("Parsed WorkedOn: " + movieIdStr + ", " + personIdStr + ", " + categoryStr);

                try {
                    Row row = new WorkedOnRow(
                            movieIdStr.getBytes(StandardCharsets.ISO_8859_1),
                            personIdStr.getBytes(StandardCharsets.ISO_8859_1),
                            categoryStr.getBytes(StandardCharsets.ISO_8859_1)
                    );

                    int slotId;
                    if (currentPage.isFull()) {
                        bufferManager.unpinPage("workedon.data", currentPageId);
                        currentPage = bufferManager.createPage("workedon.data");
                        currentPageId = currentPage.getPid();
                    }

                    slotId = currentPage.insertRow(row);
                    bufferManager.markDirty("workedon.data", currentPageId);
                } catch (Exception e) {
                    System.out.println("Error inserting WorkedOn row: " + e.getMessage());
                }
            }

            bufferManager.unpinPage("workedon.data", currentPageId);
            bufferManager.force("workedon.data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void loadPeopleDataset(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            br.readLine(); // Skip header
            Page currentPage = bufferManager.createPage("people.data");
            int currentPageId = currentPage.getPid();

            while (true) {
                String line = br.readLine();
                if (line == null) break;
                String[] data = line.split("\t");
                if (data.length < 2) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }
                String personIdStr = data[0];
                String nameStr = data[1];
                try {
                    Row row = new PeopleRow(
                            personIdStr.getBytes(StandardCharsets.ISO_8859_1),
                            nameStr.getBytes(StandardCharsets.ISO_8859_1));
                    int slotId;
                    if (currentPage.isFull()) {
                        bufferManager.unpinPage("people.data", currentPageId);
                        currentPage = bufferManager.createPage("people.data");
                        currentPageId = currentPage.getPid();
                    }
                    slotId = currentPage.insertRow(row);
                    System.out.println("Inserted Row with ID: " + slotId + " on Page ID: " + currentPageId);
                    bufferManager.markDirty("people.data", currentPageId);
                } catch (Exception e) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }
            bufferManager.unpinPage("people.data", currentPageId);
            bufferManager.force("people.data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Search by title
    public static List<Row> searchByTitle(String title) {
        List<Row> results = new ArrayList<>();
        Iterator<Rid> rids = titleIndex.search(title);
        while (rids.hasNext()) {
            Rid rid = rids.next();
            Page page = bufferManager.getPage("movies.data", rid.getPid());
            Row row = page.getRow(rid.getSid());
            results.add(row);
            bufferManager.unpinPage("movies.data", rid.getPid());
        }
        return results;
    }

    // Search by movie ID
    public static Row searchByMovieId(int movieId) {
        System.out.println("Searching for MovieID: " + movieId);
        Iterator<Rid> rids = movieIdIndex.search(movieId);
        if (!rids.hasNext()) {
            System.out.println("No matching Rid found in index");
            return null;
        }
        Rid rid = rids.next();
        System.out.println("Found Rid in index: PageID=" + rid.getPid() + ", SlotID=" + rid.getSid());
        try {
            Page dataPage = bufferManager.getPage("movies.data", rid.getPid());
            if (dataPage == null) {
                System.out.println("Error: Could not retrieve page " + rid.getPid());
                return null;
            }
            Row dataRow = dataPage.getRow(rid.getSid());
            bufferManager.unpinPage("movies.data", rid.getPid());
            if (dataRow == null) {
                System.out.println("Error: Could not retrieve row at slot " + rid.getSid());
                return null;
            }
            System.out.println("Successfully retrieved data row of type: " + dataRow.getClass().getName());
            return dataRow;
        } catch (Exception e) {
            System.out.println("Error retrieving data row: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Range search by title
    public static List<Row> rangeSearchByTitle(String startTitle, String endTitle) {
        List<Row> results = new ArrayList<>();
        Iterator<Rid> rids = titleIndex.rangeSearch(startTitle, endTitle);
        while (rids.hasNext()) {
            Rid rid = rids.next();
            Page page = bufferManager.getPage("movies.data", rid.getPid());
            Row row = page.getRow(rid.getSid());
            results.add(row);
            bufferManager.unpinPage("movies.data", rid.getPid());
        }
        return results;
    }

    // implements pin and update using LRU
    public static void pinAndUpdateLRU(PageImplementation page, LinkedHashMap<String, PageImplementation> lruCache, String key) {
        page.pin();
        lruCache.remove(key);
        lruCache.put(key, page);
    }

    public static byte[] removeTrailingBytes(byte[] input) {
        int endIndex = input.length;
        for (int i = input.length - 1; i >= 0; i--) {
            if (input[i] != PADDING_BYTE) {
                endIndex = i + 1;
                break;
            }
        }
        return Arrays.copyOf(input, endIndex);
    }
    
    public static byte[] truncateOrPadByteArray(byte[] value, int maxLength) {
        if (value.length > maxLength) {
            return Arrays.copyOf(value, maxLength);
        } else {
            byte[] padded = new byte[maxLength];
            System.arraycopy(value, 0, padded, 0, value.length);
            Arrays.fill(padded, value.length, maxLength, (byte) 0x20);
            return padded;
        }
    }

    public static void main(String[] args) {
        bufferManager = new BufferManagerImplementation(2048);
        setBufferManager(bufferManager);
        String filepath = "C:/Users/lavan/OneDrive/Desktop/645 Database/title.basics.tsv/title.basics.tsv";
        System.out.println("Loading dataset from: " + filepath);
        loadDataset(filepath);

    }
}
