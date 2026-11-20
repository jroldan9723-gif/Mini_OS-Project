package minios;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// CPU scheduling: FCFS, SJF, Round Robin, a four-way comparison, SRTF and
// non-preemptive Priority. Every algorithm takes an arrival time per
// process; when nothing has arrived yet the CPU shows an idle gap.
public class CPUScheduler {

    // One process: a display name (typed by the user or P1, P2, ...), the
    // arrival time, the burst time and (for the priority algorithms) the
    // priority number. id keeps the input order for the tie-breaks.
    static class Process {
        int id;
        String name;
        int burst;
        int arrival;
        int priority;

        Process(int id, String name, int burst) {
            this.id = id;
            this.name = name;
            this.burst = burst;
        }

        String name() {
            return name;
        }
    }

    // The full report of one run: per-process times, the order in which the
    // processes were served and the Gantt segments (who ran from when to
    // when, with -1 marking an idle CPU).
    static class Result {
        String algorithm;
        int n;
        int[] start;
        int[] completion;
        int[] turnaround;
        int[] waiting;
        int[] response;
        List<Integer> order = new ArrayList<>();
        List<int[]> segments = new ArrayList<>();

        Result(String algorithm, int n) {
            this.algorithm = algorithm;
            this.n = n;
            start = new int[n];
            completion = new int[n];
            turnaround = new int[n];
            waiting = new int[n];
            response = new int[n];
            for (int i = 0; i < n; i++) {
                start[i] = -1;
            }
        }

        // Adds a Gantt segment, merging it with the previous one when the
        // same process (or the idle CPU) simply keeps going.
        void addSegment(int who, int from, int to) {
            if (to <= from) {
                return;
            }
            if (!segments.isEmpty()) {
                int[] last = segments.get(segments.size() - 1);
                if (last[0] == who && last[2] == from) {
                    last[2] = to;
                    return;
                }
            }
            segments.add(new int[] {who, from, to});
        }

        double avgWaiting() {
            return average(waiting);
        }

        double avgTurnaround() {
            return average(turnaround);
        }

        double avgResponse() {
            return average(response);
        }

        private double average(int[] values) {
            int sum = 0;
            for (int v : values) {
                sum += v;
            }
            return values.length == 0 ? 0.0 : (double) sum / values.length;
        }
    }

    // The main loop of the CPU tab: show the menu, run one algorithm on the
    // data the user typed, print the report, then ask again.
    public static void run(InputHelper in) {
        System.out.println("CPU SCHEDULING");
        System.out.println("Every process needs a name (or Enter for a default one),");
        System.out.println("an arrival time and a CPU burst time.");
        while (true) {
            System.out.println();
            System.out.println("  1) FCFS        (First-Come, First-Served)");
            System.out.println("  2) SJF         (Shortest Job First, non-preemptive)");
            System.out.println("  3) Round Robin (preemptive, fixed time quantum)");
            System.out.println("  4) Compare FCFS, SJF, RR and Priority on the same processes");
            System.out.println("  5) SRTF        (Shortest Remaining Time First, preemptive)");
            System.out.println("  6) Priority    (non-preemptive, smaller number = higher priority)");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 6);
            if (choice == 0) {
                return;
            }
            System.out.println();

            List<Process> procs = readProcesses(in);

            if (choice == 1) {
                printResult(procs, fcfs(procs), false);
            } else if (choice == 2) {
                printResult(procs, sjf(procs), false);
            } else if (choice == 3) {
                int quantum = in.readIntInRange("Time quantum (whole time units): ", 1, 100);
                printResult(procs, roundRobin(procs, quantum), true);
                System.out.println("Note: in Round Robin the execution list shows one entry");
                System.out.println("      per time slice, so a process can appear several times.");
            } else if (choice == 6) {
                readPriorities(in, procs);
                printResult(procs, prioritySchedule(procs), false);
            } else if (choice == 5) {
                printResult(procs, srtf(procs), true);
            } else {
                int quantum = in.readIntInRange("Time quantum for Round Robin (whole time units): ", 1, 100);
                readPriorities(in, procs);
                printComparison(procs, fcfs(procs), sjf(procs),
                        roundRobin(procs, quantum), prioritySchedule(procs));
            }
        }
    }

    // Reads the processes: an optional name, then arrival and burst. Names
    // default to P1, P2, ... and must be unique so the tables stay readable.
    private static List<Process> readProcesses(InputHelper in) {
        int n = in.readIntAtLeast("How many processes? ", 1);
        System.out.println("Names are optional: press Enter to use P1, P2, ...");
        List<Process> procs = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (int i = 0; i < n; i++) {
            String name;
            while (true) {
                String typed = in.readLine("  Name of process " + (i + 1) + ": ").trim();
                if (typed.isEmpty()) {
                    name = "P" + (i + 1);
                } else if (typed.length() > 10) {
                    System.out.println("  [!] Keep names to 10 characters or fewer.");
                    continue;
                } else {
                    name = typed;
                }
                if (used.contains(name.toLowerCase())) {
                    System.out.println("  [!] There is already a process called " + name + ".");
                    continue;
                }
                break;
            }
            used.add(name.toLowerCase());
            int arrival = in.readIntInRange("  Arrival time of " + name + ": ", 0, 1000);
            int burst = in.readIntInRange("  CPU burst time for " + name + ": ", 1, 1000);
            Process p = new Process(i + 1, name, burst);
            p.arrival = arrival;
            procs.add(p);
        }
        return procs;
    }

    private static void readPriorities(InputHelper in, List<Process> procs) {
        for (int i = 0; i < procs.size(); i++) {
            procs.get(i).priority =
                    in.readInt("  Priority of " + procs.get(i).name() + " (smaller number = higher priority): ");
        }
    }

    // FCFS: the arrived process that came first runs first; the CPU waits
    // when nobody has arrived yet. Ties: input order.
    static Result fcfs(List<Process> procs) {
        return schedule(procs, "FCFS (First-Come, First-Served)", new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (procs.get(a).arrival != procs.get(b).arrival) {
                    return procs.get(a).arrival - procs.get(b).arrival;
                }
                return procs.get(a).id - procs.get(b).id;
            }
        });
    }

    // SJF (non-preemptive): whenever the CPU is free, the arrived process
    // with the shortest burst runs next. Ties: earlier arrival, then id.
    static Result sjf(List<Process> procs) {
        return schedule(procs, "SJF (Shortest Job First, non-preemptive)", new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                Process pa = procs.get(a);
                Process pb = procs.get(b);
                if (pa.burst != pb.burst) {
                    return pa.burst - pb.burst;
                }
                if (pa.arrival != pb.arrival) {
                    return pa.arrival - pb.arrival;
                }
                return pa.id - pb.id;
            }
        });
    }

    // Priority (non-preemptive): the arrived process with the smallest
    // priority number runs next. Ties: earlier arrival, then id.
    static Result prioritySchedule(List<Process> procs) {
        return schedule(procs,
                "Priority (non-preemptive, smaller number = higher priority)",
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer a, Integer b) {
                        Process pa = procs.get(a);
                        Process pb = procs.get(b);
                        if (pa.priority != pb.priority) {
                            return pa.priority - pb.priority;
                        }
                        if (pa.arrival != pb.arrival) {
                            return pa.arrival - pb.arrival;
                        }
                        return pa.id - pb.id;
                    }
                });
    }

    // The shared non-preemptive engine: at each decision point pick the best
    // arrived process by the given rule, run it to completion, and let the
    // CPU idle until the next arrival when nothing is ready.
    private static Result schedule(List<Process> procs, String algorithm, Comparator<Integer> rule) {
        int n = procs.size();
        Result r = new Result(algorithm, n);
        boolean[] done = new boolean[n];
        int time = 0;
        int finished = 0;

        while (finished < n) {
            int pick = -1;
            for (int i = 0; i < n; i++) {
                if (done[i] || procs.get(i).arrival > time) {
                    continue;
                }
                if (pick < 0 || rule.compare(i, pick) < 0) {
                    pick = i;
                }
            }

            if (pick < 0) {
                // Nobody has arrived: jump straight to the next arrival.
                int next = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!done[i]) {
                        next = Math.min(next, procs.get(i).arrival);
                    }
                }
                r.addSegment(-1, time, next);
                time = next;
                continue;
            }

            int begin = time;
            r.start[pick] = begin;
            time += procs.get(pick).burst;
            r.completion[pick] = time;
            r.addSegment(pick, begin, time);
            r.order.add(pick);
            done[pick] = true;
            finished++;
        }

        fillTimes(r, procs);
        return r;
    }

    // Round Robin: every ready process gets one slice of the quantum from a
    // queue and an unfinished process goes to the back. Processes join the
    // queue when they arrive, and the CPU idles while the queue is empty.
    static Result roundRobin(List<Process> procs, int quantum) {
        int n = procs.size();
        Result r = new Result("Round Robin (quantum = " + quantum + ")", n);
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) {
            remaining[i] = procs.get(i).burst;
        }

        // Arrivals are admitted in arrival order (ties in input order).
        List<Integer> byArrival = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            byArrival.add(i);
        }
        Collections.sort(byArrival, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (procs.get(a).arrival != procs.get(b).arrival) {
                    return procs.get(a).arrival - procs.get(b).arrival;
                }
                return procs.get(a).id - procs.get(b).id;
            }
        });

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int nextArrival = 0;
        int time = 0;
        int finished = 0;

        while (finished < n) {
            nextArrival = admitArrivals(procs, byArrival, queue, nextArrival, time);

            if (queue.isEmpty()) {
                int jump = procs.get(byArrival.get(nextArrival)).arrival;
                r.addSegment(-1, time, jump);
                time = jump;
                continue;
            }

            int i = queue.poll();
            int slice = Math.min(quantum, remaining[i]);
            if (r.start[i] < 0) {
                r.start[i] = time;
            }
            r.addSegment(i, time, time + slice);
            r.order.add(i);
            time += slice;
            remaining[i] -= slice;

            // Processes that arrived during the slice queue up before the
            // preempted process goes to the back (the usual RR rule).
            nextArrival = admitArrivals(procs, byArrival, queue, nextArrival, time);

            if (remaining[i] == 0) {
                r.completion[i] = time;
                finished++;
            } else {
                queue.add(i);
            }
        }

        fillTimes(r, procs);
        return r;
    }

    // Adds every process that has arrived by 'time' to the ready queue and
    // returns the next index in byArrival that still has to arrive.
    private static int admitArrivals(List<Process> procs, List<Integer> byArrival,
                                     ArrayDeque<Integer> queue, int from, int time) {
        int k = from;
        while (k < byArrival.size() && procs.get(byArrival.get(k)).arrival <= time) {
            queue.add(byArrival.get(k));
            k++;
        }
        return k;
    }

    // SRTF / SJF preemptive: at every tick the arrived process with the
    // smallest remaining burst runs. Ties: earlier arrival, then id.
    static Result srtf(List<Process> procs) {
        int n = procs.size();
        Result r = new Result("SRTF (Shortest Remaining Time First, preemptive)", n);
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) {
            remaining[i] = procs.get(i).burst;
        }

        int time = 0;
        int finished = 0;
        while (finished < n) {
            int pick = -1;
            for (int i = 0; i < n; i++) {
                if (remaining[i] == 0 || procs.get(i).arrival > time) {
                    continue;
                }
                if (pick == -1
                        || remaining[i] < remaining[pick]
                        || (remaining[i] == remaining[pick]
                            && procs.get(i).arrival < procs.get(pick).arrival)
                        || (remaining[i] == remaining[pick]
                            && procs.get(i).arrival == procs.get(pick).arrival
                            && procs.get(i).id < procs.get(pick).id)) {
                    pick = i;
                }
            }

            if (pick == -1) {
                r.addSegment(-1, time, time + 1);
                time++;
                continue;
            }

            if (r.start[pick] < 0) {
                r.start[pick] = time;
                r.order.add(pick);
            }
            r.addSegment(pick, time, time + 1);
            remaining[pick]--;
            time++;
            if (remaining[pick] == 0) {
                r.completion[pick] = time;
                finished++;
            }
        }

        fillTimes(r, procs);
        return r;
    }

    // Turns start / completion into the rubric numbers: turnaround is
    // completion minus arrival, waiting is turnaround minus the burst, and
    // response is the first start minus arrival.
    private static void fillTimes(Result r, List<Process> procs) {
        for (int i = 0; i < procs.size(); i++) {
            Process p = procs.get(i);
            r.turnaround[i] = r.completion[i] - p.arrival;
            r.waiting[i] = r.turnaround[i] - p.burst;
            r.response[i] = r.start[i] - p.arrival;
        }
    }

    // One report for every algorithm: execution order, Gantt chart, the
    // timing table, then the three averages.
    private static void printResult(List<Process> procs, Result r, boolean preemptionLabels) {
        System.out.println();
        System.out.println("Algorithm: " + r.algorithm);
        System.out.println("Execution order: " + names(procs, r.order));
        System.out.println();
        System.out.println("Gantt chart:");
        System.out.println(gantt(procs, r, preemptionLabels));
        System.out.println();
        System.out.printf("%-10s %7s %5s %10s %10s %7s %8s%n",
                "Process", "Arrival", "Burst", "Completion", "Turnaround", "Waiting", "Response");
        System.out.println("---------- ------- ----- ---------- ---------- ------- --------");
        for (int i = 0; i < procs.size(); i++) {
            Process p = procs.get(i);
            System.out.printf("%-10s %7d %5d %10d %10d %7d %8d%n",
                    p.name(), p.arrival, p.burst,
                    r.completion[i], r.turnaround[i], r.waiting[i], r.response[i]);
        }
        System.out.printf("Average turnaround time: %.2f%n", r.avgTurnaround());
        System.out.printf("Average waiting time   : %.2f%n", r.avgWaiting());
        System.out.printf("Average response time  : %.2f%n", r.avgResponse());
    }

    private static String gantt(List<Process> procs, Result r, boolean preemptionLabels) {
        StringBuilder chart = new StringBuilder("  ");
        for (int i = 0; i < r.segments.size(); i++) {
            int[] seg = r.segments.get(i);
            String label;
            if (seg[0] < 0) {
                label = "idle";
            } else {
                label = procs.get(seg[0]).name();
                if (preemptionLabels && r.completion[seg[0]] > seg[2]) {
                    label += " (preempted)";
                }
            }
            if (i > 0) {
                chart.append(" | ");
            }
            chart.append(seg[1]).append("-").append(seg[2]).append(" ").append(label);
        }
        return chart.toString();
    }

    // Side-by-side table of FCFS, SJF, Round Robin and Priority on the same
    // processes, so the user can see which algorithm is better and why.
    private static void printComparison(List<Process> procs, Result fcfsR, Result sjfR,
                                        Result rrR, Result priR) {
        System.out.println();
        System.out.println("COMPARISON OF THE FOUR ALGORITHMS");
        System.out.println("(W = waiting time, T = turnaround time)");
        System.out.println();
        System.out.printf("%-10s %5s | %-8s | %-8s | %-8s | %-8s%n",
                "Process", "Burst", "FCFS", "SJF", "RR", "Priority");
        System.out.printf("%-10s %5s | %-8s | %-8s | %-8s | %-8s%n",
                "", "", "W    T", "W    T", "W    T", "W    T");
        System.out.println("---------- ----- | -------- | -------- | -------- | --------");
        for (int i = 0; i < procs.size(); i++) {
            System.out.printf("%-10s %5d | %-8s | %-8s | %-8s | %-8s%n",
                    procs.get(i).name(), procs.get(i).burst,
                    fcfsR.waiting[i] + " " + fcfsR.turnaround[i],
                    sjfR.waiting[i] + " " + sjfR.turnaround[i],
                    rrR.waiting[i] + " " + rrR.turnaround[i],
                    priR.waiting[i] + " " + priR.turnaround[i]);
        }
        System.out.println("---------- ----- | -------- | -------- | -------- | --------");
        System.out.println("Averages (waiting / turnaround):");
        System.out.printf("  %-9s: %.2f / %.2f%n", "FCFS", fcfsR.avgWaiting(), fcfsR.avgTurnaround());
        System.out.printf("  %-9s: %.2f / %.2f%n", "SJF", sjfR.avgWaiting(), sjfR.avgTurnaround());
        System.out.printf("  %-9s: %.2f / %.2f%n", "RR", rrR.avgWaiting(), rrR.avgTurnaround());
        System.out.printf("  %-9s: %.2f / %.2f%n", "Priority", priR.avgWaiting(), priR.avgTurnaround());
        System.out.println();
        System.out.println("Best average waiting time   : "
                + bestOf(new Result[] {fcfsR, sjfR, rrR, priR}, true));
        System.out.println("Best average turnaround time: "
                + bestOf(new Result[] {fcfsR, sjfR, rrR, priR}, false));
    }

    private static String bestOf(Result[] results, boolean waiting) {
        Result best = results[0];
        for (Result r : results) {
            double mine = waiting ? r.avgWaiting() : r.avgTurnaround();
            double theirs = waiting ? best.avgWaiting() : best.avgTurnaround();
            if (mine < theirs) {
                best = r;
            }
        }
        return shortName(best.algorithm) + " (" + String.format("%.2f", waiting
                ? best.avgWaiting() : best.avgTurnaround()) + ")";
    }

    private static String shortName(String algorithm) {
        if (algorithm.startsWith("Round")) {
            return "RR";
        }
        int cut = algorithm.indexOf(' ');
        return cut < 0 ? algorithm : algorithm.substring(0, cut);
    }

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
