// File: src/minios/MultiprogrammingSimulator.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * MultiprogrammingSimulator - several jobs share ONE CPU and ONE memory.
 *
 * WHAT THIS DEMONSTRATES
 *   - Every job has an ARRIVAL TIME. Before it arrives it does not exist for
 *     the OS (state NOT ARRIVED); at its arrival time it enters the JOB QUEUE.
 *   - Jobs wait in the JOB QUEUE until there is enough free MEMORY to load them.
 *   - Loaded jobs sit in the READY state until the CPU is free.
 *   - The CPU runs one job at a time and switches away after a time quantum
 *     (Round Robin), so every loaded job makes progress.
 *   - A job can leave the CPU to WAIT for the single I/O device and later
 *     come back to READY - the usual job states:
 *
 *        NOT ARRIVED --(arrival time)--> JOB QUEUE --(memory found)--> READY
 *                                                              READY --(CPU)--> RUNNING
 *           ^                                     ^               |
 *           |                                     +---(quantum)---+
 *           |                                     |               |
 *           +--(terminated, memory freed)---- TERMINATED   WAITING (I/O or I/O queue)
 *
 * SIMPLIFICATIONS:
 *   - Jobs are loaded CONTIGUOUSLY with first-fit; there is no paging or
 *     segmentation inside this simulation.
 *   - There is one CPU and ONE I/O device: only one job can use the device
 *     at a time, other jobs line up in an I/O queue. Each job performs ONE
 *     I/O operation at the halfway point of its burst, lasting IO_DURATION
 *     ticks on the device.
 *   - A "tick" is an abstract time unit, not a real millisecond.
 *   - When things could happen in the same tick, this order is used:
 *     (1) jobs arrive, (2) load jobs into memory, (3) give the CPU to a
 *     ready job, (4) progress the I/O device, (5) run the current job for
 *     one tick.
 *
 * TIME DEFINITIONS used in the final report:
 *   Turnaround = finished at - arrival time.
 *   Waiting    = ticks spent in the JOB QUEUE, READY queue or I/O queue
 *                (time running and time actually on the I/O device do NOT
 *                count as waiting).
 */
public class MultiprogrammingSimulator {

    private static final int IO_DURATION = 3;   // ticks of the one simulated I/O per job
    private static final int MAX_TICKS = 10000; // safety limit in case of a bug

    /** Our tiny "job control block". */
    private static class Job {
        String name;
        int mem;          // kilobytes of RAM this job needs while loaded
        int burst;        // total CPU ticks the job needs
        int arrivalTime;  // tick at which the job appears (enters the job queue)
        int executed = 0; // CPU ticks received so far
        int quantumUsed = 0; // CPU ticks since the job last got the CPU
        int remainingIo = 0; // ticks left of I/O (0 = not doing I/O now)
        boolean ioDone = false;
        // NOT ARRIVED | JOB QUEUE | READY | RUNNING | WAITING (I/O) |
        // WAITING (I/O queue) | TERMINATED
        String state = "NOT ARRIVED";
        int loadedAt = -1;
        int finishedAt = -1;
        int waitTicks = 0; // ticks spent waiting (queue / ready / I/O queue)

        Job(String name, int mem, int burst, int arrivalTime) {
            this.name = name;
            this.mem = mem;
            this.burst = burst;
            this.arrivalTime = arrivalTime;
        }

        /** The tick at which the job starts its one I/O: half its burst, rounded up. */
        int ioPoint() {
            return (burst + 1) / 2;
        }

        int remainingCpu() {
            return burst - executed;
        }
    }

    /** One occupied chunk of contiguous memory. */
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
        System.out.println("MULTIPROGRAMMING - jobs sharing memory and the CPU");
        while (true) {
            System.out.println();
            System.out.println("  1) Start a new multiprogramming simulation");
            System.out.println("  0) Back to main menu");
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
        int n = in.readIntInRange("How many jobs? ", 1, 20);

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
        System.out.println("Simulation rules:");
        System.out.println("  - first-fit contiguous loading, memory = " + memSize + " KB");
        System.out.println("  - Round Robin on the CPU, quantum = " + quantum + " tick(s)");
        System.out.println("  - each job does ONE I/O of " + IO_DURATION
                + " ticks at the halfway point of its burst, one job at a time on the device");
        System.out.println("  - a job only exists for the OS once its arrival time is reached");
        System.out.println();
        System.out.println("Tick | CPU | READY (queue order) | WAITING | I/O device | JOB QUEUE | not arrived | memory");
        System.out.println("-----+-----+---------------------+---------+------------+-----------+-------------+-------");

        // ---- initial state: nobody has arrived yet ---------------------------
        List<Job> ready = new ArrayList<>();        // loaded, waiting for the CPU
        List<Alloc> memory = new ArrayList<>();     // occupied chunks, kept sorted
        Job current = null;                         // the job using the CPU (null = CPU idle)
        Job ioJob = null;                           // the ONE job on the I/O device
        List<Job> ioQueue = new ArrayList<>();      // jobs waiting for the device
        int time = 0;
        int terminated = 0;

        while (terminated < jobs.size() && time < MAX_TICKS) {
            List<String> events = new ArrayList<>();
            boolean memoryChanged = false;

            // (0) ARRIVALS: jobs appear (and may enter the job queue) at
            // their arrival time. Jobs that have not arrived are invisible
            // to the loader and to the CPU.
            for (Job j : jobs) {
                if ("NOT ARRIVED".equals(j.state) && j.arrivalTime <= time) {
                    j.state = "JOB QUEUE";
                    events.add(j.name + " arrived -> JOB QUEUE");
                }
            }

            // (1) LOADER: try to load waiting jobs (in job order) while space fits.
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
                // no room yet: this job keeps waiting for memory
            }

            // (2) DISPATCHER: if the CPU is idle, give it to the first ready job.
            if (current == null && !ready.isEmpty()) {
                current = ready.remove(0);
                current.state = "RUNNING";
                current.quantumUsed = 0; // fresh time slice
                events.add(current.name + " -> RUNNING");
            }

            // (3) I/O DEVICE: exactly one tick for the job that is on the
            // device; when it finishes, the next queued job takes the device.
            Job ioThisTick = ioJob; // the job that is ON THE DEVICE this tick
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

            // (4) CPU: the current job runs for exactly one tick.
            // Keep whoever is using the CPU this tick, because the job may
            // finish or yield at the end of the tick and would then disappear
            // from the state columns even though it used this tick.
            Job runningThisTick = current;
            if (current != null) {
                Job j = current;
                j.executed++;
                j.quantumUsed++;

                if (j.executed >= j.burst) {
                    // Job finished: free its memory so others can be loaded.
                    j.state = "TERMINATED";
                    j.finishedAt = time + 1;
                    removeFromMemory(memory, j.name);
                    events.add(j.name + " FINISHED (memory freed)");
                    terminated++;
                    current = null;
                    memoryChanged = true;
                } else if (j.executed == j.ioPoint() && !j.ioDone) {
                    // Halfway point reached: this job wants the I/O device.
                    j.ioDone = true;
                    if (ioJob == null) {
                        j.remainingIo = IO_DURATION;
                        ioJob = j;
                        j.state = "WAITING (I/O)";
                        events.add(j.name + " starts I/O (" + IO_DURATION + " ticks) - device in use");
                    } else {
                        // Device is busy: wait in the I/O queue (NOT ready).
                        j.state = "WAITING (I/O queue)";
                        ioQueue.add(j);
                        events.add(j.name + " waits for the I/O device (in use by "
                                + ioJob.name + ")");
                    }
                    current = null;
                } else if (j.quantumUsed >= quantum) {
                    // Out of time slice: back of the ready queue.
                    j.state = "READY";
                    ready.add(j);
                    events.add(j.name + " quantum expired -> READY");
                    current = null;
                }
            }

            // Waiting-time bookkeeping: a job is WAITING during this tick
            // when it has arrived, is not finished, and used neither the CPU
            // nor the I/O device this tick (it sat in the job queue, the
            // ready queue or the I/O queue). This keeps the identity
            // turnaround = waiting + burst + I/O time exact.
            for (Job j : jobs) {
                if (j == runningThisTick || j == ioThisTick
                        || "NOT ARRIVED".equals(j.state) || "TERMINATED".equals(j.state)) {
                    continue;
                }
                j.waitTicks++;
            }

            // ---- print this tick's snapshot --------------------------------
            String snap = snapshot(runningThisTick, ready, jobs, memory, ioJob);
            String evts = events.isEmpty() ? "-" : join(events);
            System.out.printf("%-4d | %s | %s%n", time, snap, evts);
            if (memoryChanged) {
                printMemoryLayout(memory, memSize, jobs); // shows holes + fragmentation
            }
            time++;
        }

        if (terminated < jobs.size()) {
            System.out.println("[!] Simulation stopped at the safety limit (" + MAX_TICKS
                    + " ticks). This should not happen - please report the input.");
            return;
        }

        // ---- final report ---------------------------------------------------
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
        System.out.println();
        System.out.println("Note: turnaround = finished at - arrival time. Waiting time counts");
        System.out.println("the ticks spent in the job queue, the ready queue or the I/O queue;");
        System.out.println("time running and time actually on the I/O device are not waiting.");
    }

    // ------------------------------------------------------------------
    // Snapshot helpers - what the user sees in the trace table
    // ------------------------------------------------------------------

    /** One-line description of every job's state at the END of this tick. */
    private static String snapshot(Job runningThisTick, List<Job> ready, List<Job> jobs,
                                   List<Alloc> memory, Job ioJob) {
        // CPU column: whoever consumed this tick's time slice (may have
        // finished or yielded at the very end of the tick).
        StringBuilder cpu;
        if (runningThisTick == null) {
            cpu = new StringBuilder("idle");
        } else {
            cpu = new StringBuilder(runningThisTick.name)
                    .append(" (").append(Math.max(0, runningThisTick.remainingCpu())).append(" left)");
        }

        // READY column: actual queue order, so the viewer sees who runs next.
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
        for (Job j : jobs) { // original job order (TERMINATED jobs are not listed)
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

    /** Appends "value" to a comma-separated list (creates the first entry). */
    private static void appendComma(StringBuilder sb, String value) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(value);
    }

    /**
     * Prints the memory layout with addresses and sizes, the free totals and
     * (when it applies) the external fragmentation situation. Called whenever
     * the layout changes (a job was loaded or finished).
     */
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
        for (Alloc a : memory) { // holes before, between and after blocks
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

        // External fragmentation: jobs waiting for memory that would fit in
        // the TOTAL free space but not in any SINGLE hole.
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

    // ------------------------------------------------------------------
    // Memory helpers (first-fit into one contiguous block)
    // ------------------------------------------------------------------

    /** @return start address of the first hole big enough, or -1 if none. */
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
