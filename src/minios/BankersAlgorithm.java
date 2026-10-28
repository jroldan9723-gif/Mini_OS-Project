// File: src/minios/BankersAlgorithm.java
package minios;

import java.util.ArrayList;
import java.util.List;

/**
 * BankersAlgorithm - checks whether the system is in a SAFE state.
 *
 * THE IDEA IN PLAIN ENGLISH
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
 * ASSUMPTIONS
 *   - Processes are labelled P1..Pn and resources R1..Rm (1-based labels).
 *   - All numbers are non-negative, and Max >= Allocation for every cell.
 *   - A process releases ALL its resources when it finishes (classic model).
 */
public class BankersAlgorithm {

    /** Shows the feature submenu until the user goes back. */
    public static void run(InputHelper in) {
        System.out.println("BANKER'S ALGORITHM (deadlock avoidance)");
        while (true) {
            System.out.println();
            System.out.println("  1) Enter data and check for a safe state");
            System.out.println("  0) Back to main menu");
            int choice = in.readIntInRange("  Choose an option: ", 0, 1);
            if (choice == 0) {
                return;
            }
            System.out.println();
            runOnce(in);
        }
    }

    /** Reads one full problem instance and prints the safety check. */
    private static void runOnce(InputHelper in) {
        int n;
        int m;
        int[][] allocation;
        int[][] max;
        int[] available;

        // Keep re-reading until the numbers are consistent, so a mistake
        // in one row does not force restarting the whole program.
        while (true) {
            n = in.readIntInRange("Number of processes: ", 1, 10);
            m = in.readIntInRange("Number of resource types: ", 1, 10);

            System.out.println("Allocation matrix - one row per process, "
                    + m + " number(s) per row (space separated).");
            allocation = readMatrix(in, "Allocation", n, m);
            System.out.println("Maximum-need matrix - same format.");
            max = readMatrix(in, "Max", n, m);
            available = in.readIntArray("Available resources (" + m + " number(s)): ", m);

            String error = validate(allocation, max, available);
            if (error == null) {
                break;
            }
            System.out.println("[!] " + error);
            System.out.println("    Please enter the data again.\n");
        }

        // Need = Max - Allocation
        int[][] need = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                need[i][j] = max[i][j] - allocation[i][j];
            }
        }

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
