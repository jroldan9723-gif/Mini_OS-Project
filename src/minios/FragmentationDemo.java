package minios;

import java.util.ArrayList;
import java.util.List;

// Fragmentation, in two demos:
// - internal: fixed partitions, waste is the space left inside a partition
//   that the process cannot use;
// - external: variable memory, free space exists but is split into holes.
// First-Fit, Best-Fit and Worst-Fit can run separately or be compared on
// exactly the same data.
public class FragmentationDemo {

    // One allocation: a named block living at [start .. start + size).
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
            return start + size;
        }
    }

    // Menu: internal demo, external demo, or back.
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

    private static void internalDemo(InputHelper in) {
        System.out.println("INTERNAL FRAGMENTATION: space wasted INSIDE a partition.");
        int strategy = askStrategy(in);

        int nPartitions = in.readIntAtLeast("Number of fixed partitions: ", 1);
        int[] partitionSize = new int[nPartitions];
        for (int i = 0; i < nPartitions; i++) {
            partitionSize[i] = in.readIntInRange("  Size of partition " + (i + 1) + " (KB): ", 1, 100000);
        }

        int nProcs = in.readIntAtLeast("Number of processes: ", 1);
        String[] procName = new String[nProcs];
        int[] procSize = new int[nProcs];
        for (int i = 0; i < nProcs; i++) {
            procName[i] = "P" + (i + 1);
            procSize[i] = in.readIntInRange("  Size of " + procName[i] + " (KB): ", 1, 100000);
        }

        if (strategy == 4) {

            System.out.println();
            System.out.println("COMPARISON - same partitions and processes for every strategy");
            int[] totals = new int[4];
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

    // Places every process into a free partition that fits, following the
    // chosen strategy: 1 = first that fits, 2 = smallest that fits,
    // 3 = largest that fits. A process with no free partition stays -1
    // (it has to wait).
    private static int[] assignInternal(int[] partitionSize, int[] procSize, int strategy) {
        int[] assigned = new int[procSize.length];
        boolean[] used = new boolean[partitionSize.length];

        for (int i = 0; i < procSize.length; i++) {
            assigned[i] = -1;
            for (int p = 0; p < partitionSize.length; p++) {
                if (used[p] || partitionSize[p] < procSize[i]) {
                    continue;
                }
                if (assigned[i] == -1) {
                    assigned[i] = p;
                    if (strategy == 1) {
                        break;
                    }
                } else if (strategy == 2 && partitionSize[p] < partitionSize[assigned[i]]) {
                    assigned[i] = p;
                } else if (strategy == 3 && partitionSize[p] > partitionSize[assigned[i]]) {
                    assigned[i] = p;
                }

            }
            if (assigned[i] >= 0) {
                used[assigned[i]] = true;
            }
        }
        return assigned;
    }

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

    private static void externalDemo(InputHelper in) {
        System.out.println("EXTERNAL FRAGMENTATION: free space exists, but it is split into holes.");
        int strategy = askStrategy(in);

        int memSize = in.readIntInRange("Total memory (KB): ", 1, 1000000);

        int nAlloc = in.readIntAtLeast("How many jobs to allocate first? ", 1);
        String[] jobNames = new String[nAlloc];
        int[] jobSizes = new int[nAlloc];
        for (int i = 0; i < nAlloc; i++) {
            jobNames[i] = in.readLine("  Name of job " + (i + 1) + " (e.g. A): ").trim();
            jobSizes[i] = in.readIntInRange("  Size of " + jobNames[i] + " (KB): ", 1, memSize);
        }

        int nFree = in.readIntAtLeast("How many jobs to free? ", 0);
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

        List<Alloc> memory = new ArrayList<>();
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

    // Variable allocation: collect the current holes (gaps between blocks
    // plus the tail), pick one that fits according to the strategy, and
    // insert the new block at that address. Returns false if nothing fits.
    private static boolean allocate(List<Alloc> memory, String name, int size, int memSize,
                                    int strategy) {

        List<int[]> holes = new ArrayList<>();
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
                continue;
            }
            if (best == -1) {
                best = i;
                if (strategy == 1) {
                    break;
                }
            } else if (strategy == 2 && holes.get(i)[1] < holes.get(best)[1]) {
                best = i;
            } else if (strategy == 3 && holes.get(i)[1] > holes.get(best)[1]) {
                best = i;
            }

        }
        if (best == -1) {
            return false;
        }
        insertSorted(memory, new Alloc(name, holes.get(best)[0], size));
        return true;
    }

    // Walks the memory map and returns {total free KB, largest hole KB}.
    // totalFree >= request while largestHole < request is exactly the
    // situation called external fragmentation.
    private static int[] freeStats(List<Alloc> memory, int memSize) {
        int totalFree = 0;
        int largestHole = 0;
        int cursor = 0;
        for (Alloc a : memory) {
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

    private static String requestVerdict(int totalFree, int largestHole, int request) {
        if (largestHole >= request) {
            return "fits";
        }
        if (totalFree >= request) {
            return "NO FIT -> external fragmentation";
        }
        return "not enough memory";
    }

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
