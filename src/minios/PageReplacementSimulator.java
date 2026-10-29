// File: src/minios/PageReplacementSimulator.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * PageReplacementSimulator - when a needed page is not in RAM, the OS must
 * evict ("replace") some page that IS in RAM. This class compares three
 * classic replacement policies on the same reference string:
 *
 *   FIFO      (First-In First-Out) : evict the page that entered memory first.
 *   LRU       (Least Recently Used) : evict the page that was used least recently.
 *   Optimal   (OPT / Belady's)      : evict the page that will be used furthest
 *                                     in the future (theoretical best - a real
 *                                     OS cannot know the future, it is used as
 *                                     a benchmark).
 *
 * After every reference we print the state of the frames and whether the
 * reference was a PAGE FAULT (had to load a page) or a HIT (page already
 * there - no replacement needed).
 *
 * ASSUMPTIONS
 *   - Frames start empty.
 *   - FIFO advances its replacement pointer only when a fault occurs.
 *   - Optimal ties (equal future distance) pick the lower-numbered slot.
 */
public class PageReplacementSimulator {

    /** One algorithm's result: a row per reference + the fault count. */
    private static class Result {
        String algorithm;
        List<String> rows = new ArrayList<>(); // pre-formatted lines for the table
        int faults = 0;

        Result(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    public static void run(InputHelper in) {
        System.out.println("PAGE REPLACEMENT - FIFO, LRU and Optimal");
        while (true) {
            System.out.println();
            System.out.println("  1) Run all three algorithms on a reference string");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 1);
            if (choice == 0) {
                return;
            }
            System.out.println();
            runOnce(in);
        }
    }

    private static void runOnce(InputHelper in) {
        int count = in.readIntInRange("How many page references? ", 1, 100);
        int[] refs = in.readIntArray("Reference string (" + count + " page number(s), space separated): ", count);
        for (int r : refs) {
            if (r < 0) {
                System.out.println("  [!] Page numbers must be >= 0. Restarting this run.");
                return;
            }
        }
        int frames = in.readIntInRange("Number of frames in RAM: ", 1, 20);
        System.out.println();

        Result fifo = runFifo(refs, frames);
        Result lru = runLru(refs, frames);
        Result opt = runOptimal(refs, frames);

        printTable(fifo, frames);
        printTable(lru, frames);
        printTable(opt, frames);

        System.out.println("COMPARISON");
        System.out.println("  Algorithm   Page faults");
        System.out.println("  ---------   -----------");
        System.out.printf("  %-9s %11d%n", "FIFO", fifo.faults);
        System.out.printf("  %-9s %11d%n", "LRU", lru.faults);
        System.out.printf("  %-9s %11d%n", "Optimal", opt.faults);
        System.out.println();
    }

    // ------------------------------------------------------------------
    // FIFO: keep a circular pointer to the OLDEST page; replace it on a fault.
    // ------------------------------------------------------------------
    private static Result runFifo(int[] refs, int frames) {
        Result r = new Result("FIFO");
        Integer[] slots = new Integer[frames]; // null = empty frame
        int next = 0; // circular replacement pointer

        for (int ref : refs) {
            int pos = indexOf(slots, ref);
            if (pos >= 0) {
                r.rows.add(formatRow(ref, slots, "HIT"));
            } else {
                r.faults++;
                slots[next] = ref;               // evict whatever lives at 'next'
                next = (next + 1) % frames;      // pointer moves only on a FAULT
                r.rows.add(formatRow(ref, slots, "FAULT"));
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // LRU: replace the page whose last use is furthest in the past.
    // We keep a "clock" that increases on every reference; each frame stores
    // the clock value of its last use.
    // ------------------------------------------------------------------
    private static Result runLru(int[] refs, int frames) {
        Result r = new Result("LRU");
        Integer[] slots = new Integer[frames];
        int[] lastUse = new int[frames];
        int clock = 1;

        for (int ref : refs) {
            int pos = indexOf(slots, ref);
            if (pos >= 0) {
                lastUse[pos] = clock++; // touched again -> most recently used
                r.rows.add(formatRow(ref, slots, "HIT"));
            } else {
                r.faults++;
                int victim = pickEmpty(slots);
                if (victim < 0) {
                    victim = leastRecentlyUsed(slots, lastUse);
                }
                slots[victim] = ref;
                lastUse[victim] = clock++;
                r.rows.add(formatRow(ref, slots, "FAULT"));
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Optimal: replace the page that will be used furthest in the future
    // (or never again). Looks ahead in the reference string.
    // ------------------------------------------------------------------
    private static Result runOptimal(int[] refs, int frames) {
        Result r = new Result("Optimal");
        Integer[] slots = new Integer[frames];

        for (int i = 0; i < refs.length; i++) {
            int ref = refs[i];
            int pos = indexOf(slots, ref);
            if (pos >= 0) {
                r.rows.add(formatRow(ref, slots, "HIT"));
            } else {
                r.faults++;
                int victim = pickEmpty(slots);
                if (victim < 0) {
                    victim = optimalVictim(slots, refs, i + 1);
                }
                slots[victim] = ref;
                r.rows.add(formatRow(ref, slots, "FAULT"));
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Small helpers shared by the algorithms
    // ------------------------------------------------------------------
    /** Position of a page in the frames, or -1 if it is not loaded. */
    private static int indexOf(Integer[] slots, int page) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i] == page) {
                return i;
            }
        }
        return -1;
    }

    /** First empty (null) frame, or -1 if all frames are full. */
    private static int pickEmpty(Integer[] slots) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /** LRU victim = frame with the smallest last-use stamp. */
    private static int leastRecentlyUsed(Integer[] slots, int[] lastUse) {
        int victim = 0;
        for (int i = 1; i < slots.length; i++) {
            if (lastUse[i] < lastUse[victim]) {
                victim = i;
            }
        }
        return victim;
    }

    /**
     * Optimal victim = the frame whose page appears furthest in the future.
     * If a page never appears again, its distance is Integer.MAX_VALUE, so it
     * is chosen immediately.
     */
    private static int optimalVictim(Integer[] slots, int[] refs, int fromIndex) {
        int victim = 0;
        int farthest = -1; // how far ahead we found the next use
        for (int i = 0; i < slots.length; i++) {
            int next = Integer.MAX_VALUE; // assume "never used again"
            for (int k = fromIndex; k < refs.length; k++) {
                if (refs[k] == slots[i]) {
                    next = k;
                    break;
                }
            }
            if (next > farthest) {
                farthest = next;
                victim = i;
            }
        }
        return victim;
    }

    /**
     * Formats one table row, e.g.:
     * "     7 |     7     -     - | FAULT "
     * (column widths are kept equal so the table lines up).
     */
    private static String formatRow(int ref, Integer[] slots, String outcome) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %3d | ", ref));
        for (int i = 0; i < slots.length; i++) {
            sb.append(String.format("%5s ", slots[i] == null ? "-" : String.valueOf(slots[i])));
        }
        sb.append("| ").append(String.format("%-6s", outcome));
        return sb.toString();
    }

    private static void printTable(Result r, int frames) {
        System.out.println(r.algorithm + "  (frames = " + frames + ")");
        System.out.println("  Ref | " + repeat("frame ", frames) + "| Result");
        System.out.println("  --- | " + repeat("------ ", frames) + "|--------");
        for (String row : r.rows) {
            System.out.println(row);
        }
        System.out.println("  Total page faults: " + r.faults);
        System.out.println();
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
