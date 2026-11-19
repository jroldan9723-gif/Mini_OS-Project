package minios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CPUScheduler {

    static class Process {
        int id;
        int burst;
        int arrival;

        Process(int id, int burst) {
            this.id = id;
            this.burst = burst;
        }

        String name() {
            return "P" + id;
        }
    }

    static class Result {
        String algorithm;
        int[] waiting;
        int[] turnaround;
        List<Integer> order;

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

    public static void run(InputHelper in) {
        System.out.println("CPU SCHEDULING");
        System.out.println("Options 1-4 assume every process arrives at time 0.");
        System.out.println("SRTF (option 5) asks for an arrival time per process.");
        while (true) {
            System.out.println();
            System.out.println("  1) FCFS        (First-Come, First-Served)");
            System.out.println("  2) SJF         (Shortest Job First, non-preemptive)");
            System.out.println("  3) Round Robin (preemptive, fixed time quantum)");
            System.out.println("  4) Compare all three on the same processes");
            System.out.println("  5) SRTF        (Shortest Remaining Time First, preemptive)");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 5);
            if (choice == 0) {
                return;
            }
            System.out.println();

            if (choice == 5) {

                List<Process> procs = readSrtfProcesses(in);
                printSrtfResult(procs, srtf(procs));
                continue;
            }

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

    private static List<Process> readProcesses(InputHelper in) {
        int n = in.readIntInRange("How many processes? ", 1, 20);
        List<Process> procs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int burst = in.readIntInRange("  CPU burst time for P" + (i + 1) + ": ", 1, 1000);
            procs.add(new Process(i + 1, burst));
        }
        return procs;
    }

    private static List<Process> readSrtfProcesses(InputHelper in) {
        int n = in.readIntInRange("How many processes? ", 1, 20);
        List<Process> procs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int arrival = in.readIntInRange("  Arrival time of P" + (i + 1) + ": ", 0, 1000);
            int burst = in.readIntInRange("  CPU burst time for P" + (i + 1) + ": ", 1, 1000);
            Process p = new Process(i + 1, burst);
            p.arrival = arrival;
            procs.add(p);
        }
        return procs;
    }

    static Result fcfs(List<Process> procs) {
        Result r = new Result("FCFS (First-Come, First-Served)", procs.size());
        int currentTime = 0;
        for (int i = 0; i < procs.size(); i++) {
            Process p = procs.get(i);
            r.waiting[i] = currentTime;
            r.turnaround[i] = currentTime + p.burst;
            currentTime += p.burst;
            r.order.add(i);
        }
        return r;
    }

    static Result sjf(List<Process> procs) {
        Result r = new Result("SJF (Shortest Job First, non-preemptive)", procs.size());

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
                    return pa.burst - pb.burst;
                }
                return pa.id - pb.id;
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

    static Result roundRobin(List<Process> procs, int quantum) {
        Result r = new Result("Round Robin (quantum = " + quantum + ")", procs.size());
        int n = procs.size();

        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) {
            remaining[i] = procs.get(i).burst;
        }

        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            queue.add(i);
        }

        int currentTime = 0;
        int finished = 0;
        while (finished < n) {
            int i = queue.poll();
            int slice = Math.min(quantum, remaining[i]);
            r.order.add(i);
            currentTime += slice;
            remaining[i] -= slice;

            if (remaining[i] == 0) {

                r.turnaround[i] = currentTime;
                r.waiting[i] = currentTime - procs.get(i).burst;
                finished++;
            } else {
                queue.add(i);
            }
        }
        return r;
    }

    static class SrtfResult {
        int[] completion;
        int[] turnaround;
        int[] waiting;
        int[] response;
        List<int[]> segments;
        List<Integer> order;

        SrtfResult(int n) {
            completion = new int[n];
            turnaround = new int[n];
            waiting = new int[n];
            response = new int[n];
            segments = new ArrayList<>();
            order = new ArrayList<>();
        }
    }

    static SrtfResult srtf(List<Process> procs) {
        int n = procs.size();
        SrtfResult r = new SrtfResult(n);
        int[] remaining = new int[n];
        int[] firstStart = new int[n];
        for (int i = 0; i < n; i++) {
            remaining[i] = procs.get(i).burst;
            firstStart[i] = -1;
        }

        List<Integer> owner = new ArrayList<>();
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
                owner.add(-1);
                time++;
                continue;
            }

            owner.add(pick);
            if (firstStart[pick] < 0) {
                firstStart[pick] = time;
            }
            remaining[pick]--;
            time++;
            if (remaining[pick] == 0) {
                r.completion[pick] = time;
                finished++;
            }
        }

        int tick = 0;
        while (tick < owner.size()) {
            int who = owner.get(tick);
            int start = tick;
            while (tick < owner.size() && owner.get(tick) == who) {
                tick++;
            }
            r.segments.add(new int[] {who, start, tick});
            if (who >= 0) {
                r.order.add(who);
            }
        }

        for (int i = 0; i < n; i++) {
            r.turnaround[i] = r.completion[i] - procs.get(i).arrival;
            r.waiting[i] = r.turnaround[i] - procs.get(i).burst;
            r.response[i] = firstStart[i] - procs.get(i).arrival;
        }
        return r;
    }

    private static void printSrtfResult(List<Process> procs, SrtfResult r) {
        System.out.println();
        System.out.println("Algorithm: SRTF (Shortest Remaining Time First, preemptive)");

        StringBuilder order = new StringBuilder();
        for (int k = 0; k < r.order.size(); k++) {
            if (k > 0) {
                order.append(" -> ");
            }
            order.append(procs.get(r.order.get(k)).name());
        }
        System.out.println("Execution order: " + order);

        System.out.println();
        System.out.println("Gantt chart:");
        StringBuilder gantt = new StringBuilder("  ");
        for (int i = 0; i < r.segments.size(); i++) {
            int[] seg = r.segments.get(i);
            String label;
            if (seg[0] < 0) {
                label = "idle";
            } else {
                label = procs.get(seg[0]).name();
                if (r.completion[seg[0]] > seg[2]) {
                    label += " (preempted)";
                }
            }
            if (i > 0) {
                gantt.append(" | ");
            }
            gantt.append(seg[1]).append("-").append(seg[2]).append(" ").append(label);
        }
        System.out.println(gantt);

        System.out.println();
        System.out.printf("%-8s %8s %6s %11s %11s %8s %9s%n",
                "Process", "Arrival", "Burst", "Completion", "Turnaround", "Waiting", "Response");
        System.out.println("-------- -------- ------ ----------- ----------- -------- ---------");
        double sumTat = 0;
        double sumWt = 0;
        double sumRt = 0;
        for (int i = 0; i < procs.size(); i++) {
            System.out.printf("%-8s %8d %6d %11d %11d %8d %9d%n",
                    procs.get(i).name(), procs.get(i).arrival, procs.get(i).burst,
                    r.completion[i], r.turnaround[i], r.waiting[i], r.response[i]);
            sumTat += r.turnaround[i];
            sumWt += r.waiting[i];
            sumRt += r.response[i];
        }
        System.out.printf("Average turnaround time: %.2f%n", sumTat / procs.size());
        System.out.printf("Average waiting time   : %.2f%n", sumWt / procs.size());
        System.out.printf("Average response time  : %.2f%n", sumRt / procs.size());
    }

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
