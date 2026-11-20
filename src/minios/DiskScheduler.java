package minios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Disk scheduling: decides in which order the read/write head serves the
// pending cylinder requests. FCFS and SSTF follow the queue or the
// distances; SCAN, C-SCAN, LOOK and C-LOOK sweep the disk in one direction
// first (according to the chosen initial direction). The total head
// movement of every algorithm is reported so they can be compared.
public class DiskScheduler {

    // One algorithm's result: the service steps (annotated) and the number
    // of cylinders the head travelled in total.
    private static class Result {
        String algorithm;
        List<String> steps = new ArrayList<>();
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

    private static class Problem {
        int head;
        int[] requests;
        int maxCylinder;
        boolean towardHigh;
    }

    // Menu: one algorithm, or all six on the same queue plus a summary.
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

    // Reads the disk limits, the head position and the queue. A request
    // outside the disk makes the whole problem be entered again, so only a
    // consistent queue reaches the algorithms. The direction question only
    // matters for SCAN / C-SCAN / LOOK / C-LOOK.
    private static Problem readProblem(InputHelper in) {
        Problem p = new Problem();
        while (true) {
            p.maxCylinder = in.readIntInRange("Highest cylinder number (disk = 0..max): ", 1, 1_000_000);
            p.head = in.readIntInRange("Initial head position (0.." + p.maxCylinder + "): ", 0, p.maxCylinder);
            int n = in.readIntAtLeast("Number of pending requests: ", 1);
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

    // FCFS: serve the requests exactly in the order they arrived.
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

    // SSTF: always serve the unserved request closest to the current head
    // position (ties: the lower cylinder number).
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

    // SCAN: sweep toward the chosen direction, all the way to the disk
    // boundary, then turn around and serve the other side.
    private static Result scan(Problem p) {
        Result r = new Result("SCAN (elevator)");
        buildRoute(r, p, true, false);
        return r;
    }

    // C-SCAN: sweep one way to the boundary, jump back to the other end in
    // one big step, and continue in the same direction.
    private static Result cscan(Problem p) {
        Result r = new Result("C-SCAN (circular elevator)");
        buildRoute(r, p, true, true);
        return r;
    }

    // LOOK: like SCAN, but the head turns around at the last request
    // instead of travelling to the disk boundary.
    private static Result look(Problem p) {
        Result r = new Result("LOOK");
        buildRoute(r, p, false, false);
        return r;
    }

    // C-LOOK: like C-SCAN without the boundary trips; the jump goes from
    // the last served request straight to the farthest one on the other side.
    private static Result clook(Problem p) {
        Result r = new Result("C-LOOK");
        buildRoute(r, p, false, true);
        return r;
    }

    // Shared route builder for SCAN, C-SCAN, LOOK and C-LOOK:
    // 1. split the queue into requests above and below the head,
    // 2. serve the first leg in the chosen direction,
    // 3. optionally touch the boundary (SCAN / C-SCAN) or jump (circular
    //    variants), 4. serve the second leg.
    // Steps are annotated: "(at head)" costs nothing, "(end of disk)" is a
    // boundary trip, "(wrap)" and "(jump)" are the long jumps of the
    // circular algorithms - and every travelled cylinder is counted.
    private static void buildRoute(Result r, Problem p, boolean useBoundary, boolean circular) {
        r.steps.add(String.valueOf(p.head));
        int cur = p.head;

        List<Integer> above = new ArrayList<>();
        List<Integer> below = new ArrayList<>();
        for (int req : p.requests) {
            if (req > p.head) {
                above.add(req);
            } else if (req < p.head) {
                below.add(req);
            } else {

                r.steps.add(req + " (at head)");
            }
        }
        Collections.sort(above);
        Collections.sort(below);
        Collections.reverse(below);

        List<Integer> firstLeg = p.towardHigh ? above : below;
        List<Integer> secondLeg = p.towardHigh ? below : above;
        if (circular) {

            secondLeg = new ArrayList<>(secondLeg);
            Collections.sort(secondLeg);
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

            if (cur != edge) {
                r.movement += Math.abs(cur - edge);
                cur = edge;
                r.steps.add(edge + " (end of disk)");
            }
            if (secondLeg.isEmpty()) {
                return;
            }
            if (circular) {

                int otherEnd = p.towardHigh ? 0 : p.maxCylinder;
                r.movement += Math.abs(cur - otherEnd);
                cur = otherEnd;
                r.steps.add(otherEnd + " (wrap)");
            }
        } else {

            if (secondLeg.isEmpty()) {
                return;
            }
            if (circular) {

                int target = secondLeg.get(0);
                r.movement += Math.abs(cur - target);
                cur = target;
            }
        }

        for (int i = 0; i < secondLeg.size(); i++) {
            int req = secondLeg.get(i);
            r.movement += Math.abs(cur - req);
            cur = req;
            if (circular && !useBoundary && i == 0) {
                r.steps.add(req + " (jump)");
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
