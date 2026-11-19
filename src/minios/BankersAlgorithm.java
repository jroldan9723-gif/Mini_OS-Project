// File: src/minios/BankersAlgorithm.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * BankersAlgorithm - checks whether the system is in a SAFE state.
 *
 * HOW IT WORKS
 *   Each process may eventually need up to Max[i] of every resource, but it
 *   currently HOLDS Allocation[i]. So it may still ASK for:
 *       Need[i] = Max[i] - Allocation[i]
 *
 *   The safety rule: pretend the currently available resources are free cash
 *   ("Work"). We may let a process finish ONLY if Need[i] <= Work for every
 *   resource. When it finishes, it returns everything it held:
 *       Work = Work + Allocation[i]
 *   If we can keep finding such a process until EVERY process has finished,
 *   the system is SAFE and the order we found is called a SAFE SEQUENCE.
 *   If we get stuck with processes that can never be satisfied, the system is
 *   UNSAFE (unsafe does not mean deadlock has happened - it means deadlock
 *   COULD happen and the OS must refuse the request).
 *
 *   The Resource Request Algorithm (option 2) applies the same rules to one
 *   request: the request is granted only if the state AFTER the tentative
 *   allocation is still safe; otherwise the allocation is undone.
 *
 * ASSUMPTIONS
 *   - Processes are labelled P1..Pn and resources R1..Rm (1-based labels).
 *   - All numbers are non-negative, and Max >= Allocation for every cell.
 *   - A process releases ALL its resources when it finishes (classic model).
 */
public class BankersAlgorithm {

    // The current system state, kept between menu choices so that option 2
    // (resource request) works on the data that option 1 loaded.
    private static boolean loaded = false;
    private static int curN;
    private static int curM;
    private static int[][] curAlloc;
    private static int[][] curMax;
    private static int[] curAvail;

    /** Shows the feature submenu until the user goes back. */
    public static void run(InputHelper in) {
        System.out.println("BANKER'S ALGORITHM (deadlock avoidance)");
        while (true) {
            System.out.println();
            System.out.println("  1) Enter data and check for a safe state");
            System.out.println("  2) Resource request (give a process more resources)");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 2);
            if (choice == 0) {
                return;
            }
            System.out.println();
            if (choice == 1) {
                runOnce(in);
            } else {
                requestMode(in);
            }
        }
    }

    /** Reads and validates one full problem instance into the current-state fields. */
    private static void loadData(InputHelper in) {
        // Keep re-reading until the numbers are consistent, so a mistake
        // in one row does not force restarting the whole program.
        while (true) {
            curN = in.readIntInRange("Number of processes: ", 1, 10);
            curM = in.readIntInRange("Number of resource types: ", 1, 10);

            System.out.println("Allocation matrix - one row per process, "
                    + curM + " number(s) per row (space separated).");
            curAlloc = readMatrix(in, "Allocation", curN, curM);
            System.out.println("Maximum-need matrix - same format.");
            curMax = readMatrix(in, "Max", curN, curM);
            curAvail = in.readIntArray("Available resources (" + curM + " number(s)): ", curM);

            String error = validate(curAlloc, curMax, curAvail);
            if (error == null) {
                break;
            }
            System.out.println("[!] " + error);
            System.out.println("    Please enter the data again.\n");
        }
        loaded = true;
    }

    /** Reads one full problem instance and prints the safety check. */
    private static void runOnce(InputHelper in) {
        loadData(in);
        int n = curN;
        int m = curM;
        int[][] allocation = curAlloc;
        int[][] max = curMax;
        int[] available = curAvail;

        // Need = Max - Allocation
        int[][] need = computeNeed(allocation, max);

        printMatrix("Need matrix (Max - Allocation)", need, m);

        // Run the safety algorithm, keeping a step-by-step log for the user.
        List<Integer> safeOrder = new ArrayList<>();
        StringBuilder log = new StringBuilder();
        boolean safe = checkSafe(need, allocation, available, safeOrder, log);

        System.out.println("Safety check:");
        System.out.println("  Work (available) starts at " + fmt(available));
        System.out.print(log);

        if (safe) {
            System.out.println();
            System.out.println("RESULT: SAFE STATE - the OS can satisfy every process in some order.");
            System.out.print("Safe sequence: ");
            for (int k = 0; k < safeOrder.size(); k++) {
                if (k > 0) {
                    System.out.print(" -> ");
                }
                System.out.print("P" + (safeOrder.get(k) + 1));
            }
            System.out.println();
        } else {
            // Report which processes could not be guaranteed to finish.
            StringBuilder stuck = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (!safeOrder.contains(i)) {
                    if (stuck.length() > 0) {
                        stuck.append(", ");
                    }
                    stuck.append("P").append(i + 1);
                }
            }
            System.out.println();
            System.out.println("RESULT: UNSAFE STATE - no safe sequence exists;");
            System.out.println("        a deadlock could occur if requests are granted.");
            System.out.println("Processes that could not be guaranteed to finish: " + stuck);
        }
    }

    // ------------------------------------------------------------------
    // Resource Request Algorithm (Dijkstra)
    // ------------------------------------------------------------------
    /**
     * Lets the user make a resource request from one process:
     *   1. request must exist, be non-negative, <= Need and <= Available
     *   2. pretend to allocate: Available -= request, Allocation += request
     *   3. run the SAME safety algorithm as option 1
     *   4. safe   -> keep the new values (request granted)
     *      unsafe -> undo the pretend allocation (request denied)
     */
    private static void requestMode(InputHelper in) {
        if (!loaded) {
            System.out.println("(No data yet - please enter the system data first.)");
            System.out.println();
            loadData(in);
        }
        int n = curN;
        int m = curM;

        int p = in.readIntInRange("Process making the request (1.." + n + "): ", 1, n);
        int[] request = in.readIntArray("Request for P" + p + " (" + m + " number(s)): ", m);
        int idx = p - 1;
        int[][] need = computeNeed(curAlloc, curMax);

        System.out.println();
        System.out.println("Request from P" + p + ": " + fmt(request));
        System.out.println("Remaining need of P" + p + ": " + fmt(need[idx]));
        System.out.println("Available right now        : " + fmt(curAvail));

        // --- validation: none of these checks may change any value ---
        for (int j = 0; j < m; j++) {
            if (request[j] < 0) {
                System.out.println("Request is INVALID: resource R" + (j + 1) + " is negative.");
                System.out.println("System state unchanged.");
                return;
            }
        }
        for (int j = 0; j < m; j++) {
            if (request[j] > need[idx][j]) {
                System.out.println("Request is INVALID: it exceeds the remaining need of P"
                        + p + " (" + fmt(need[idx]) + ").");
                System.out.println("System state unchanged.");
                return;
            }
        }
        for (int j = 0; j < m; j++) {
            if (request[j] > curAvail[j]) {
                System.out.println("Request is VALID but the resources are NOT available"
                        + " (R" + (j + 1) + " needs " + request[j] + ", only "
                        + curAvail[j] + " free).");
                System.out.println("System state unchanged.");
                return;
            }
        }
        System.out.println("Request is valid and the resources are available.");

        // --- pretend to allocate, then ask the safety algorithm ---
        for (int j = 0; j < m; j++) {
            curAvail[j] -= request[j];
            curAlloc[idx][j] += request[j];
        }

        List<Integer> safeOrder = new ArrayList<>();
        StringBuilder log = new StringBuilder();
        boolean safe = checkSafe(computeNeed(curAlloc, curMax), curAlloc, curAvail, safeOrder, log);

        System.out.println();
        System.out.println("Safety check after granting the request:");
        if (log.length() == 0) {
            System.out.println("  (no remaining process fits in the tentative work - nothing can run)");
        }
        System.out.print(log);

        if (safe) {
            System.out.println("Result: SAFE - the request is GRANTED.");
            System.out.println();
            System.out.println("Available now: " + fmt(curAvail));
            printMatrix("Updated allocation", curAlloc, m);
            printMatrix("Updated need (Max - Allocation)", computeNeed(curAlloc, curMax), m);
        } else {
            // Undo the pretend allocation - nothing may change on denial.
            for (int j = 0; j < m; j++) {
                curAvail[j] += request[j];
                curAlloc[idx][j] -= request[j];
            }
            System.out.println("Result: UNSAFE - the request is DENIED and the");
            System.out.println("        original Available / Allocation values are restored.");
            System.out.println("Available still: " + fmt(curAvail));
        }
    }

    /** Need = Max - Allocation. */
    private static int[][] computeNeed(int[][] allocation, int[][] max) {
        int[][] need = new int[allocation.length][max[0].length];
        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[0].length; j++) {
                need[i][j] = max[i][j] - allocation[i][j];
            }
        }
        return need;
    }

    /**
     * The safety algorithm itself.
     *
     * @return true  if every process can finish (safe sequence stored in safeOrder)
     *         false if we get stuck (safeOrder then lists the processes that
     *               could be finished before getting stuck)
     */
    private static boolean checkSafe(int[][] need, int[][] allocation, int[] available,
                                     List<Integer> safeOrder, StringBuilder log) {
        int n = allocation.length;
        int m = available.length;
        int[] work = available.clone();   // resources that become free as processes finish
        boolean[] finished = new boolean[n];
        int done = 0;

        while (done < n) {
            // Find the first unfinished process whose Need fits inside Work.
            int pick = -1;
            for (int i = 0; i < n; i++) {
                if (finished[i]) {
                    continue;
                }
                boolean fits = true;
                for (int j = 0; j < m; j++) {
                    if (need[i][j] > work[j]) {
                        fits = false;
                        break;
                    }
                }
                if (fits) {
                    pick = i;
                    break;
                }
            }

            if (pick == -1) {
                return false; // stuck: no remaining process can be satisfied
            }

            // Pretend this process runs and returns all its resources.
            finished[pick] = true;
            safeOrder.add(pick);
            done++;
            log.append("  P").append(pick + 1)
               .append(": need ").append(fmt(need[pick]))
               .append(" <= work ").append(fmt(work))
               .append("  -> runs, returns ").append(fmt(allocation[pick]));
            for (int j = 0; j < m; j++) {
                work[j] += allocation[pick][j];
            }
            log.append(", work = ").append(fmt(work)).append('\n');
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Input helpers / validation
    // ------------------------------------------------------------------
    private static int[][] readMatrix(InputHelper in, String label, int n, int m) {
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            matrix[i] = in.readIntArray("  " + label + " for P" + (i + 1) + " (" + m + " number(s)): ", m);
        }
        return matrix;
    }

    /** Returns null when the data is valid, otherwise a message explaining the problem. */
    private static String validate(int[][] allocation, int[][] max, int[] available) {
        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[0].length; j++) {
                if (allocation[i][j] < 0) {
                    return "Allocation for P" + (i + 1) + " contains a negative number.";
                }
                if (max[i][j] < 0) {
                    return "Max need for P" + (i + 1) + " contains a negative number.";
                }
                if (max[i][j] < allocation[i][j]) {
                    return "P" + (i + 1) + " already holds " + allocation[i][j]
                            + " of R" + (j + 1) + " but its max need is only " + max[i][j]
                            + " (max must be >= allocation).";
                }
            }
        }
        for (int j = 0; j < available.length; j++) {
            if (available[j] < 0) {
                return "Available count for R" + (j + 1) + " is negative.";
            }
        }
        return null; // valid
    }

    private static void printMatrix(String title, int[][] matrix, int m) {
        System.out.println(title + ":");
        System.out.print("       ");
        for (int j = 0; j < m; j++) {
            System.out.printf("%6s", "R" + (j + 1));
        }
        System.out.println();
        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("   P%d  ", i + 1);
            for (int j = 0; j < m; j++) {
                System.out.printf("%6d", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    /** Formats a vector as "[1,2,2]" for the step-by-step log. */
    private static String fmt(int[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(values[i]);
        }
        return sb.append("]").toString();
    }
}
