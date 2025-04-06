package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestCasesPerformance {
    private static BufferManager bm;

    // Data structures to store test results
    private static final List<PerformanceData> p1Data = new ArrayList<>();
    private static final List<PerformanceData> p2Data = new ArrayList<>();

    static class PerformanceData {
        String rangeEnd;
        int count;
        long directTime;
        long indexTime;
        double ratio;

        public PerformanceData(String rangeEnd, int count, long directTime, long indexTime, double ratio) {
            this.rangeEnd = rangeEnd;
            this.count = count;
            this.directTime = directTime;
            this.indexTime = indexTime;
            this.ratio = ratio;
        }
    }

    public static void main(String[] args) {
        // Initialize the BufferManager
        bm = new BufferManagerImplementation(50);

        File dataFile = new File("movies.data");
        if (!dataFile.exists()) {
            System.out.println("movies.data file does not exist. Please run Lab 1 loadDataset first.");
            return;
        }

        // Initialize the visualizer
        PerformanceVisualizer visualizer = new PerformanceVisualizer();

        // Run tests
        runPerformanceTestTitle(visualizer);
        runPerformanceTestMovieId(visualizer);

        // Show visualizations
        visualizer.showVisualizations(p1Data, p2Data);
    }

    private static void runPerformanceTestTitle(PerformanceVisualizer visualizer) {
        System.out.println("----- Running Performance Test P1 (Title Range Queries) -----");
        BTree<String, Rid> titleIndex = buildTitleIndex();

        String startKey = "A";
        for (char endChar = 'B'; endChar <= 'Z'; endChar++) {
            String endKey = String.valueOf(endChar);

            long startTime = System.nanoTime();
            int countDirect = directScanTitleRange(startKey, endKey);
            long durationDirect = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            int countIndex = indexScanTitleRange(titleIndex, startKey, endKey);
            long durationIndex = System.nanoTime() - startTime;

            double ratio = durationDirect / (durationIndex + 1.0);

            // Store results
            PerformanceData data = new PerformanceData(endKey, countDirect, durationDirect, durationIndex, ratio);
            p1Data.add(data);

            // Update visualization
            visualizer.updateP1Plot(p1Data);

            System.out.println(endKey + "," + countDirect + "," + durationDirect + "," + durationIndex + "," + ratio);
        }
        System.out.println("----- Performance Test P1 Completed -----\n");
    }

    private static void runPerformanceTestMovieId(PerformanceVisualizer visualizer) {
        System.out.println("----- Running Performance Test P2 (MovieId Range Queries) -----");
        BTree<Integer, Rid> movieIdIndex = buildMovieIdIndex();

        int startKey = 101;
        for (int endKey = 200; endKey <= 1000; endKey += 100) {
            long startTime = System.nanoTime();
            int countDirect = directScanMovieIdRange(startKey, endKey);
            long durationDirect = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            int countIndex = indexScanMovieIdRange(movieIdIndex, startKey, endKey);
            long durationIndex = System.nanoTime() - startTime;

            double ratio = durationDirect / (durationIndex + 1.0);

            // Store results
            PerformanceData data = new PerformanceData(String.valueOf(endKey), countDirect, durationDirect, durationIndex, ratio);
            p2Data.add(data);

            // Update visualization
            visualizer.updateP2Plot(p2Data);

            System.out.println(endKey + "," + countDirect + "," + durationDirect + "," + durationIndex + "," + ratio);
        }
        System.out.println("----- Performance Test P2 Completed -----\n");
    }

    private static BTree<String, Rid> buildTitleIndex() {
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
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
        return titleIndex;
    }

    private static int directScanTitleRange(String startKey, String endKey) {
        int count = 0;
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
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
                    if (title.compareTo(startKey) >= 0 && title.compareTo(endKey) <= 0) {
                        count++;
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return count;
    }

    private static int indexScanTitleRange(BTree<String, Rid> titleIndex, String startKey, String endKey) {
        int count = 0;
        Iterator<Rid> iter = titleIndex.rangeSearch(startKey, endKey);
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }

    private static BTree<Integer, Rid> buildMovieIdIndex() {
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
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
                            // Skip invalid movieIds
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return movieIdIndex;
    }

    private static int directScanMovieIdRange(int startKey, int endKey) {
        int count = 0;
        File dataFile = new File("movies.data");
        long fileSize = dataFile.length();
        int numPages = (int) (fileSize / Page.PAGE_SIZE);
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
                            if (movieId >= startKey && movieId <= endKey) {
                                count++;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid movieIds
                        }
                    }
                }
                rowId++;
            }
            bm.unpinPage("movies.data", pageId);
        }
        return count;
    }

    private static int indexScanMovieIdRange(BTree<Integer, Rid> movieIdIndex, int startKey, int endKey) {
        int count = 0;
        Iterator<Rid> iter = movieIdIndex.rangeSearch(startKey, endKey);
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }
}

class PerformanceVisualizer {
    private final JFrame p1Frame;
    private final JFrame p2Frame;
    private final P1PlotPanel p1Panel;
    private final P2PlotPanel p2Panel;

    public PerformanceVisualizer() {
        // Initialize P1 visualization
        p1Frame = new JFrame("Title Range Queries (P1)");
        p1Frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        p1Frame.setSize(800, 600);
        p1Panel = new P1PlotPanel();
        p1Frame.add(p1Panel);
        p1Frame.setVisible(true);

        // Initialize P2 visualization
        p2Frame = new JFrame("MovieID Range Queries (P2)");
        p2Frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        p2Frame.setSize(800, 600);
        p2Panel = new P2PlotPanel();
        p2Frame.add(p2Panel);
        p2Frame.setVisible(true);
    }

    public void updateP1Plot(List<TestCasesPerformance.PerformanceData> data) {
        p1Panel.updateData(data);
        p1Panel.repaint();
    }

    public void updateP2Plot(List<TestCasesPerformance.PerformanceData> data) {
        p2Panel.updateData(data);
        p2Panel.repaint();
    }

    public void showVisualizations(List<TestCasesPerformance.PerformanceData> p1Data,
                                   List<TestCasesPerformance.PerformanceData> p2Data) {
        updateP1Plot(p1Data);
        updateP2Plot(p2Data);
    }

    static class P1PlotPanel extends JPanel {
        private List<TestCasesPerformance.PerformanceData> data = new ArrayList<>();

        public void updateData(List<TestCasesPerformance.PerformanceData> newData) {
            this.data = new ArrayList<>(newData);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw axes
            int margin = 50;
            int width = getWidth() - 2 * margin;
            int height = getHeight() - 2 * margin;

            g2.drawLine(margin, margin + height, margin + width, margin + height);
            g2.drawLine(margin, margin, margin, margin + height);

            // Labels
            g2.drawString("Title Range", margin + width/2 - 30, margin + height + 30);
            g2.drawString("Time (ms)", margin - 40, margin + height/2);
            g2.drawString("Title Range Queries (P1)", margin + width/2 - 100, margin - 20);

            // Calculate scales
            double maxTime = data.stream()
                    .mapToLong(d -> Math.max(d.directTime, d.indexTime))
                    .max().orElse(1) / 1_000_000.0;
            int xStep = width / (data.size() - 1);

            // Plot data
            for (int i = 0; i < data.size(); i++) {
                TestCasesPerformance.PerformanceData d = data.get(i);
                int x = margin + i * xStep;

                // Direct scan (red)
                int y1 = margin + height - (int)((d.directTime/1_000_000.0) * (height/maxTime));
                g2.setColor(Color.RED);
                g2.fillOval(x-3, y1-3, 6, 6);

                // Index access (blue)
                int y2 = margin + height - (int)((d.indexTime/1_000_000.0) * (height/maxTime));
                g2.setColor(Color.BLUE);
                g2.fillOval(x-3, y2-3, 6, 6);

                // Connect points
                if (i > 0) {
                    TestCasesPerformance.PerformanceData prev = data.get(i-1);
                    int prevX = margin + (i-1) * xStep;
                    int prevY1 = margin + height - (int)((prev.directTime/1_000_000.0) * (height/maxTime));
                    int prevY2 = margin + height - (int)((prev.indexTime/1_000_000.0) * (height/maxTime));

                    g2.setColor(Color.RED);
                    g2.drawLine(prevX, prevY1, x, y1);

                    g2.setColor(Color.BLUE);
                    g2.drawLine(prevX, prevY2, x, y2);
                }

                // X-axis labels
                g2.setColor(Color.BLACK);
                g2.drawString(d.rangeEnd, x-10, margin + height + 15);
            }

            // Legend
            g2.setColor(Color.RED);
            g2.drawString("Full Scan", margin + width - 100, margin + 30);
            g2.setColor(Color.BLUE);
            g2.drawString("Index Access", margin + width - 100, margin + 50);
        }
    }

    static class P2PlotPanel extends JPanel {
        private List<TestCasesPerformance.PerformanceData> data = new ArrayList<>();

        public void updateData(List<TestCasesPerformance.PerformanceData> newData) {
            this.data = new ArrayList<>(newData);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw axes
            int margin = 50;
            int width = getWidth() - 2 * margin;
            int height = getHeight() - 2 * margin;

            g2.drawLine(margin, margin + height, margin + width, margin + height);
            g2.drawLine(margin, margin, margin, margin + height);

            // Labels
            g2.drawString("MovieID Range", margin + width/2 - 30, margin + height + 30);
            g2.drawString("Time (ms)", margin - 40, margin + height/2);
            g2.drawString("MovieID Range Queries (P2)", margin + width/2 - 100, margin - 20);

            // Calculate scales
            double maxTime = data.stream()
                    .mapToLong(d -> Math.max(d.directTime, d.indexTime))
                    .max().orElse(1) / 1_000_000.0;
            int xStep = width / (data.size() - 1);

            // Plot data
            for (int i = 0; i < data.size(); i++) {
                TestCasesPerformance.PerformanceData d = data.get(i);
                int x = margin + i * xStep;

                // Direct scan (red)
                int y1 = margin + height - (int)((d.directTime/1_000_000.0) * (height/maxTime));
                g2.setColor(Color.RED);
                g2.fillOval(x-3, y1-3, 6, 6);

                // Index access (blue)
                int y2 = margin + height - (int)((d.indexTime/1_000_000.0) * (height/maxTime));
                g2.setColor(Color.BLUE);
                g2.fillOval(x-3, y2-3, 6, 6);

                // Connect points
                if (i > 0) {
                    TestCasesPerformance.PerformanceData prev = data.get(i-1);
                    int prevX = margin + (i-1) * xStep;
                    int prevY1 = margin + height - (int)((prev.directTime/1_000_000.0) * (height/maxTime));
                    int prevY2 = margin + height - (int)((prev.indexTime/1_000_000.0) * (height/maxTime));

                    g2.setColor(Color.RED);
                    g2.drawLine(prevX, prevY1, x, y1);

                    g2.setColor(Color.BLUE);
                    g2.drawLine(prevX, prevY2, x, y2);
                }

                // X-axis labels
                g2.setColor(Color.BLACK);
                g2.drawString(d.rangeEnd, x-10, margin + height + 15);
            }

            // Legend
            g2.setColor(Color.RED);
            g2.drawString("Full Scan", margin + width - 100, margin + 30);
            g2.setColor(Color.BLUE);
            g2.drawString("Index Access", margin + width - 100, margin + 50);
        }
    }
}