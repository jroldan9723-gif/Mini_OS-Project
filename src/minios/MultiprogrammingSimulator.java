package minios;

import java.util.ArrayList;
import java.util.List;

// Jobs share memory, the CPU and one I/O device. Everything happens in
// ticks so one table row can be printed per tick.
public class MultiprogrammingSimulator {

    // How long one I/O takes, in ticks.
    private static final int IO_DURATION = 3;
    // Safety limit, so a mistake cannot make the simulation run forever.
    private static final int MAX_TICKS = 10000;

    private static class Job {
        String name;
        int mem;
        int burst;
        int arrivalTime;
        int executed = 0;
        int quantumUsed = 0;
        int remainingIo = 0;
        boolean ioDone = false;

        String state = "NOT ARRIVED";
        int loadedAt = -1;
        int finishedAt = -1;
        int waitTicks = 0;

        Job(String name, int mem, int burst, int arrivalTime) {
            this.name = name;
            this.mem = mem;
            this.burst = burst;
            this.arrivalTime = arrivalTime;
        }

        int ioPoint() {
            return (burst + 1) / 2;
        }

        int remainingCpu() {
            return burst - executed;
        }
    }

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

    public static void run(InputHelper in) {
        if (!InputHelper.isQuiet()) {
            System.out.println("MULTIPROGRAMMING - jobs sharing memory and the CPU");
        }
        while (true) {
            if (!InputHelper.isQuiet()) {
                System.out.println();
                System.out.println("  1) Start a new multiprogramming simulation");
                System.out.println("  0) Back to main menu");
            }
            int choice = in.readIntInRange("  Choose an option: ", 0, 1);
            if (choice == 0) {
                return;
            }
            System.out.println();
            runOnce(in);
        }
    }

    private static void runOnce(InputHelper in) {
        int memSize = in.readIntInRange("Main memory size (KB): ", 1, 1_000_000);
        int n = in.readIntAtLeast("How many jobs? ", 1);

        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name;
            while (true) {
                name = in.readLine("  Name of job " + (i + 1) + " (e.g. J1): ").trim();
                if (name.isEmpty()) {
                    System.out.println("  [!] The name cannot be empty.");
                } else if (findJob(jobs, name) != null) {
                    System.out.println("  [!] There is already a job called " + name + ".");
                } else {
                    break;
                }
            }
            int size = in.readIntInRange("  Memory needed by " + name + " (KB): ", 1, memSize);
            int burst = in.readIntInRange("  CPU burst of " + name + " (ticks): ", 1, 1000);
            int arrival = in.readIntInRange("  Arrival time of " + name + " (ticks): ", 0, 10000);
            jobs.add(new Job(name, size, burst, arrival));
        }

        int quantum = in.readIntInRange("Time quantum for the CPU (ticks): ", 1, 100);

        System.out.println();
        if (!InputHelper.isQuiet()) {
            System.out.println("Simulation rules:");
            System.out.println("  - first-fit contiguous loading, memory = " + memSize + " KB");
            System.out.println("  - Round Robin on the CPU, quantum = " + quantum + " tick(s)");
            System.out.println("  - each job does ONE I/O of " + IO_DURATION
                    + " ticks at the halfway point of its burst, one job at a time on the device");
            System.out.println("  - a job only exists for the OS once its arrival time is reached");
            System.out.println();
        }
        System.out.println("Tick | CPU | READY (queue order) | WAITING | I/O device | JOB QUEUE | not arrived | memory");
        System.out.println("-----+-----+---------------------+---------+------------+-----------+-------------+-------");

        List<Job> ready = new ArrayList<>();
        List<Alloc> memory = new ArrayList<>();
        Job current = null;
        Job ioJob = null;
        List<Job> ioQueue = new ArrayList<>();
        int time = 0;
        int terminated = 0;

        while (terminated < jobs.size() && time < MAX_TICKS) {
            List<String> events = new ArrayList<>();
            boolean memoryChanged = false;

            for (Job j : jobs) {
                if ("NOT ARRIVED".equals(j.state) && j.arrivalTime <= time) {
                    j.state = "JOB QUEUE";
                    events.add(j.name + " arrived -> JOB QUEUE");
                }
            }

            for (Job j : jobs) {
                if (!"JOB QUEUE".equals(j.state)) {
                    continue;
                }
                int hole = firstFit(memory, j.mem, memSize);
                if (hole >= 0) {
                    insertSorted(memory, new Alloc(j.name, hole, j.mem));
                    j.state = "READY";
                    j.loadedAt = time;
                    ready.add(j);
                    events.add(j.name + " LOADED (" + j.mem + " KB at " + hole + ")");
                    memoryChanged = true;
                }

            }

            if (current == null && !ready.isEmpty()) {
                current = ready.remove(0);
                current.state = "RUNNING";
                current.quantumUsed = 0;
                events.add(current.name + " -> RUNNING");
            }

            Job ioThisTick = ioJob;
            if (ioJob != null) {
                ioJob.remainingIo--;
                if (ioJob.remainingIo == 0) {
                    events.add(ioJob.name + " I/O finished - device free");
                    ioJob.state = "READY";
                    ready.add(ioJob);
                    ioJob = null;
                }
            }
            if (ioJob == null && !ioQueue.isEmpty()) {
                Job next = ioQueue.remove(0);
                next.remainingIo = IO_DURATION;
                ioJob = next;
                next.state = "WAITING (I/O)";
                events.add(next.name + " starts I/O (" + IO_DURATION + " ticks) - device in use");
            }

            Job runningThisTick = current;
            if (current != null) {
                Job j = current;
                j.executed++;
                j.quantumUsed++;

                if (j.executed >= j.burst) {

                    j.state = "TERMINATED";
                    j.finishedAt = time + 1;
                    removeFromMemory(memory, j.name);
                    events.add(j.name + " FINISHED (memory freed)");
                    terminated++;
                    current = null;
                    memoryChanged = true;
                } else if (j.executed == j.ioPoint() && !j.ioDone) {

                    j.ioDone = true;
                    if (ioJob == null) {
                        j.remainingIo = IO_DURATION;
                        ioJob = j;
                        j.state = "WAITING (I/O)";
                        events.add(j.name + " starts I/O (" + IO_DURATION + " ticks) - device in use");
                    } else {

                        j.state = "WAITING (I/O queue)";
                        ioQueue.add(j);
                        events.add(j.name + " waits for the I/O device (in use by "
                                + ioJob.name + ")");
                    }
                    current = null;
                } else if (j.quantumUsed >= quantum) {

                    j.state = "READY";
                    ready.add(j);
                    events.add(j.name + " quantum expired -> READY");
                    current = null;
                }
            }

            for (Job j : jobs) {
                if (j == runningThisTick || j == ioThisTick
                        || "NOT ARRIVED".equals(j.state) || "TERMINATED".equals(j.state)) {
                    continue;
                }
                j.waitTicks++;
            }

            String snap = snapshot(runningThisTick, ready, jobs, memory, ioJob);
            String evts = events.isEmpty() ? "-" : join(events);
            System.out.printf("%-4d | %s | %s%n", time, snap, evts);
            if (memoryChanged) {
                printMemoryLayout(memory, memSize, jobs);
            }
            time++;
        }

        if (terminated < jobs.size()) {
            System.out.println("[!] Simulation stopped at the safety limit (" + MAX_TICKS
                    + " ticks). This should not happen - please report the input.");
            return;
        }

        int busyTicks = 0;
        double sumTurnaround = 0;
        double sumWaiting = 0;
        for (Job j : jobs) {
            busyTicks += j.burst;
            sumTurnaround += (j.finishedAt - j.arrivalTime);
            sumWaiting += j.waitTicks;
        }

        System.out.println();
        System.out.println("All jobs finished at tick " + time + ".");
        System.out.println();
        System.out.printf("%-6s %8s %8s %6s %10s %12s %11s %8s%n",
                "Job", "Arrival", "Mem(KB)", "Burst", "Loaded at", "Finished at",
                "Turnaround", "Waiting");
        System.out.println("------ -------- -------- ------ ---------- ------------ ----------- --------");
        for (Job j : jobs) {
            System.out.printf("%-6s %8d %8d %6d %10d %12d %11d %8d%n",
                    j.name, j.arrivalTime, j.mem, j.burst, j.loadedAt, j.finishedAt,
                    j.finishedAt - j.arrivalTime, j.waitTicks);
        }
        System.out.println();
        System.out.println("Total simulation time  : " + time + " ticks");
        System.out.printf("Average turnaround time: %.2f ticks%n", sumTurnaround / jobs.size());
        System.out.printf("Average waiting time   : %.2f ticks%n", sumWaiting / jobs.size());
        System.out.printf("CPU utilization        : %.2f%% (%d busy ticks out of %d)%n",
                100.0 * busyTicks / time, busyTicks, time);
        System.out.println("Completed jobs         : " + terminated + " / " + jobs.size());
        // The GUI output stays results-only, so the GUI tab shows this rule in
        // its hint line instead.
        if (!InputHelper.isQuiet()) {
            System.out.println();
            System.out.println("Note: turnaround = finished at - arrival time. Waiting time counts");
            System.out.println("the ticks spent in the job queue, the ready queue or the I/O queue;");
            System.out.println("time running and time actually on the I/O device are not waiting.");
        }
    }

    private static String snapshot(Job runningThisTick, List<Job> ready, List<Job> jobs,
                                   List<Alloc> memory, Job ioJob) {

        StringBuilder cpu;
        if (runningThisTick == null) {
            cpu = new StringBuilder("idle");
        } else {
            cpu = new StringBuilder(runningThisTick.name)
                    .append(" (").append(Math.max(0, runningThisTick.remainingCpu())).append(" left)");
        }

        StringBuilder readyList = new StringBuilder();
        for (Job j : ready) {
            if (readyList.length() > 0) {
                readyList.append(", ");
            }
            readyList.append(j.name);
        }

        StringBuilder waiting = new StringBuilder();
        StringBuilder queued = new StringBuilder();
        StringBuilder notYet = new StringBuilder();
        for (Job j : jobs) {
            if ("WAITING (I/O)".equals(j.state)) {
                appendComma(waiting, j.name + " (I/O, " + j.remainingIo + " left)");
            } else if ("WAITING (I/O queue)".equals(j.state)) {
                appendComma(waiting, j.name + " (I/O queue)");
            } else if ("JOB QUEUE".equals(j.state)) {
                appendComma(queued, j.name);
            } else if ("NOT ARRIVED".equals(j.state)) {
                appendComma(notYet, j.name + "@" + j.arrivalTime);
            }
        }

        StringBuilder mem = new StringBuilder();
        for (Alloc a : memory) {
            appendComma(mem, a.name);
        }

        String device = ioJob == null ? "free" : ioJob.name + " (" + ioJob.remainingIo + " left)";

        return "CPU: " + cpu
                + " | READY: " + (readyList.length() == 0 ? "-" : readyList)
                + " | WAITING: " + (waiting.length() == 0 ? "-" : waiting)
                + " | I/O device: " + device
                + " | QUEUE: " + (queued.length() == 0 ? "-" : queued)
                + " | not arrived: " + (notYet.length() == 0 ? "-" : notYet)
                + " | MEM: " + (mem.length() == 0 ? "-" : mem);
    }

    private static void appendComma(StringBuilder sb, String value) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(value);
    }

    private static void printMemoryLayout(List<Alloc> memory, int memSize, List<Job> jobs) {
        StringBuilder sb = new StringBuilder("    memory: ");
        int cursor = 0;
        for (Alloc a : memory) {
            if (a.start > cursor) {
                sb.append("[free ").append(cursor).append('-').append(a.start).append("] ");
            }
            sb.append('[').append(a.name).append(' ').append(a.start).append('-')
              .append(a.end()).append(", ").append(a.size).append(" KB] ");
            cursor = a.end();
        }
        if (cursor < memSize) {
            sb.append("[free ").append(cursor).append('-').append(memSize).append("] ");
        }

        int totalFree = 0;
        int largestHole = 0;
        cursor = 0;
        for (Alloc a : memory) {
            int hole = a.start - cursor;
            totalFree += hole;
            largestHole = Math.max(largestHole, hole);
            cursor = a.end();
        }
        int tail = memSize - cursor;
        totalFree += tail;
        largestHole = Math.max(largestHole, tail);

        sb.append("| free total ").append(totalFree).append(" KB, largest hole ")
          .append(largestHole).append(" KB");
        System.out.println(sb);

        StringBuilder frag = new StringBuilder();
        for (Job j : jobs) {
            if ("JOB QUEUE".equals(j.state) && j.mem <= totalFree && j.mem > largestHole) {
                appendComma(frag, j.name + " (needs " + j.mem + " KB)");
            }
        }
        if (frag.length() > 0) {
            System.out.println("    external fragmentation: " + frag
                    + " would fit in total free memory but not in any single hole");
        }
    }

    private static String join(List<String> events) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(events.get(i));
        }
        return sb.toString();
    }

    // Start address of the first hole that is big enough, or -1 if none fits.
    private static int firstFit(List<Alloc> memory, int size, int memSize) {
        int cursor = 0;
        for (Alloc a : memory) {
            if (a.start - cursor >= size) {
                return cursor;
            }
            cursor = a.end();
        }
        if (memSize - cursor >= size) {
            return cursor;
        }
        return -1;
    }

    private static void insertSorted(List<Alloc> memory, Alloc a) {
        int pos = 0;
        while (pos < memory.size() && memory.get(pos).start < a.start) {
            pos++;
        }
        memory.add(pos, a);
    }

    private static void removeFromMemory(List<Alloc> memory, String name) {
        for (int i = 0; i < memory.size(); i++) {
            if (memory.get(i).name.equals(name)) {
                memory.remove(i);
                return;
            }
        }
    }

    private static Job findJob(List<Job> jobs, String name) {
        for (Job j : jobs) {
            if (j.name.equalsIgnoreCase(name)) {
                return j;
            }
        }
        return null;
    }
}
