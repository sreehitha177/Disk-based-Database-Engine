// src/test/java/org/example/perf/QueryPerformanceTest.java
package org.example.perf;

import org.example.*;
import org.example.Executor.*;
import org.example.BufferManagement.*;
import org.example.metrics.PageCounter;
import org.example.Rows.Row;
import org.junit.*;

import java.util.*;

public class QueryPerformanceTest {

    /* ---------- constants you know up‑front ---------- */
    private static final int  BUF_SIZE = 100;      // the B parameter
    private static final int  P_MOVIES   = 96;     // #pages loaded by preprocessing
    private static final int  P_WORKEDON =  ? ;    // fill in from your loader output
    private static final int  P_PEOPLE   =  ? ;
    private static final int  C_OTHER    = 4;      // #buffer frames “C” (see hand‑out)
    private static final int  N_BLOCK    = (BUF_SIZE - C_OTHER) / 2;

    private BufferManager buf;

    @Before
    public void buildInMemoryTables() {
        buf = new BufferManagerImplementation(BUF_SIZE);
        utilities_new.setBufferManager(buf);

        /* load the three data files that pre_process wrote earlier */
        utilities_new.loadDataset          ("cleaned_movies.tsv");
        utilities_new.loadWorkedOnDataset  ("cleaned_workedon.tsv");
        utilities_new.loadPeopleDataset    ("cleaned_people.tsv");
    }

    /* -------------------------------------------------- */
    @Test
    public void profilePipeline() {
        Map<String,long[]> stats = new LinkedHashMap<>();

        /* ---------- operator 1: range selection on Movies ---------- */
        stats.put("σ_title", runAndCount(() -> {
            Operator sel = new RangeSelectionOperator(
                    new ScanOperator(buf,"movies.data"), "A", "Z");
            count(sel);
        }));
        /* ---------- operator 2: σ+π on WorksOn (materialised) ------ */
        stats.put("σπ_workedon", runAndCount(() -> {
            Operator proj = new ProjectionOperator(
                    new SelectionOperator(
                        new ScanOperator(buf,"workedon.data"), "director"),
                    buf);
            count(proj);
        }));
        /* ---------- operator 3: BNLJ (Movies ⨝ Temp) --------------- */
        stats.put("join1", runAndCount(() -> {
            Operator j = new BNLJOperator(
                    new RangeSelectionOperator(
                        new ScanOperator(buf,"movies.data"),"A","Z"),
                    new ProjectionOperator(
                        new SelectionOperator(
                            new ScanOperator(buf,"workedon.data"),"director"),
                    buf),
                    buf, BUF_SIZE);
            count(j);
        }));
        /* ---------- operator 4: BNLJ (prev ⨝ People) --------------- */
        stats.put("join2", runAndCount(() -> {
            Operator j2 = new BNLJOperator(
                    /* left = result of join1 re‑opened */
                    new BNLJOperator(
                        new RangeSelectionOperator(
                            new ScanOperator(buf,"movies.data"),"A","Z"),
                        new ProjectionOperator(
                            new SelectionOperator(
                                new ScanOperator(buf,"workedon.data"),"director"),
                        buf),
                        buf, BUF_SIZE),
                    new ScanOperator(buf,"people.data"),
                    buf, BUF_SIZE);
            count(j2);
        }));
        /* ---------- operator 5: final projection ------------------- */
        stats.put("π_title,name", runAndCount(() -> {
            Operator pipeline =
                new FinalProjectionOperator(
                    new BNLJOperator(
                        new BNLJOperator(
                            new RangeSelectionOperator(
                               new ScanOperator(buf,"movies.data"),"A","Z"),
                            new ProjectionOperator(
                               new SelectionOperator(
                                   new ScanOperator(buf,"workedon.data"),"director"),
                               buf),
                            buf, BUF_SIZE),
                        new ScanOperator(buf,"people.data"),
                        buf, BUF_SIZE));
            count(pipeline);
        }));

        /* ---------- print & assert -------------------------------- */
        System.out.printf("%-15s %10s %10s %10s%n","OPERATOR","READS","EST","R/W");
        for (var e: stats.entrySet()) {
            long actR = e.getValue()[0], actW = e.getValue()[1], est = estimate(e.getKey());
            System.out.printf("%-15s %10d %10d %5d/%d%n",
                    e.getKey(), actR, est, actR, actW);
            Assert.assertTrue("too many reads for "+e.getKey(), actR <= 1.5*est);
        }
    }

    /* helpers ------------------------------------------------------ */
    private static void count(Operator op) {
        op.open(); while (op.next()!=null); op.close();
    }

    /** run λ, return {reads,writes} */
    private static long[] runAndCount(Runnable r){
        PageCounter.reset(); r.run(); return new long[]{PageCounter.reads(), PageCounter.writes()};
    }

    /** textbook formulas – adjust once and reuse */
    private static long estimate(String op){
        return switch (op){
            case "σ_title"      -> P_MOVIES;                     // full scan
            case "σπ_workedon"  -> P_WORKEDON + writtenTempPages();
            case "join1"        -> blocks(P_MOVIES)*P_WORKEDON;  // BNL outer‑blocks * inner pages
            case "join2"        -> blocks(tempPages())*P_PEOPLE; // analogous
            case "π_title,name" -> 0;                            // pipelined
            default -> 0;
        };
    }
    private static int blocks(int pages){ return (int)Math.ceil((double)pages / N_BLOCK); }
    private static int tempPages(){ /* depends on how many directors rows you kept */ return ?; }
    private static int writtenTempPages(){ return tempPages(); }
}