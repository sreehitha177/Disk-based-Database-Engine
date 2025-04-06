package org.example;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class TestCases {
    // We'll use a single BufferManager instance for all tests.
    private static BufferManager bm;

    public static void main(String[] args) {
        // Initialize the BufferManager with sufficient pages.
        bm = new BufferManagerImplementation(50);

        // Run tests sequentially.
        testC1(); // Build title index and point query on title.
        testC2(); // Build movieId index and point query on movieId.
        testC3(); // Point queries on multiple keys for both indexes.
        testC4(); // (Range queries; if needed, can be added later)
    }

    /**
     * Test C1:
     * Scan the Movies table (movies.data) sequentially,
     * build a B+ tree index keyed on the title attribute,
     * and perform a point query for a known title.
     */
    public static void testC1() {
        System.out.println("----- Running Test C1 (Title Index) -----");

        File dataFile = new File("movies.data");
        if (!dataFile.exists()) {
            System.out.println("movies.data file does not exist. Please run Lab 1 loadDataset first.");
            return;
        }
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
        System.out.println("Scanning " + numPages + " pages in movies.data for title index.");

        // Create a new B+ tree index for titles.
        BTree<String, Rid> titleIndex = new BTreeImplementation<>(10, bm, "test.movies.title.idx");

        // Scan each page sequentially.
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) {
                System.out.println("Page " + pageId + " not available.");
                continue;
            }
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    // Convert stored title bytes to String.
                    String title = new String(dRow.getTitle(), StandardCharsets.ISO_8859_1).trim();
                    Rid rid = new Rid(page.getPid(), rowId);
                    titleIndex.insert(title, rid);
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }

        System.out.println("Finished building title index for Test C1.");
        // Perform a point query for a known title.
        String searchTitle = "Place du théâtre français";
        System.out.println("Point Query on Title Index for: " + searchTitle);
        Iterator<Rid> iter = titleIndex.search(searchTitle);
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Rid r = iter.next();
                Page p = bm.getPage("movies.data", r.getPid());
                DataRow dr = (DataRow) p.getRow(r.getSid());
                String foundTitle = new String(dr.getTitle(), StandardCharsets.ISO_8859_1).trim();
                System.out.println("Retrieved Title: " + foundTitle + " at Page: " + r.getPid() + ", Slot: " + r.getSid());
                bm.unpinPage("movies.data", r.getPid());
            }
        } else {
            System.out.println("No records found for title: " + searchTitle);
        }
        System.out.println("----- Test C1 Completed -----\n");
    }

    /**
     * Test C2:
     * Scan the Movies table (movies.data) sequentially,
     * build a B+ tree index keyed on the movieId attribute,
     * and perform a point query for a known movieId.
     */
    public static void testC2() {
        System.out.println("----- Running Test C2 (MovieId Index) -----");

        File dataFile = new File("movies.data");
        if (!dataFile.exists()) {
            System.out.println("movies.data file does not exist. Please run Lab 1 loadDataset first.");
            return;
        }
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
        System.out.println("Scanning " + numPages + " pages in movies.data for movieId index.");

        // Create a new B+ tree index for movieId.
        BTree<Integer, Rid> movieIdIndex = new BTreeImplementation<>(10, bm, "test.movies.movieid.idx");

        // Scan each page sequentially.
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) {
                System.out.println("Page " + pageId + " not available.");
                continue;
            }
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String movieIdStr = new String(dRow.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                    if (movieIdStr.isEmpty()) {
                        System.out.println("Skipping row at Page " + page.getPid() + ", Slot " + rowId + " because movieId is empty.");
                    } else {
                        String numericPart = movieIdStr.replace("tt", "");
                        try {
                            int movieId = Integer.parseInt(numericPart);
                            Rid rid = new Rid(page.getPid(), rowId);
                            movieIdIndex.insert(movieId, rid);
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping row at Page " + page.getPid() + ", Slot " + rowId +
                                               " due to NumberFormatException: " + e.getMessage());
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }

        System.out.println("Finished building movieId index for Test C2.");
        // Perform a point query for a known movieId.
        int searchMovieId = 101;
        System.out.println("Point Query on MovieId Index for: " + searchMovieId);
        Iterator<Rid> iter = movieIdIndex.search(searchMovieId);
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Rid r = iter.next();
                Page p = bm.getPage("movies.data", r.getPid());
                DataRow dr = (DataRow) p.getRow(r.getSid());
                String movieIdStr = new String(dr.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                String numericPart = movieIdStr.replace("tt", "");
                try {
                    int foundMovieId = Integer.parseInt(numericPart);
                    System.out.println("Retrieved MovieId: " + foundMovieId + " at Page: " + r.getPid() + ", Slot: " + r.getSid());
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing movieId at Page " + r.getPid() + ", Slot " + r.getSid());
                }
                bm.unpinPage("movies.data", r.getPid());
            }
        } else {
            System.out.println("No records found for movieId: " + searchMovieId);
        }
        System.out.println("----- Test C2 Completed -----\n");
    }

    /**
     * Test C3:
     * Perform point queries on multiple keys using both the title and movieId indexes.
     * For the title index, search for several known titles and retrieve the corresponding rows.
     * For the movieId index, search for several known movieIds and retrieve the corresponding rows.
     */
    public static void testC3() {
        System.out.println("----- Running Test C3 (Multiple Point Queries) -----");

        File dataFile = new File("movies.data");
        if (!dataFile.exists()) {
            System.out.println("movies.data file does not exist. Please run Lab 1 loadDataset first.");
            return;
        }
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);

        // Build Title Index.
        BTree<String, Rid> titleIndex = new BTreeImplementation<>(10, bm, "test.movies.title.idx");
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String title = new String(dRow.getTitle(), StandardCharsets.ISO_8859_1).trim();
                    Rid rid = new Rid(page.getPid(), rowId);
                    titleIndex.insert(title, rid);
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        // Define an array of title keys to test.
        String[] titleKeys = {"Place du théâtre français", "The Blue Bandanna", "A Misalliance", "Nonexistent Title"};
        for (String key : titleKeys) {
            System.out.println("Point Query on Title Index for: " + key);
            Iterator<Rid> iter = titleIndex.search(key);
            int count = 0;
            while (iter.hasNext()) {
                Rid r = iter.next();
                Page p = bm.getPage("movies.data", r.getPid());
                DataRow dr = (DataRow) p.getRow(r.getSid());
                String foundTitle = new String(dr.getTitle(), StandardCharsets.ISO_8859_1).trim();
                System.out.println("Retrieved Title: " + foundTitle + " at Page: " + r.getPid() + ", Slot: " + r.getSid());
                bm.unpinPage("movies.data", r.getPid());
                count++;
            }
            if (count == 0) {
                System.out.println("No records found for title: " + key);
            }
        }

        // Build MovieId Index.
        BTree<Integer, Rid> movieIdIndex = new BTreeImplementation<>(10, bm, "test.movies.movieid.idx");
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String movieIdStr = new String(dRow.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                    if (!movieIdStr.isEmpty()) {
                        String numericPart = movieIdStr.replace("tt", "");
                        try {
                            int movieId = Integer.parseInt(numericPart);
                            Rid rid = new Rid(page.getPid(), rowId);
                            movieIdIndex.insert(movieId, rid);
                        } catch (NumberFormatException e) {
                            // Skip invalid movieId.
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        // Define an array of movieId keys to test.
        int[] movieIdKeys = {101, 202, 9999};
        for (int key : movieIdKeys) {
            System.out.println("Point Query on MovieId Index for: " + key);
            Iterator<Rid> iter = movieIdIndex.search(key);
            int count = 0;
            while (iter.hasNext()) {
                Rid r = iter.next();
                Page p = bm.getPage("movies.data", r.getPid());
                DataRow dr = (DataRow) p.getRow(r.getSid());
                String movieIdStr = new String(dr.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                String numericPart = movieIdStr.replace("tt", "");
                try {
                    int foundMovieId = Integer.parseInt(numericPart);
                    System.out.println("Retrieved MovieId: " + foundMovieId + " at Page: " + r.getPid() + ", Slot: " + r.getSid());
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing movieId at Page " + r.getPid() + ", Slot " + r.getSid());
                }
                bm.unpinPage("movies.data", r.getPid());
                count++;
            }
            if (count == 0) {
                System.out.println("No records found for movieId: " + key);
            }
        }
        System.out.println("----- Test C3 Completed -----\n");
    }

    /**
     * Test C4:
     * Run a range query using an index.
     * For the title index, retrieve all records with titles within a specified range.
     * For the movieId index, retrieve all records with movieIds within a specified numeric range.
     */
    public static void testC4() {
        System.out.println("----- Running Test C4 (Range Queries) -----");

        File dataFile = new File("movies.data");
        if (!dataFile.exists()) {
            System.out.println("movies.data file does not exist. Please run Lab 1 loadDataset first.");
            return;
        }
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);

        // Build Title Index.
        BTree<String, Rid> titleIndex = new BTreeImplementation<>(10, bm, "test.movies.title.idx");
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String title = new String(dRow.getTitle(), StandardCharsets.ISO_8859_1).trim();
                    Rid rid = new Rid(page.getPid(), rowId);
                    titleIndex.insert(title, rid);
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }

        // Define a title range.
        String startTitle = "The";
        String endTitle = "Thez";
        System.out.println("Range Query on Title Index for titles between \"" + startTitle + "\" and \"" + endTitle + "\":");
        Iterator<Rid> titleRangeIter = titleIndex.rangeSearch(startTitle, endTitle);
        int titleCount = 0;
        while (titleRangeIter.hasNext()) {
            Rid r = titleRangeIter.next();
            Page p = bm.getPage("movies.data", r.getPid());
            DataRow dr = (DataRow) p.getRow(r.getSid());
            String foundTitle = new String(dr.getTitle(), StandardCharsets.ISO_8859_1).trim();
            System.out.println("Found Title: " + foundTitle + " at Page: " + r.getPid() + ", Slot: " + r.getSid());
            bm.unpinPage("movies.data", r.getPid());
            titleCount++;
        }
        System.out.println("Total titles found in range: " + titleCount);

        // Build MovieId Index.
        BTree<Integer, Rid> movieIdIndex = new BTreeImplementation<>(10, bm, "test.movies.movieid.idx");
        for (int pageId = 0; pageId < numPages; pageId++) {
            Page page = bm.getPage("movies.data", pageId);
            if (page == null) continue;
            int rowId = 0;
            while (true) {
                Row row = page.getRow(rowId);
                if (row == null) break;
                if (row instanceof DataRow) {
                    DataRow dRow = (DataRow) row;
                    String movieIdStr = new String(dRow.getMovieId(), StandardCharsets.ISO_8859_1).trim();
                    if (!movieIdStr.isEmpty()) {
                        String numericPart = movieIdStr.replace("tt", "");
                        try {
                            int movieId = Integer.parseInt(numericPart);
                            Rid rid = new Rid(page.getPid(), rowId);
                            movieIdIndex.insert(movieId, rid);
                        } catch (NumberFormatException e) {
                            // Skip invalid movieId.
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }

        // Define a movieId range.
        int startMovieId = 101;
        int endMovieId = 1000;
        System.out.println("Range Query on MovieId Index for movieIds between " + startMovieId + " and " + endMovieId + ":");
        Iterator<Rid> movieRangeIter = movieIdIndex.rangeSearch(startMovieId, endMovieId);
        int movieCount = 0;
        while (movieRangeIter.hasNext()) {
            Rid r = movieRangeIter.next();
            Page p = bm.getPage("movies.data", r.getPid());
            DataRow dr = (DataRow) p.getRow(r.getSid());
            String movieIdStr = new String(dr.getMovieId(), StandardCharsets.ISO_8859_1).trim();
            String numericPart = movieIdStr.replace("tt", "");
            try {
                int foundMovieId = Integer.parseInt(numericPart);
                System.out.println("Found MovieId: " + foundMovieId + " at Page: " + r.getPid() + ", Slot: " + r.getSid());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing movieId at Page: " + r.getPid() + ", Slot: " + r.getSid());
            }
            bm.unpinPage("movies.data", r.getPid());
            movieCount++;
        }
        System.out.println("Total movieIds found in range: " + movieCount);

        System.out.println("----- Test C4 Completed -----\n");
    }
}
