// File: src/minios/FragmentationDemo.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentationDemo - explains and demonstrates the two kinds of fragmentation.
 *
 * INTERNAL fragmentation = memory wasted INSIDE an allocated block.
 *   Classic example: fixed-size partitions. A 80 KB process placed in a
 *   100 KB partition wastes 20 KB - that 20 KB cannot be used by anybody.
 *
 * EXTERNAL fragmentation = free memory exists, but it is scattered in HOLES
 *   that are each too small for the new request.
 *   Classic example: allocate A, B, C side by side; free B; now there are two
 *   holes. A new request that fits in the TOTAL free space but not in any
 *   SINGLE hole has to wait (or the OS must compact memory).
 *
 * ALLOCATION STRATEGIES (both demos support all three)
 *   First-Fit : take the first partition / hole large enough.
 *   Best-Fit  : take the smallest partition / hole that fits.
 *   Worst-Fit : take the largest partition / hole that fits.
 *   The demos also offer a comparison mode that runs all three on exactly
 *   the same data (each run works on its own copy of the memory layout).
 *
 * ASSUMPTIONS
 *   - Internal demo: fixed partitions, one process per partition.
 *   - External demo: one contiguous memory block, variable-size allocation;
 *     jobs keep contiguous space (no paging in this demo).
 *   - Equal-size candidates are broken by the lowest partition / earliest
 *     hole, so the result is always deterministic.
 */
public class FragmentationDemo {

    /** One allocated chunk in the external-fragmentation demo. */
    private static class Alloc {
        String name;
        int start;
        int size;

        Alloc(String name, int start, int size) {
            this.name = name;
            this.start = start;
            this.size = size;
        }

        int end() {
            return start + size; // one past the last byte, so ranges do not overlap
        }
    }

    public static void run(InputHelper in) {
        System.out.println("FRAGMENTATION - internal vs external");
        while (true) {
            System.out.println();
            System.out.println("  1) Internal fragmentation (fixed partitions)");
            System.out.println("  2) External fragmentation (holes in variable memory)");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 2);
            if (choice == 0) {
                return;
            }
            System.out.println();
            if (choice == 1) {
                internalDemo(in);
            } else {
                externalDemo(in);
            }
        }
    }

    // ------------------------------------------------------------------
    // Strategy selection (shared by both demos)
    // ------------------------------------------------------------------
    private static int askStrategy(InputHelper in) {
        System.out.println("Allocation strategy:");
        System.out.println("  1) First-Fit  (first partition/hole that fits)");
        System.out.println("  2) Best-Fit   (smallest one that fits)");
        System.out.println("  3) Worst-Fit  (largest one that fits)");
        System.out.println("  4) Compare all three on the same data");
        return in.readIntInRange("  Choose a strategy: ", 1, 4);
    }

    private static String strategyName(int strategy) {
        if (strategy == 2) {
            return "best-fit";
        }
        if (strategy == 3) {
            return "worst-fit";
        }
        return "first-fit";
    }

    // ------------------------------------------------------------------
    // 1) INTERNAL FRAGMENTATION
    // ------------------------------------------------------------------
    private static void internalDemo(InputHelper in) {
        System.out.println("INTERNAL FRAGMENTATION: space wasted INSIDE a partition.");
        int strategy = askStrategy(in);

        int nPartitions = in.readIntInRange("Number of fixed partitions: ", 1, 10);
        int[] partitionSize = new int[nPartitions];
        for (int i = 0; i < nPartitions; i++) {
            partitionSize[i] = in.readIntInRange("  Size of partition " + (i + 1) + " (KB): ", 1, 100000);
        }

        int nProcs = in.readIntInRange("Number of processes: ", 1, 20);
        String[] procName = new String[nProcs];
        int[] procSize = new int[nProcs];
        for (int i = 0; i < nProcs; i++) {
            procName[i] = "P" + (i + 1);
            procSize[i] = in.readIntInRange("  Size of " + procName[i] + " (KB): ", 1, 100000);
        }

        if (strategy == 4) {
            // Compare: same data, three separate runs (assignInternal only
            // reads the arrays, so every run starts fresh).
            System.out.println();
            System.out.println("COMPARISON - same partitions and processes for every strategy");
            int[] totals = new int[4]; // indexed 1..3
            for (int s = 1; s <= 3; s++) {
                System.out.println();
                totals[s] = printInternalResult(procName, partitionSize, procSize,
                        assignInternal(partitionSize, procSize, s), s);
            }
            System.out.println("Summary (total internal fragmentation):");
            for (int s = 1; s <= 3; s++) {
                System.out.println("  " + strategyName(s) + ": " + totals[s] + " KB");
            }
        } else {
            System.out.println();
            printInternalResult(procName, partitionSize, procSize,
                    assignInternal(partitionSize, procSize, strategy), strategy);
        }
    }

    /**
     * Places every process into a partition using the chosen strategy.
     * Returns, for each process, the index of its partition (-1 = no free
     * partition fits, the process has to wait). One process per partition.
     */
    private static int[] assignInternal(int[] partitionSize, int[] procSize, int strategy) {
        int[] assigned = new int[procSize.length];
        boolean[] used = new boolean[partitionSize.length];

        for (int i = 0; i < procSize.length; i++) {
            assigned[i] = -1;
            for (int p = 0; p < partitionSize.length; p++) {
                if (used[p] || partitionSize[p] < procSize[i]) {
                    continue; // taken, or too small
                }
                if (assigned[i] == -1) {
                    assigned[i] = p;
                    if (strategy == 1) {
                        break; // First-Fit: the first one that fits wins
                    }
                } else if (strategy == 2 && partitionSize[p] < partitionSize[assigned[i]]) {
                    assigned[i] = p; // Best-Fit: keep looking for a smaller one
                } else if (strategy == 3 && partitionSize[p] > partitionSize[assigned[i]]) {
                    assigned[i] = p; // Worst-Fit: keep looking for a larger one
                }
                // equal sizes keep the earlier (lower) partition number
            }
            if (assigned[i] >= 0) {
                used[assigned[i]] = true;
            }
        }
        return assigned;
    }

    /** Prints the allocation table for one strategy and returns the total waste. */
    private static int printInternalResult(String[] procName, int[] partitionSize, int[] procSize,
                                           int[] assigned, int strategy) {
        System.out.println("Allocation result (" + strategyName(strategy) + "):");
        System.out.println("  Partition | Size | Process | Wasted (internal frag)");
        System.out.println("  ----------|------|---------|----------------------");
        boolean[] used = new boolean[partitionSize.length];
        int totalWasted = 0;
        for (int i = 0; i < procSize.length; i++) {
            if (assigned[i] < 0) {
                System.out.printf("  %-9s | %4s | %-7s | %s%n", "(any)", "-", procName[i],
                        "did not fit in any free partition (waits)");
            } else {
                used[assigned[i]] = true;
                int wasted = partitionSize[assigned[i]] - procSize[i];
                totalWasted += wasted;
                System.out.printf("  %-9s | %4d | %-7s | %4d KB%n",
                        "partition " + (assigned[i] + 1), partitionSize[assigned[i]],
                        procName[i], wasted);
            }
        }
        // Partitions that no process entered are not "fragmentation", they are
        // simply unused - but note them so the picture is complete.
        for (int p = 0; p < partitionSize.length; p++) {
            if (!used[p]) {
                System.out.printf("  %-9s | %4d | %-7s | %s%n",
                        "partition " + (p + 1), partitionSize[p], "-", "empty (no process)");
            }
        }
        System.out.println();
        System.out.println("Total internal fragmentation (wasted inside partitions): "
                + totalWasted + " KB");
        return totalWasted;
    }

    // ------------------------------------------------------------------
    // 2) EXTERNAL FRAGMENTATION
    // ------------------------------------------------------------------
    private static void externalDemo(InputHelper in) {
        System.out.println("EXTERNAL FRAGMENTATION: free space exists, but it is split into holes.");
        int strategy = askStrategy(in);

        int memSize = in.readIntInRange("Total memory (KB): ", 1, 1000000);

        // Read the whole scenario first, so the comparison mode can replay
        // exactly the same data for each of the three strategies.
        int nAlloc = in.readIntInRange("How many jobs to allocate first? ", 1, 20);
        String[] jobNames = new String[nAlloc];
        int[] jobSizes = new int[nAlloc];
        for (int i = 0; i < nAlloc; i++) {
            jobNames[i] = in.readLine("  Name of job " + (i + 1) + " (e.g. A): ").trim();
            jobSizes[i] = in.readIntInRange("  Size of " + jobNames[i] + " (KB): ", 1, memSize);
        }

        int nFree = in.readIntInRange("How many jobs to free? ", 0, 20);
        String[] freeNames = new String[nFree];
        for (int i = 0; i < nFree; i++) {
            freeNames[i] = in.readLine("  Name of the job to free: ").trim();
        }

        int request = in.readIntInRange("New request size (KB): ", 1, memSize);

        if (strategy == 4) {
            System.out.println();
            System.out.println("COMPARISON - same jobs, frees and request for every strategy");
            for (int s = 1; s <= 3; s++) {
                List<Alloc> memory = buildScenario(memSize, jobNames, jobSizes, freeNames, s);
                int[] stats = freeStats(memory, memSize);
                System.out.println();
                System.out.println("--- " + strategyName(s) + " ---");
                printMemoryMap(memory, memSize);
                System.out.println("  Total free: " + stats[0] + " KB, largest hole: "
                        + stats[1] + " KB, request of " + request + " KB: "
                        + requestVerdict(stats[0], stats[1], request));
            }
            return;
        }

        // Single strategy: play the scenario step by step (like before).
        List<Alloc> memory = new ArrayList<>(); // always kept sorted by start address
        for (int i = 0; i < nAlloc; i++) {
            if (!allocate(memory, jobNames[i], jobSizes[i], memSize, strategy)) {
                System.out.println("  [!] " + jobNames[i] + " (" + jobSizes[i]
                        + " KB) does not fit anywhere; skipped.");
            } else {
                System.out.println("  " + jobNames[i] + " allocated.");
                printMemoryMap(memory, memSize);
            }
        }
        for (String name : freeNames) {
            if (free(memory, name)) {
                System.out.println("  " + name + " freed.");
                printMemoryMap(memory, memSize);
            } else {
                System.out.println("  [!] No job named " + name + " is currently allocated.");
            }
        }

        // A new request: this is where external fragmentation shows up.
        int[] stats = freeStats(memory, memSize);
        int totalFree = stats[0];
        int largestHole = stats[1];

        System.out.println();
        System.out.println("Request of " + request + " KB:");
        System.out.println("  Total free memory : " + totalFree + " KB");
        System.out.println("  Largest single hole: " + largestHole + " KB");

        if (largestHole >= request) {
            System.out.println("  -> The request FITS (largest hole >= request). No external");
            System.out.println("     fragmentation problem in this case.");
        } else if (totalFree >= request) {
            System.out.println("  -> EXTERNAL FRAGMENTATION! There is enough free memory in total");
            System.out.println("     (" + totalFree + " KB >= " + request + " KB) but no single hole is large");
            System.out.println("     enough (largest hole = " + largestHole + " KB). The job must wait, or the");
            System.out.println("     OS has to compact memory / use paging.");
        } else {
            System.out.println("  -> Simply NOT ENOUGH MEMORY: total free (" + totalFree
                    + " KB) is smaller than the request (" + request + " KB).");
        }
    }

    /** Replays the whole scenario for one strategy; used by the comparison mode. */
    private static List<Alloc> buildScenario(int memSize, String[] jobNames, int[] jobSizes,
                                             String[] freeNames, int strategy) {
        List<Alloc> memory = new ArrayList<>();
        for (int i = 0; i < jobNames.length; i++) {
            allocate(memory, jobNames[i], jobSizes[i], memSize, strategy);
        }
        for (String name : freeNames) {
            free(memory, name);
        }
        return memory;
    }

    /**
     * Picks a hole for the job with the chosen strategy and inserts it.
     * Returns false if no hole is large enough (the job cannot be placed).
     */
    private static boolean allocate(List<Alloc> memory, String name, int size, int memSize,
                                    int strategy) {
        // Collect every hole: gaps between blocks and the tail of memory.
        List<int[]> holes = new ArrayList<>(); // each entry: {start, size}
        int cursor = 0;
        for (Alloc a : memory) {
            if (a.start - cursor > 0) {
                holes.add(new int[] {cursor, a.start - cursor});
            }
            cursor = a.end();
        }
        if (memSize - cursor > 0) {
            holes.add(new int[] {cursor, memSize - cursor});
        }

        int best = -1;
        for (int i = 0; i < holes.size(); i++) {
            if (holes.get(i)[1] < size) {
                continue; // too small
            }
            if (best == -1) {
                best = i;
                if (strategy == 1) {
                    break; // First-Fit: the first hole in address order wins
                }
            } else if (strategy == 2 && holes.get(i)[1] < holes.get(best)[1]) {
                best = i; // Best-Fit: keep the smallest fitting hole
            } else if (strategy == 3 && holes.get(i)[1] > holes.get(best)[1]) {
                best = i; // Worst-Fit: keep the largest fitting hole
            }
            // equal sizes keep the earlier hole (lower address)
        }
        if (best == -1) {
            return false;
        }
        insertSorted(memory, new Alloc(name, holes.get(best)[0], size));
        return true;
    }

    /** @return {total free memory, largest single hole} in KB. */
    private static int[] freeStats(List<Alloc> memory, int memSize) {
        int totalFree = 0;
        int largestHole = 0;
        int cursor = 0;
        for (Alloc a : memory) { // gaps before, between and after allocations are holes
            int hole = a.start - cursor;
            totalFree += hole;
            largestHole = Math.max(largestHole, hole);
            cursor = a.end();
        }
        int tail = memSize - cursor;
        totalFree += tail;
        largestHole = Math.max(largestHole, tail);
        return new int[] {totalFree, largestHole};
    }

    /** Short verdict used by the comparison mode. */
    private static String requestVerdict(int totalFree, int largestHole, int request) {
        if (largestHole >= request) {
            return "fits";
        }
        if (totalFree >= request) {
            return "NO FIT -> external fragmentation";
        }
        return "not enough memory";
    }

    /** Removes the allocation with this name (creates a hole). Returns false if not found. */
    private static boolean free(List<Alloc> memory, String name) {
        for (int i = 0; i < memory.size(); i++) {
            if (memory.get(i).name.equalsIgnoreCase(name)) {
                memory.remove(i);
                return true;
            }
        }
        return false;
    }

    private static void insertSorted(List<Alloc> memory, Alloc a) {
        int pos = 0;
        while (pos < memory.size() && memory.get(pos).start < a.start) {
            pos++;
        }
        memory.add(pos, a);
    }

    /** Prints e.g. [A 0-400] [HOLE 400-500] [C 500-800] [HOLE 800-1000]. */
    private static void printMemoryMap(List<Alloc> memory, int memSize) {
        StringBuilder sb = new StringBuilder("  Memory map: ");
        int cursor = 0;
        for (Alloc a : memory) {
            if (a.start > cursor) {
                sb.append("[HOLE ").append(cursor).append('-').append(a.start).append("] ");
            }
            sb.append('[').append(a.name).append(' ').append(a.start).append('-').append(a.end()).append("] ");
            cursor = a.end();
        }
        if (cursor < memSize) {
            sb.append("[HOLE ").append(cursor).append('-').append(memSize).append(']');
        }
        System.out.println(sb.toString());
    }
}
