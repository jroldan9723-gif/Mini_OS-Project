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
 *             requests, ALWAYS travel on to the disk boundary (even when the
 *     other side is empty), then reverse and service the rest.
 *   C-SCAN  : like SCAN, but at the boundary the head JUMPS to the other
 *             boundary and continues in the SAME direction (never reverses).
 *   LOOK    : like SCAN, but reverses at the LAST request of the current
 *             direction instead of travelling to the disk boundary.
 *   C-LOOK  : like C-SCAN, but jumps from the last request directly to the
 *             farthest pending request on the other side (never travels to
 *             the physical boundaries, never reverses).
 *
 * ASSUMPTIONS
 *   - Cylinders are numbered 0 .. maxCylinder.
 *   - SSTF ties (same distance) are broken by the LOWER cylinder number.
 *   - The circular jumps of C-SCAN (boundary -> boundary) and C-LOOK
 *     (request -> request) are COUNTED as head movement. Some books leave
 *     them out; the output says which we used.
 *   - A request that happens to equal the current head position costs 0.
 *   - When nothing is left to service, SCAN/C-SCAN stop at the disk
 *     boundary and LOOK/C-LOOK stop at the last request (no pointless
 *     extra travel).
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
            System.out.println("  5) LOOK   (SCAN without the trip to the boundary)");
            System.out.println("  6) C-LOOK (circular LOOK)");
            System.out.println("  7) Compare all six on the same requests");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 7);
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
            } else if (choice == 5) {
                printResult(look(p));
            } else if (choice == 6) {
                printResult(clook(p));
            } else {
                Result a = fcfs(p);
                Result b = sstf(p);
                Result c = scan(p);
                Result d = cscan(p);
                Result e = look(p);
                Result f = clook(p);
                printResult(a);
                printResult(b);
                printResult(c);
                printResult(d);
                printResult(e);
                printResult(f);
                System.out.println("SUMMARY (total head movement)");
                System.out.println("  FCFS  : " + a.movement + " cylinders");
                System.out.println("  SSTF  : " + b.movement + " cylinders");
                System.out.println("  SCAN  : " + c.movement + " cylinders");
                System.out.println("  C-SCAN: " + d.movement + " cylinders"
                        + "  (includes the return jump - some books exclude it)");
                System.out.println("  LOOK  : " + e.movement + " cylinders");
                System.out.println("  C-LOOK: " + f.movement + " cylinders"
                        + "  (includes the jump - some books exclude it)");
                Result[] all = {a, b, c, d, e, f};
                Result best = a;
                for (Result r : all) {
                    if (r.movement < best.movement) {
                        best = r;
                    }
                }
                System.out.println("  Smallest movement in this run: " + best.movement
                        + " cylinders (" + best.algorithm + ").");
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
                "Initial direction for SCAN / C-SCAN / LOOK / C-LOOK (1 = toward higher cylinders, 2 = toward lower): ",
                1, 2) == 1;
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
    // SCAN: move toward the chosen direction, ALWAYS continue to the disk
    // boundary, then reverse and service the other side.
    // ------------------------------------------------------------------
    private static Result scan(Problem p) {
        Result r = new Result("SCAN (elevator)");
        buildRoute(r, p, true, false);
        return r;
    }

    // ------------------------------------------------------------------
    // C-SCAN: like SCAN, but at the boundary the head WRAPS to the other
    // boundary and keeps going in the ORIGINAL direction.
    // ------------------------------------------------------------------
    private static Result cscan(Problem p) {
        Result r = new Result("C-SCAN (circular elevator)");
        buildRoute(r, p, true, true);
        return r;
    }

    // ------------------------------------------------------------------
    // LOOK: like SCAN, but reverses at the LAST request of the first leg
    // instead of travelling to the disk boundary.
    // ------------------------------------------------------------------
    private static Result look(Problem p) {
        Result r = new Result("LOOK");
        buildRoute(r, p, false, false);
        return r;
    }

    // ------------------------------------------------------------------
    // C-LOOK: like C-SCAN, but jumps from the last request directly to the
    // farthest pending request on the other side (no boundary travel).
    // ------------------------------------------------------------------
    private static Result clook(Problem p) {
        Result r = new Result("C-LOOK");
        buildRoute(r, p, false, true);
        return r;
    }

    /**
     * Shared engine for SCAN, C-SCAN, LOOK and C-LOOK.
     *
     * @param useBoundary true  = SCAN/C-SCAN: always travel on to the disk
     *                            boundary after the first leg
     *                    false = LOOK/C-LOOK: reverse / jump at the last
     *                            request of the first leg
     * @param circular    true  = C-SCAN/C-LOOK: keep the ORIGINAL direction
     *                            (wrap around instead of reversing)
     *                    false = SCAN/LOOK: reverse direction
     */
    private static void buildRoute(Result r, Problem p, boolean useBoundary, boolean circular) {
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
                // Request exactly at the head: serviced instantly, cost 0
                // (counted once only - it is not part of either leg).
                r.steps.add(req + " (at head)");
            }
        }
        Collections.sort(above); // ascending
        Collections.sort(below);
        Collections.reverse(below); // descending

        // "First leg": whichever direction the user chose. below is sorted
        // descending and above ascending, so each list is already in travel
        // order for the direction it is used in.
        List<Integer> firstLeg = p.towardHigh ? above : below;
        List<Integer> secondLeg = p.towardHigh ? below : above;
        if (circular) {
            // The circular algorithms keep travelling in the ORIGINAL
            // direction afterwards, so the far side must be read in that
            // direction too: ascending when heading up, descending when down.
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

        if (useBoundary) {
            // SCAN / C-SCAN: always continue to the disk boundary, even when
            // there is nothing left on the other side.
            if (cur != edge) {
                r.movement += Math.abs(cur - edge);
                cur = edge;
                r.steps.add(edge + " (end of disk)");
            }
            if (secondLeg.isEmpty()) {
                return; // nothing left to service: stop at the boundary
            }
            if (circular) {
                // Jump from one boundary straight to the other one.
                int otherEnd = p.towardHigh ? 0 : p.maxCylinder;
                r.movement += Math.abs(cur - otherEnd); // counted (see class comment)
                cur = otherEnd;
                r.steps.add(otherEnd + " (wrap)");
            }
        } else {
            // LOOK / C-LOOK: never travel to the physical boundaries.
            if (secondLeg.isEmpty()) {
                return; // everything on this side is done
            }
            if (circular) {
                // Jump from the last request of this leg directly to the
                // farthest pending request on the other side (the first
                // element of secondLeg, which is sorted in travel direction).
                // The landing is marked "(jump)" when it is serviced below,
                // so the request is only listed once.
                int target = secondLeg.get(0);
                r.movement += Math.abs(cur - target); // counted (see class comment)
                cur = target;
            }
        }

        // Service the remaining side. secondLeg is already ordered along the
        // direction of travel (reversed for SCAN/LOOK, same direction for
        // the circular variants).
        for (int i = 0; i < secondLeg.size(); i++) {
            int req = secondLeg.get(i);
            r.movement += Math.abs(cur - req);
            cur = req;
            if (circular && !useBoundary && i == 0) {
                r.steps.add(req + " (jump)"); // first request reached by a C-LOOK jump
            } else {
                r.steps.add(String.valueOf(req));
            }
        }
    }

    private static void printResult(Result r) {
        System.out.println(r.algorithm);
        System.out.println("  Order of service : " + r.path());
        System.out.println("  Total head movement: " + r.movement + " cylinders");
        System.out.println();
    }
}
