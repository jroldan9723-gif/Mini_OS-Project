package minios;

import java.util.ArrayList;
import java.util.List;

// Banker's algorithm (deadlock avoidance): the OS pretends to lend every
// requested resource, checks whether every process could still finish in
// some order (a safe state), and takes the loan back if not.
//
// The last entered data set stays in memory so that follow-up requests can
// run against it, exactly like a real session at the console.
public class BankersAlgorithm {

    // Data of the last successful entry (allocation, max, available).
    private static boolean loaded = false;
    private static int curN;
    private static int curM;
    private static int[][] curAlloc;
    private static int[][] curMax;
    private static int[] curAvail;

    // Menu: enter data and check for a safe state, or make a resource
    // request for one process.
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

    // Reads n, m, the allocation matrix, the max matrix and Available.
    // If validation fails (negative numbers, max < allocation) the whole
    // data set is read again, so only consistent data reaches the check.
    private static void loadData(InputHelper in) {

        while (true) {
            curN = in.readIntAtLeast("Number of processes: ", 1);
            curM = in.readIntAtLeast("Number of resource types: ", 1);

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

    // Safety check: Need = Max - Allocation, then try to find an order in
    // which every process can finish. Prints the safe sequence, or which
    // processes could not be guaranteed to finish.
    private static void runOnce(InputHelper in) {
        loadData(in);
        int n = curN;
        int m = curM;
        int[][] allocation = curAlloc;
        int[][] max = curMax;
        int[] available = curAvail;

        int[][] need = computeNeed(allocation, max);

        printMatrix("Need matrix (Max - Allocation)", need, m);

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

    // Resource request algorithm: first the three validity checks
    // (negative, exceeds need, exceeds Available), then a tentative
    // allocation. The safety test runs on the tentative state; on success
    // the request is kept, otherwise Allocation and Available are restored.
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

            for (int j = 0; j < m; j++) {
                curAvail[j] += request[j];
                curAlloc[idx][j] -= request[j];
            }
            System.out.println("Result: UNSAFE - the request is DENIED and the");
            System.out.println("        original Available / Allocation values are restored.");
            System.out.println("Available still: " + fmt(curAvail));
        }
    }

    // Need[i][j] = how much more process i may still request of resource j.
    private static int[][] computeNeed(int[][] allocation, int[][] max) {
        int[][] need = new int[allocation.length][max[0].length];
        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[0].length; j++) {
                need[i][j] = max[i][j] - allocation[i][j];
            }
        }
        return need;
    }

    // The safety algorithm itself: repeatedly look for a process whose whole
    // Need fits in Work (a copy of Available). If it fits, pretend it runs to
    // completion and returns its Allocation to Work. If every unfinished
    // process is blocked, the state is unsafe. The log records every step
    // so the user can follow the reasoning line by line.
    private static boolean checkSafe(int[][] need, int[][] allocation, int[] available,
                                     List<Integer> safeOrder, StringBuilder log) {
        int n = allocation.length;
        int m = available.length;
        int[] work = available.clone();
        boolean[] finished = new boolean[n];
        int done = 0;

        while (done < n) {

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
                return false;
            }

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

    private static int[][] readMatrix(InputHelper in, String label, int n, int m) {
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            matrix[i] = in.readIntArray("  " + label + " for P" + (i + 1) + " (" + m + " number(s)): ", m);
        }
        return matrix;
    }

    // Basic sanity of the entered data: no negative counts and no max that
    // is smaller than what the process already holds. Returns an error
    // message, or null when everything is consistent.
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
        return null;
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
