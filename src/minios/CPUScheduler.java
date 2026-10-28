// File: src/minios/CPUScheduler.java
package minios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * CPUScheduler - simulates how a CPU decides which ready process runs next.
 *
 * ALGORITHMS IMPLEMENTED
 *   1. FCFS        First-Come, First-Served (non-preemptive, arrival order)
 *   2. SJF         Shortest Job First       (non-preemptive, shortest burst)
 *   3. Round Robin (RR)                     (preemptive, fixed time quantum)
 *
 * KEY FORMULAS (textbook definitions)
 *   Turnaround time = completion time - arrival time
 *   Waiting time    = turnaround time - burst time
 *   (Because turnaround = time in system = burst + waiting.)
 *
 * ASSUMPTIONS / SIMPLIFICATIONS
 *   - Every process arrives at time 0 (all jobs are ready from the start).
 *     This keeps the arithmetic easy to verify by hand.
 *   - SJF is the NON-preemptive version (once a job starts it runs to completion).
 *   - Burst times are positive whole numbers ("time units" of 1 ms, 1 tick, ...).
 *   - FCFS runs processes in the order they were entered.
 */
public class CPUScheduler {

    /** One simulated process - our tiny "process control block" (PCB). */
    static class Process {
        int id;    // 1, 2, 3, ... (displayed as P1, P2, P3, ...)
        int burst; // total CPU time the process needs

        Process(int id, int burst) {
            this.id = id;
            this.burst = burst;
        }

        String name() {
            return "P" + id;
        }
    }

    /** The outcome of one scheduling algorithm: times per process + order of execution. */
    static class Result {
        String algorithm;   // e.g. "FCFS"
        int[] waiting;      // waiting time, indexed like the input list
        int[] turnaround;   // turnaround time, indexed like the input list
        List<Integer> order; // execution order (process indices); RR: one entry per time slice

        Result(String algorithm, int n) {
            this.algorithm = algorithm;
            this.waiting = new int[n];
            this.turnaround = new int[n];
            this.order = new ArrayList<>();
        }

        double avgWaiting() {
            return average(waiting);
        }

        double avgTurnaround() {
            return average(turnaround);
        }

        private double average(int[] values) {
            int sum = 0;
            for (int v : values) {
                sum += v;
            }
            return values.length == 0 ? 0.0 : (double) sum / values.length;
        }
    }

    /** Entry point: shows the CPU-scheduling submenu until the user goes back. */
    public static void run(InputHelper in) {
        System.out.println("CPU SCHEDULING");
        System.out.println("Assumption: every process arrives at time 0.");
        while (true) {
            System.out.println();
            System.out.println("  1) FCFS        (First-Come, First-Served)");
            System.out.println("  2) SJF         (Shortest Job First, non-preemptive)");
            System.out.println("  3) Round Robin (preemptive, fixed time quantum)");
            System.out.println("  4) Compare all three on the same processes");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 4);
            if (choice == 0) {
                return;
            }
            System.out.println();

            List<Process> procs = readProcesses(in);

            if (choice == 1) {
                printResult(procs, fcfs(procs));
            } else if (choice == 2) {
                printResult(procs, sjf(procs));
            } else if (choice == 3) {
                int quantum = in.readIntInRange("Time quantum (whole time units): ", 1, 100);
                printResult(procs, roundRobin(procs, quantum));
                System.out.println("Note: in Round Robin the execution list shows one entry");
                System.out.println("      per time slice, so a process can appear several times.");
            } else {
                int quantum = in.readIntInRange("Time quantum for Round Robin (whole time units): ", 1, 100);
                Result r1 = fcfs(procs);
                Result r2 = sjf(procs);
                Result r3 = roundRobin(procs, quantum);
                printComparison(procs, r1, r2, r3);
            }
        }
    }

    /** Reads the number of processes and each burst time (each value is re-asked if invalid). */
    private static List<Process> readProcesses(InputHelper in) {
        int n = in.readIntInRange("How many processes? ", 1, 20);
        List<Process> procs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int burst = in.readIntInRange("  CPU burst time for P" + (i + 1) + ": ", 1, 1000);
            procs.add(new Process(i + 1, burst));
        }
        return procs;
    }

    // ------------------------------------------------------------------
    // FCFS: run processes in input (arrival) order, one after another.
    // ------------------------------------------------------------------
    static Result fcfs(List<Process> procs) {
        Result r = new Result("FCFS (First-Come, First-Served)", procs.size());
        int currentTime = 0; // when the CPU becomes free again
        for (int i = 0; i < procs.size(); i++) {
            Process p = procs.get(i);
            r.waiting[i] = currentTime;          // waited until the CPU was free
            r.turnaround[i] = currentTime + p.burst; // completion time (arrival = 0)
            currentTime += p.burst;              // CPU busy until this process finishes
            r.order.add(i);
        }
        return r;
    }

    // ------------------------------------------------------------------
    // SJF (non-preemptive): sort by burst, then behave like FCFS.
    // Shortest burst first => provably smallest average waiting time
    // when all processes arrive at the same time.
    // ------------------------------------------------------------------
    static Result sjf(List<Process> procs) {
        Result r = new Result("SJF (Shortest Job First, non-preemptive)", procs.size());

        // Indices sorted by burst time (ties broken by lower id, to stay deterministic).
        List<Integer> execOrder = new ArrayList<>();
        for (int i = 0; i < procs.size(); i++) {
            execOrder.add(i);
        }
        Collections.sort(execOrder, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                Process pa = procs.get(a);
                Process pb = procs.get(b);
                if (pa.burst != pb.burst) {
                    return pa.burst - pb.burst; // shorter job first
                }
                return pa.id - pb.id;          // tie: earlier P-number first
            }
        });

        int currentTime = 0;
        for (int idx : execOrder) {
            Process p = procs.get(idx);
            r.waiting[idx] = currentTime;
            r.turnaround[idx] = currentTime + p.burst;
            currentTime += p.burst;
            r.order.add(idx);
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Round Robin: everyone waits in a queue and gets at most "quantum"
    // time units at a time. If the process is not finished, it goes to the
    // back of the queue (this is what makes RR "fair").
    // ------------------------------------------------------------------
    static Result roundRobin(List<Process> procs, int quantum) {
        Result r = new Result("Round Robin (quantum = " + quantum + ")", procs.size());
        int n = procs.size();

        int[] remaining = new int[n]; // burst time still left for each process
        for (int i = 0; i < n; i++) {
            remaining[i] = procs.get(i).burst;
        }

        // All processes arrive at time 0, so the ready queue starts in input order.
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            queue.add(i);
        }

        int currentTime = 0;
        int finished = 0;
        while (finished < n) {
            int i = queue.poll();               // next process in line
            int slice = Math.min(quantum, remaining[i]); // run at most one quantum
            r.order.add(i);                     // record this time slice
            currentTime += slice;
            remaining[i] -= slice;

            if (remaining[i] == 0) {
                // Finished: turnaround = completion time (arrival = 0).
                r.turnaround[i] = currentTime;
                r.waiting[i] = currentTime - procs.get(i).burst;
                finished++;
            } else {
                queue.add(i); // not done yet -> back of the ready queue
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Pretty printing
    // ------------------------------------------------------------------
    private static void printResult(List<Process> procs, Result r) {
        System.out.println();
        System.out.println("Algorithm: " + r.algorithm);
        System.out.println("Execution order: " + names(procs, r.order));
        System.out.println();
        System.out.printf("%-8s %6s %8s %11s%n", "Process", "Burst", "Waiting", "Turnaround");
        System.out.println("-------- ------ -------- -----------");
        for (int i = 0; i < procs.size(); i++) {
            System.out.printf("%-8s %6d %8d %11d%n",
                    procs.get(i).name(), procs.get(i).burst, r.waiting[i], r.turnaround[i]);
        }
        System.out.printf("Average waiting time   : %.2f%n", r.avgWaiting());
        System.out.printf("Average turnaround time: %.2f%n", r.avgTurnaround());
    }

    private static void printComparison(List<Process> procs, Result fcfsR, Result sjfR, Result rrR) {
        System.out.println();
        System.out.println("COMPARISON OF THE THREE ALGORITHMS");
        System.out.println("(W = waiting time, T = turnaround time)");
        System.out.println();
        System.out.printf("%-8s %5s | %-7s | %-7s | %-7s%n",
                "Process", "Burst", "FCFS", "SJF", "RR");
        System.out.printf("%-8s %5s | %-7s | %-7s | %-7s%n",
                "", "", "W    T", "W    T", "W    T");
        System.out.println("-------- ----- | ------- | ------- | -------");
        for (int i = 0; i < procs.size(); i++) {
            System.out.printf("%-8s %5d | %3d %3d | %3d %3d | %3d %3d%n",
                    procs.get(i).name(), procs.get(i).burst,
                    fcfsR.waiting[i], fcfsR.turnaround[i],
                    sjfR.waiting[i], sjfR.turnaround[i],
                    rrR.waiting[i], rrR.turnaround[i]);
        }
        System.out.println("-------- ----- | ------- | ------- | -------");
        System.out.println("Averages (waiting / turnaround):");
        System.out.printf("  FCFS : %.2f / %.2f%n", fcfsR.avgWaiting(), fcfsR.avgTurnaround());
        System.out.printf("  SJF  : %.2f / %.2f%n", sjfR.avgWaiting(), sjfR.avgTurnaround());
        System.out.printf("  RR   : %.2f / %.2f%n", rrR.avgWaiting(), rrR.avgTurnaround());
    }

    /** Turns a list of process indices into "P1 -> P2 -> P3". */
    private static String names(List<Process> procs, List<Integer> order) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < order.size(); k++) {
            if (k > 0) {
                sb.append(" -> ");
            }
            sb.append(procs.get(order.get(k)).name());
        }
        return sb.toString();
    }
}
