package org.example;

import java.io.*;
        import java.nio.charset.StandardCharsets;
import java.util.*;
        import java.util.stream.Collectors;

public class utilities_new {
    private static BufferManager bufferManager;
    private static BTree<String, Rid> titleIndex;
    private static BTree<Integer, Rid> movieIdIndex;
    private static final byte PADDING_BYTE = 0x20;

    // Set the buffer manager instance
    public static void setBufferManager(BufferManager bm) {
        bufferManager = bm;
        // Initialize indexes after buffer manager is set
        initializeIndexes();
    }

    private static void initializeIndexes() {
        // Create B+ tree indexes
        titleIndex = new BTreeImplementation<>(10, bufferManager, "movies.title.idx");
        movieIdIndex = new BTreeImplementation<>(10, bufferManager, "movies.movieid.idx");
    }


    // Modified loadDataset to build indexes
    public static void loadDataset(String filepath) {
        File file = new File(filepath);

        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            // Skip header
            br.readLine();

            Page currentPage = bufferManager.createPage("movies.data");
            int currentPageId = currentPage.getPid();
            int currentSlot = 0;
            int totalRows = 0;

            while (true) {
                String line = br.readLine();
                if (line == null) break;

                String[] data = line.split("\t");
                if (data.length < 3) {
                    System.out.println("Skipping malformed line: " + line);
                    totalRows++;
                    continue;
                }

                // Process movie data
                String movieIdStr = data[0];
                String title = data[2];
                if (title.length() > 30) {
                    title = title.substring(0, 30);
                }


                try {
//                    int movieId = Integer.parseInt(movieIdStr);
                    String numericPart = movieIdStr.replace("tt", "");
                    int movieId = Integer.parseInt(numericPart);

                    if (movieIdStr.getBytes(StandardCharsets.UTF_8).length > 9) {
                        System.out.println("Skipping row with oversized movieId: " + movieIdStr);
                        totalRows++;
//                        finalRows++;
                        continue;
                    }
//                    title = title.length() > 30 ? title.substring(0, 30) : title;

                    // Create row and insert into page
                    Row row = new DataRow(movieIdStr.getBytes(StandardCharsets.UTF_8),
                            title.getBytes(StandardCharsets.UTF_8));

                    // Insert into data page
                    int slotId;
                    if (currentPage.isFull()) {
                        // Page full, create new page
                        bufferManager.unpinPage("movies.data", currentPageId);
                        currentPage = bufferManager.createPage("movies.data");
                        currentPageId = currentPage.getPid();
                        currentSlot = 0;
                        slotId = currentPage.insertRow(row);
                    }
                    else {
                        slotId = currentPage.insertRow(row);
                    }

                    totalRows++;
                    System.out.println("Inserted Row with ID: " + slotId + " on Page ID: " + currentPageId);

                    // Create index entries
                    Rid rid = new Rid(currentPageId, slotId);
                    titleIndex.insert(title, rid);
                    movieIdIndex.insert(movieId, rid);

//                    totalRows++;
                    currentSlot++;
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid movie ID: " + movieIdStr);
                }
            }

            bufferManager.unpinPage("movies.data", currentPageId);
            System.out.println("Total rows processed: " + totalRows);

            // Force write all dirty pages to disk
            bufferManager.force("movies.data");
            bufferManager.force("movies.title.idx");
            bufferManager.force("movies.movield.idx");
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
        Iterator<Rid> rids = movieIdIndex.search(movieId);
        if (!rids.hasNext()) return null;

        Rid rid = rids.next();
        Page page = bufferManager.getPage("movies.data", rid.getPid());
        Row row = page.getRow(rid.getSid());
        bufferManager.unpinPage("movies.data", rid.getPid());
        return row;
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

    // Existing utility methods...
//    public static void pinAndUpdateLRU(PageImplementation page, LinkedHashMap<Integer, PageImplementation> lruCache) {
//        page.pin();
//        lruCache.remove(page.getPid());
//        lruCache.put(page.getPid(), page);
//    }

    public static void pinAndUpdateLRU(PageImplementation page, LinkedHashMap<String, PageImplementation> lruCache, String key) {
        page.pin();
        lruCache.remove(key); // Remove if exists
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
        // Initialize buffer manager with 2048 pages
        bufferManager = new BufferManagerImplementation(5);
        setBufferManager(bufferManager);

        // Load dataset and build indexes
//        String filepath = "/Users/sreehithanarayana/Downloads/title.basics.tsv";
//        String filepath = "/Users/sreehithanarayana/Desktop/Hellothere.tsv";
        String filepath = "/Users/sreehithanarayana/Downloads/test_sample.tsv";

        loadDataset(filepath);


        DataRow row = (DataRow)searchByMovieId(12345);


        System.out.println(new String(row.getTitle()));
        System.out.println(new String(row.getMovieId()));
        // Example queries

//        List<Row> inceptionResults = searchByTitle("Blacksmith Scene");
////        System.out.println(inceptionResults);
//        if(inceptionResults!=null) {
//            System.out.println("\nSearch Results for 'Blacksmith Scene':");
//            inceptionResults.forEach(row ->
//                    System.out.println(new String(row.movieId) + ": " + new String(row.title)));
//        }
//        else{
//            System.out.println("'Inception' not found");
//        }
//
//        System.out.println("\nSearch for Movie ID 12345:");
//        Row movie = searchByMovieId(12345);
//        if (movie != null) {
//            System.out.println(new String(movie.movieId) + ": " + new String(movie.title));
//        }
//
//        System.out.println("\nMovies between 'A' and 'C':");
//        List<Row> rangeResults = rangeSearchByTitle("A", "C");
//        rangeResults.forEach(row ->
//                System.out.println(new String(row.movieId) + ": " + new String(row.title)));
    }
}