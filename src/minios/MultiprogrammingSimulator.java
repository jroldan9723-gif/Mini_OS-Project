// File: src/minios/MultiprogrammingSimulator.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * MultiprogrammingSimulator - several jobs share ONE CPU and ONE memory.
 *
 * WHAT THIS DEMONSTRATES
 *   - Jobs wait in a JOB QUEUE until there is enough free MEMORY to load them.
 *   - Loaded jobs sit in the READY state until the CPU is free.
 *   - The CPU runs one job at a time and switches away after a time quantum
 *     (Round Robin), so every loaded job makes progress.
 *   - A job can leave the CPU to WAIT for a simulated I/O operation and later
 *     come back to READY - exactly the state cycle every OS textbook draws:
 *
 *        JOB QUEUE --(loaded, memory found)--> READY --(CPU)--> RUNNING
 *           ^                                     ^               |
 *           |                                     +---(quantum)---+
 *           |                                     |               |
 *           +--(terminated, memory freed)---- TERMINATED   WAITING (I/O)
 *
 * SIMPLIFICATIONS (stated honestly - this is a teaching model, not a real OS):
 *   - Jobs are loaded CONTIGUOUSLY with first-fit; there is no paging or
 *     segmentation inside this simulation.
 *   - All jobs "arrive" at time 0, in the order entered.
 *   - There is exactly one CPU and one idealized I/O device. Each job performs
 *     ONE I/O operation at the halfway point of its burst, lasting
 *     IO_DURATION ticks. The device never fails and never has to queue
 *     several jobs at once (jobs overlap waiting trivially because each does
 *     I/O only once).
 *   - A "tick" is an abstract time unit, not a real millisecond.
 *   - When two things could happen in the same tick, this order is used:
 *     (1) load jobs into memory, (2) give the CPU to a ready job,
 *     (3) progress I/O, (4) run the current job for one tick.
 */
public class MultiprogrammingSimulator {

    private static final int IO_DURATION = 3;   // ticks of the one simulated I/O per job
    private static final int MAX_TICKS = 10000; // safety belt: stop if something goes wrong

    /** Our tiny "job control block". */
    private static class Job {
        String name;
        int mem;          // kilobytes of RAM this job needs while loaded
        int burst;        // total CPU ticks the job needs
        int executed = 0; // CPU ticks received so far
        int quantumUsed = 0; // CPU ticks since the job last got the CPU
        int remainingIo = 0; // ticks left of I/O (0 = not doing I/O now)
        boolean ioDone = false;
        String state = "JOB QUEUE"; // JOB QUEUE | READY | RUNNING | WAITING (I/O) | TERMINATED
        int loadedAt = -1;
        int finishedAt = -1;

        Job(String name, int mem, int burst) {
            this.name = name;
            this.mem = mem;
            this.burst = burst;
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
            jobs.add(new Job(name, size, burst));
        }

        int quantum = in.readIntInRange("Time quantum for the CPU (ticks): ", 1, 100);

        System.out.println();
        System.out.println("Simulation rules:");
        System.out.println("  - first-fit contiguous loading, memory = " + memSize + " KB");
        System.out.println("  - Round Robin on the CPU, quantum = " + quantum + " tick(s)");
        System.out.println("  - each job does ONE I/O of " + IO_DURATION
                + " ticks at the halfway point of its burst");
        System.out.println();
        System.out.println("Tick | State after the tick:  CPU | READY (queue order) | WAITING | JOB QUEUE | memory");
        System.out.println("-----+--------------------------------------------------------------+-----------------------");

        // ---- initial state: every job is waiting for memory -----------------
        List<Job> jobQueue = new ArrayList<>(jobs); // not yet loaded
        List<Job> ready = new ArrayList<>();        // loaded, waiting for the CPU
        List<Alloc> memory = new ArrayList<>();     // occupied chunks, kept sorted
        Job current = null;                         // the job using the CPU (null = CPU idle)
        int time = 0;
        int terminated = 0;

        while (terminated < jobs.size() && time < MAX_TICKS) {
            List<String> events = new ArrayList<>();

            // (1) LOADER: try to load waiting jobs (in order) while space fits.
            for (int i = 0; i < jobQueue.size(); ) {
                Job j = jobQueue.get(i);
                int hole = firstFit(memory, j.mem, memSize);
                if (hole >= 0) {
                    insertSorted(memory, new Alloc(j.name, hole, j.mem));
                    jobQueue.remove(i);
                    j.state = "READY";
                    j.loadedAt = time;
                    ready.add(j);
                    events.add(j.name + " LOADED (" + j.mem + " KB at " + hole + ")");
                } else {
                    i++; // no room yet - this job keeps waiting for memory
                }
            }

            // (2) DISPATCHER: if the CPU is idle, give it to the first ready job.
            if (current == null && !ready.isEmpty()) {
                current = ready.remove(0);
                current.state = "RUNNING";
                current.quantumUsed = 0; // fresh time slice
                events.add(current.name + " -> RUNNING");
            }

            // (3) I/O: one tick passes for every job that was already doing I/O.
            for (Job j : jobs) {
                if ("WAITING (I/O)".equals(j.state)) {
                    j.remainingIo--;
                    if (j.remainingIo == 0) {
                        j.state = "READY";
                        ready.add(j);
                        events.add(j.name + " I/O finished -> READY");
                    }
                }
            }

            // (4) CPU: the current job runs for exactly one tick.
            // Remember WHO is using the CPU during this tick — the job may
            // finish/yield at the end of the tick, but it still used it.
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
                } else if (j.executed == j.ioPoint() && !j.ioDone) {
                    // Halfway point reached: this job goes to the I/O device.
                    j.ioDone = true;
                    j.remainingIo = IO_DURATION;
                    j.state = "WAITING (I/O)";
                    events.add(j.name + " starts I/O (" + IO_DURATION + " ticks)");
                    current = null;
                } else if (j.quantumUsed >= quantum) {
                    // Out of time slice: back of the ready queue.
                    j.state = "READY";
                    ready.add(j);
                    events.add(j.name + " quantum expired -> READY");
                    current = null;
                }
            }

            // ---- print this tick's snapshot --------------------------------
            String snap = snapshot(runningThisTick, ready, jobs, memory);
            String evts = events.isEmpty() ? "-" : join(events);
            System.out.printf("%-4d | %s | %s%n", time, snap, evts);
            time++;
        }

        if (terminated < jobs.size()) {
            System.out.println("[!] Simulation stopped at the safety limit (" + MAX_TICKS
                    + " ticks). This should not happen - please report the input.");
            return;
        }

        // ---- final report ---------------------------------------------------
        System.out.println();
        System.out.println("All jobs finished at tick " + time + ".");
        System.out.println();
        System.out.printf("%-6s %8s %6s %9s %10s %11s%n",
                "Job", "Mem(KB)", "Burst", "Loaded at", "Finished at", "Turnaround");
        System.out.println("------ -------- ------ --------- ---------- -----------");
        for (Job j : jobs) {
            System.out.printf("%-6s %8d %6d %9d %10d %11d%n",
                    j.name, j.mem, j.burst, j.loadedAt, j.finishedAt, j.finishedAt);
        }
        System.out.println();
        System.out.println("Note: 'Loaded at' is how long the job waited in the job queue");
        System.out.println("for memory; turnaround = finish time (all jobs arrived at 0).");
    }

    // ------------------------------------------------------------------
    // Snapshot helpers - what the user sees in the trace table
    // ------------------------------------------------------------------

    /** One-line description of every job's state at the END of this tick. */
    private static String snapshot(Job runningThisTick, List<Job> ready, List<Job> jobs,
                                   List<Alloc> memory) {
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
        for (Job j : jobs) { // original job order (TERMINATED jobs are not listed)
            StringBuilder bucket = null;
            String extra = "";
            if ("WAITING (I/O)".equals(j.state)) {
                bucket = waiting;
                extra = " (" + j.remainingIo + ")";
            } else if ("JOB QUEUE".equals(j.state)) {
                bucket = queued;
            }
            if (bucket != null) {
                if (bucket.length() > 0) {
                    bucket.append(", ");
                }
                bucket.append(j.name).append(extra);
            }
        }

        StringBuilder mem = new StringBuilder();
        for (Alloc a : memory) {
            if (mem.length() > 0) {
                mem.append(", ");
            }
            mem.append(a.name);
        }

        return "CPU: " + cpu
                + " | READY: " + (readyList.length() == 0 ? "-" : readyList)
                + " | WAITING: " + (waiting.length() == 0 ? "-" : waiting)
                + " | QUEUE: " + (queued.length() == 0 ? "-" : queued)
                + " | MEM: " + (mem.length() == 0 ? "-" : mem);
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
