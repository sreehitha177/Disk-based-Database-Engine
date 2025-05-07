package org.example.metrics;

/**
 * Global page‑I/O counter.
 * <p>Every time the buffer manager has to fetch a page from disk
 *       call {@code PageCounter.incRead()}</p>
 * <p>Every time it physically writes a dirty page back
 *       call {@code PageCounter.incWrite()}</p>
 * Thread‑safe and dependency‑free.
 */
public final class PageCounter {
    private static long reads  = 0;
    private static long writes = 0;

    private PageCounter() {}                      // util‑class, never instantiated

    public static synchronized void incRead () { reads++;  }
    public static synchronized void incWrite() { writes++; }

    public static synchronized long reads () { return reads;  }
    public static synchronized long writes() { return writes; }

    /** reset both counters to 0 – used by each test phase */
    public static synchronized void reset()  { reads = writes = 0; }
}