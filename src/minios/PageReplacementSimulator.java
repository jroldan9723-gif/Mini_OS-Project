// File: src/minios/PageReplacementSimulator.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * PageReplacementSimulator - when a needed page is not in RAM, the OS must
 * evict ("replace") some page that IS in RAM. This class compares four
 * classic replacement policies on the same reference string:
 *
 *   FIFO      (First-In First-Out) : evict the page that entered memory first.
 *   LRU       (Least Recently Used) : evict the page that was used least recently.
 *   Optimal   (OPT / Belady's)      : evict the page that will be used furthest
 *                                     in the future (theoretical best - a real
 *                                     OS cannot know the future, it is used as
 *                                     a benchmark).
 *   LFU       (Least Frequently Used): evict the page with the lowest access
 *                                     frequency (ties: least recently used).
 *
 * After every reference we print the state of the frames and whether the
 * reference was a PAGE FAULT (had to load a page) or a HIT (page already
 * there - no replacement needed). LFU also shows the frequency of every
 * page in memory and which page was replaced.
 *
 * ASSUMPTIONS
 *   - Frames start empty.
 *   - FIFO advances its replacement pointer only when a fault occurs.
 *   - Optimal ties (equal future distance) pick the lower-numbered slot.
 *   - LFU resets a page's counter to 1 when the page is loaded (the access
 *     that caused the load counts), and breaks frequency ties by LRU.
 */
public class PageReplacementSimulator {

    /** One algorithm's result: a row per reference + the fault count. */
    private static class Result {
        String algorithm;
        List<String> rows = new ArrayList<>(); // pre-formatted lines for the table
        int faults = 0;
        boolean showFreqs = false; // LFU table has extra frequency columns

        Result(String algorithm) {
            this.algorithm = algorithm;
        }

        int hits(int totalRefs) {
            return totalRefs - faults;
        }

        double faultRate(int totalRefs) {
            return 100.0 * faults / totalRefs;
        }

        double hitRate(int totalRefs) {
            return 100.0 * hits(totalRefs) / totalRefs;
        }
    }

    public static void run(InputHelper in) {
        System.out.println("PAGE REPLACEMENT - FIFO, LRU, Optimal and LFU");
        while (true) {
            System.out.println();
            System.out.println("  1) Run all four algorithms on a reference string");
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
        Result lfu = runLfu(refs, frames);

        int total = refs.length;
        printTable(fifo, frames, total);
        printTable(lru, frames, total);
        printTable(opt, frames, total);
        printTable(lfu, frames, total);

        System.out.println("COMPARISON");
        System.out.println("  Algorithm   Faults   Hits   Fault rate   Hit rate");
        System.out.println("  ---------   ------   ----   ----------   ---------");
        printComparisonRow("FIFO", fifo, total);
        printComparisonRow("LRU", lru, total);
        printComparisonRow("Optimal", opt, total);
        printComparisonRow("LFU", lfu, total);

        // Which algorithm produced the fewest faults in THIS test?
        Result[] all = {fifo, lru, opt, lfu};
        String[] labels = {"FIFO", "LRU", "Optimal", "LFU"};
        int best = fifo.faults;
        for (Result r : all) {
            if (r.faults < best) {
                best = r.faults;
            }
        }
        StringBuilder winners = new StringBuilder();
        for (int i = 0; i < all.length; i++) {
            if (all[i].faults == best) {
                if (winners.length() > 0) {
                    winners.append(", ");
                }
                winners.append(labels[i]);
            }
        }
        System.out.println("  Fewest page faults: " + winners + " (" + best + ").");
        System.out.println();
    }

    private static void printComparisonRow(String label, Result r, int total) {
        System.out.printf("  %-9s %6d %6d %11.2f%% %10.2f%%%n",
                label, r.faults, r.hits(total), r.faultRate(total), r.hitRate(total));
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
    // LFU: replace the page with the lowest access frequency. The counter
    // is reset to 1 when a page is loaded (the faulting access counts) and
    // incremented on every hit. Frequency ties go to the least recently
    // used page, so the choice is always deterministic.
    // ------------------------------------------------------------------
    private static Result runLfu(int[] refs, int frames) {
        Result r = new Result("LFU");
        r.showFreqs = true;
        Integer[] slots = new Integer[frames];
        int[] freq = new int[frames];    // how often each loaded page was accessed
        int[] lastUse = new int[frames]; // for the LRU tie-break
        int clock = 1;

        for (int ref : refs) {
            int pos = indexOf(slots, ref);
            if (pos >= 0) {
                freq[pos]++;               // hit: frequency goes up
                lastUse[pos] = clock++;
                r.rows.add(formatLfuRow(ref, slots, freq, -1, "HIT"));
            } else {
                r.faults++;
                int victim = pickEmpty(slots);
                if (victim < 0) {
                    victim = lowestFrequency(slots, freq, lastUse);
                }
                int replaced = slots[victim] == null ? -1 : slots[victim]; // -1 = empty frame
                slots[victim] = ref;
                freq[victim] = 1;          // fresh counter for the newly loaded page
                lastUse[victim] = clock++;
                r.rows.add(formatLfuRow(ref, slots, freq, replaced, "FAULT"));
            }
        }
        return r;
    }

    /** LFU victim = lowest frequency; ties broken by least recently used. */
    private static int lowestFrequency(Integer[] slots, int[] freq, int[] lastUse) {
        int victim = 0;
        for (int i = 1; i < slots.length; i++) {
            if (freq[i] < freq[victim]
                    || (freq[i] == freq[victim] && lastUse[i] < lastUse[victim])) {
                victim = i;
            }
        }
        return victim;
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

    /**
     * LFU table row: reference, frames, frequency of every loaded page,
     * the page that was replaced (if any) and HIT / FAULT.
     * e.g. "    3 |     2     3     1 |   1   2   3 |        1 | FAULT  "
     */
    private static String formatLfuRow(int ref, Integer[] slots, int[] freq,
                                       int replaced, String outcome) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %3d | ", ref));
        for (int i = 0; i < slots.length; i++) {
            sb.append(String.format("%5s ", slots[i] == null ? "-" : String.valueOf(slots[i])));
        }
        sb.append("|");
        for (int i = 0; i < slots.length; i++) {
            sb.append(String.format("%4s", slots[i] == null ? "-" : String.valueOf(freq[i])));
        }
        sb.append(" | ").append(String.format("%8s", replaced < 0 ? "-" : String.valueOf(replaced)));
        sb.append(" | ").append(String.format("%-6s", outcome));
        return sb.toString();
    }

    private static void printTable(Result r, int frames, int totalRefs) {
        System.out.println(r.algorithm + "  (frames = " + frames + ")");
        if (r.showFreqs) {
            System.out.println("  Ref | " + repeat("frame ", frames) + "|"
                    + repeat("freq", frames) + " | replaced | Result");
            System.out.println("  --- | " + repeat("------ ", frames) + "|"
                    + repeat("----", frames) + " | -------- |--------");
        } else {
            System.out.println("  Ref | " + repeat("frame ", frames) + "| Result");
            System.out.println("  --- | " + repeat("------ ", frames) + "|--------");
        }
        for (String row : r.rows) {
            System.out.println(row);
        }
        System.out.println("  Total page faults: " + r.faults);
        System.out.println("  Total page hits  : " + r.hits(totalRefs));
        System.out.printf("  Page fault rate  : %.2f%%%n", r.faultRate(totalRefs));
        System.out.printf("  Page hit rate    : %.2f%%%n", r.hitRate(totalRefs));
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
