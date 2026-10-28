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
 * ASSUMPTIONS
 *   - Internal demo: fixed partitions, one process per partition, first-fit
 *     (each process goes into the first free partition large enough).
 *   - External demo: one contiguous memory block, variable-size allocation,
 *     first-fit; jobs keep contiguous space (no paging in this demo).
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
    // 1) INTERNAL FRAGMENTATION
    // ------------------------------------------------------------------
    private static void internalDemo(InputHelper in) {
        System.out.println("INTERNAL FRAGMENTATION: space wasted INSIDE a partition.");
        int nPartitions = in.readIntInRange("Number of fixed partitions: ", 1, 10);
        int[] partitionSize = new int[nPartitions];
        boolean[] used = new boolean[nPartitions];
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

        // First-fit: each process takes the FIRST free partition big enough.
        String[] placedIn = new String[nProcs];      // partition label or null
        int[] placedSize = new int[nProcs];
        for (int i = 0; i < nProcs; i++) {
            for (int p = 0; p < nPartitions; p++) {
                if (!used[p] && partitionSize[p] >= procSize[i]) {
                    used[p] = true;
                    placedIn[i] = "partition " + (p + 1);
                    placedSize[i] = p;
                    break;
                }
            }
        }

        System.out.println();
        System.out.println("Allocation result (first-fit):");
        System.out.println("  Partition | Size | Process | Wasted (internal frag)");
        System.out.println("  ----------|------|---------|----------------------");
        int totalWasted = 0;
        for (int i = 0; i < nProcs; i++) {
            if (placedIn[i] == null) {
                System.out.printf("  %-9s | %4s | %-7s | %s%n", "(any)", "-", procName[i],
                        "did not fit in any free partition (waits)");
            } else {
                int wasted = partitionSize[placedSize[i]] - procSize[i];
                totalWasted += wasted;
                System.out.printf("  %-9s | %4d | %-7s | %4d KB%n",
                        "partition " + (placedSize[i] + 1), partitionSize[placedSize[i]],
                        procName[i], wasted);
            }
        }
        // Partitions that no process entered are not "fragmentation", they are
        // simply unused - but note them so the picture is complete.
        for (int p = 0; p < nPartitions; p++) {
            if (!used[p]) {
                System.out.printf("  %-9s | %4d | %-7s | %s%n",
                        "partition " + (p + 1), partitionSize[p], "-", "empty (no process)");
            }
        }
        System.out.println();
        System.out.println("Total internal fragmentation (wasted inside partitions): "
                + totalWasted + " KB");
    }

    // ------------------------------------------------------------------
    // 2) EXTERNAL FRAGMENTATION
    // ------------------------------------------------------------------
    private static void externalDemo(InputHelper in) {
        System.out.println("EXTERNAL FRAGMENTATION: free space exists, but it is split into holes.");
        int memSize = in.readIntInRange("Total memory (KB): ", 1, 1000000);
        List<Alloc> memory = new ArrayList<>(); // always kept sorted by start address

        int nAlloc = in.readIntInRange("How many jobs to allocate first? ", 1, 20);
        for (int i = 0; i < nAlloc; i++) {
            String name = in.readLine("  Name of job " + (i + 1) + " (e.g. A): ").trim();
            int size = in.readIntInRange("  Size of " + name + " (KB): ", 1, memSize);
            if (!allocate(memory, name, size, memSize)) {
                System.out.println("  [!] " + name + " (" + size + " KB) does not fit anywhere; skipped.");
            } else {
                System.out.println("  " + name + " allocated.");
                printMemoryMap(memory, memSize);
            }
        }

        int nFree = in.readIntInRange("How many jobs to free? ", 0, 20);
        for (int i = 0; i < nFree; i++) {
            String name = in.readLine("  Name of the job to free: ").trim();
            if (free(memory, name)) {
                System.out.println("  " + name + " freed.");
                printMemoryMap(memory, memSize);
            } else {
                System.out.println("  [!] No job named " + name + " is currently allocated.");
            }
        }

        // A new request: this is where external fragmentation shows up.
        int request = in.readIntInRange("New request size (KB): ", 1, memSize);
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

    /** First-fit allocation into the single contiguous memory block. Returns false if no hole fits. */
    private static boolean allocate(List<Alloc> memory, String name, int size, int memSize) {
        int cursor = 0;
        for (Alloc a : memory) {
            if (a.start - cursor >= size) { // hole between previous block and this one
                insertSorted(memory, new Alloc(name, cursor, size));
                return true;
            }
            cursor = a.end();
        }
        if (memSize - cursor >= size) { // hole at the end of memory
            insertSorted(memory, new Alloc(name, cursor, size));
            return true;
        }
        return false;
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
