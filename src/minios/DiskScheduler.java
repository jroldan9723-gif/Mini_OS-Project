// File: src/minios/DiskScheduler.java
package minios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DiskScheduler - simulates the order in which a disk head serves cylinder
 * requests, and counts how far the head physically travels.
 *
 * ALGORITHMS
 *   FCFS    : serve requests in exactly the order they arrived.
 *   SSTF    : always serve the UNVISITED request closest to the current head.
 *   SCAN    : the "elevator" - keep moving in one direction servicing
 *             requests, go to the end of the disk, then reverse.
 *   C-SCAN  : like SCAN, but when reaching the end the head JUMPS back to the
 *             other end and continues in the SAME direction (so wait times
 *             are more even).
 *
 * ASSUMPTIONS
 *   - Cylinders are numbered 0 .. maxCylinder.
 *   - SSTF ties (same distance) are broken by the LOWER cylinder number.
 *   - SCAN travels to the disk edge only when there are still requests on the
 *     other side to fetch (classic SCAN otherwise behaves like LOOK here).
 *   - For C-SCAN the return jump (end -> other end) is counted as head
 *     movement. Some books leave it out; the output says which we used.
 *   - A request that happens to equal the current head position costs 0.
 */
public class DiskScheduler {

    /** Result of one algorithm: display path + total cylinders travelled. */
    private static class Result {
        String algorithm;
        List<String> steps = new ArrayList<>(); // path entries, some annotated
        int movement = 0;

        Result(String algorithm) {
            this.algorithm = algorithm;
        }

        String path() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < steps.size(); i++) {
                if (i > 0) {
                    sb.append(" -> ");
                }
                sb.append(steps.get(i));
            }
            return sb.toString();
        }
    }

    /** All input gathered for one problem instance. */
    private static class Problem {
        int head;
        int[] requests;
        int maxCylinder;
        boolean towardHigh; // true = SCAN/C-SCAN move toward higher cylinders first
    }

    public static void run(InputHelper in) {
        System.out.println("DISK SCHEDULING");
        while (true) {
            System.out.println();
            System.out.println("  1) FCFS");
            System.out.println("  2) SSTF   (Shortest Seek Time First)");
            System.out.println("  3) SCAN   (elevator)");
            System.out.println("  4) C-SCAN (circular elevator)");
            System.out.println("  5) Compare all four on the same requests");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 5);
            if (choice == 0) {
                return;
            }
            System.out.println();
            Problem p = readProblem(in);

            if (choice == 1) {
                printResult(fcfs(p));
            } else if (choice == 2) {
                printResult(sstf(p));
            } else if (choice == 3) {
                printResult(scan(p));
            } else if (choice == 4) {
                printResult(cscan(p));
            } else {
                Result a = fcfs(p);
                Result b = sstf(p);
                Result c = scan(p);
                Result d = cscan(p);
                printResult(a);
                printResult(b);
                printResult(c);
                printResult(d);
                System.out.println("SUMMARY (total head movement)");
                System.out.println("  FCFS  : " + a.movement + " cylinders");
                System.out.println("  SSTF  : " + b.movement + " cylinders");
                System.out.println("  SCAN  : " + c.movement + " cylinders");
                System.out.println("  C-SCAN: " + d.movement + " cylinders"
                        + "  (includes the return jump - some books exclude it)");
                int best = Math.min(Math.min(a.movement, b.movement), Math.min(c.movement, d.movement));
                System.out.println("  Smallest movement in this run: " + best + " cylinders.");
            }
        }
    }

    /** Reads one problem instance, re-asking if any cylinder number is out of range. */
    private static Problem readProblem(InputHelper in) {
        Problem p = new Problem();
        while (true) {
            p.maxCylinder = in.readIntInRange("Highest cylinder number (disk = 0..max): ", 1, 1_000_000);
            p.head = in.readIntInRange("Initial head position (0.." + p.maxCylinder + "): ", 0, p.maxCylinder);
            int n = in.readIntInRange("Number of pending requests: ", 1, 50);
            p.requests = in.readIntArray("Request queue (" + n + " cylinder number(s), space separated): ", n);

            boolean ok = true;
            for (int r : p.requests) {
                if (r < 0 || r > p.maxCylinder) {
                    System.out.println("[!] Request " + r + " is outside the disk (0.."
                            + p.maxCylinder + "). Please re-enter the data.");
                    ok = false;
                    break;
                }
            }
            if (ok) {
                break;
            }
        }
        p.towardHigh = in.readIntInRange(
                "Initial direction for SCAN / C-SCAN (1 = toward higher cylinders, 2 = toward lower): ", 1, 2) == 1;
        return p;
    }

    // ------------------------------------------------------------------
    // FCFS: exactly the arrival order of the queue.
    // ------------------------------------------------------------------
    private static Result fcfs(Problem p) {
        Result r = new Result("FCFS (First-Come, First-Served)");
        r.steps.add(String.valueOf(p.head));
        int cur = p.head;
        for (int req : p.requests) {
            r.movement += Math.abs(cur - req);
            cur = req;
            r.steps.add(String.valueOf(req));
        }
        return r;
    }

    // ------------------------------------------------------------------
    // SSTF: repeatedly jump to the nearest request that was not served yet.
    // ------------------------------------------------------------------
    private static Result sstf(Problem p) {
        Result r = new Result("SSTF (Shortest Seek Time First)");
        r.steps.add(String.valueOf(p.head));
        boolean[] served = new boolean[p.requests.length];
        int cur = p.head;

        for (int k = 0; k < p.requests.length; k++) {
            int best = -1;
            int bestDist = Integer.MAX_VALUE;
            for (int i = 0; i < p.requests.length; i++) {
                if (served[i]) {
                    continue;
                }
                int dist = Math.abs(cur - p.requests[i]);
                if (dist < bestDist || (dist == bestDist && p.requests[i] < p.requests[best])) {
                    best = i;
                    bestDist = dist;
                }
            }
            served[best] = true;
            r.movement += bestDist;
            cur = p.requests[best];
            r.steps.add(String.valueOf(cur));
        }
        return r;
    }

    // ------------------------------------------------------------------
    // SCAN: move toward the chosen direction, service everything there,
    // reach the disk edge, then reverse and service the other side.
    // ------------------------------------------------------------------
    private static Result scan(Problem p) {
        Result r = new Result("SCAN (elevator)");
        buildScanLike(r, p, false);
        return r;
    }

    // ------------------------------------------------------------------
    // C-SCAN: same as SCAN, but at the edge the head WRAPS to the other end
    // and keeps going in the ORIGINAL direction.
    // ------------------------------------------------------------------
    private static Result cscan(Problem p) {
        Result r = new Result("C-SCAN (circular elevator)");
        buildScanLike(r, p, true);
        return r;
    }

    /**
     * Shared engine for SCAN and C-SCAN.
     *
     * @param circular true = C-SCAN (wrap around instead of reversing)
     */
    private static void buildScanLike(Result r, Problem p, boolean circular) {
        r.steps.add(String.valueOf(p.head));
        int cur = p.head;

        // Split the queue around the current head position.
        List<Integer> above = new ArrayList<>(); // cylinders > head
        List<Integer> below = new ArrayList<>(); // cylinders < head
        for (int req : p.requests) {
            if (req > p.head) {
                above.add(req);
            } else if (req < p.head) {
                below.add(req);
            } else {
                // Request exactly at the head: serviced instantly, cost 0.
                r.steps.add(String.valueOf(req));
            }
        }
        Collections.sort(above); // ascending
        Collections.sort(below);
        Collections.reverse(below); // descending

        // "First leg": whichever direction the user chose.
        //   below is sorted descending, above ascending, so each list is
        //   already in travel order for the direction it is used in.
        List<Integer> firstLeg = p.towardHigh ? above : below;
        List<Integer> secondLeg = p.towardHigh ? below : above;
        if (circular) {
            // C-SCAN keeps travelling in the ORIGINAL direction after the
            // wrap, so the far side must be read in that direction too:
            // ascending when heading up, descending when heading down.
            secondLeg = new ArrayList<>(secondLeg);
            Collections.sort(secondLeg); // ascending
            if (!p.towardHigh) {
                Collections.reverse(secondLeg);
            }
        }
        int edge = p.towardHigh ? p.maxCylinder : 0;

        for (int req : firstLeg) {
            r.movement += Math.abs(cur - req);
            cur = req;
            r.steps.add(String.valueOf(req));
        }

        if (secondLeg.isEmpty()) {
            return; // nothing on the other side: no reason to travel to the edge
        }

        // Reach the edge of the disk before turning around / wrapping.
        r.movement += Math.abs(cur - edge);
        cur = edge;
        r.steps.add(edge + " (end of disk)");

        if (circular) {
            // Jump from one edge straight to the other edge.
            int otherEnd = p.towardHigh ? 0 : p.maxCylinder;
            r.movement += Math.abs(cur - otherEnd); // <-- counted as movement (see class comment)
            cur = otherEnd;
            r.steps.add(otherEnd + " (wrap)");
        }

        // Service the remaining side. secondLeg is already ordered along the
        // direction of travel: reversed for SCAN, same-direction for C-SCAN.
        for (int req : secondLeg) {
            r.movement += Math.abs(cur - req);
            cur = req;
            r.steps.add(String.valueOf(req));
        }
    }

    private static void printResult(Result r) {
        System.out.println(r.algorithm);
        System.out.println("  Order of service : " + r.path());
        System.out.println("  Total head movement: " + r.movement + " cylinders");
        System.out.println();
    }
}
