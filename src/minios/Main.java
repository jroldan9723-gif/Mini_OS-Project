// File: src/minios/Main.java
package minios;

import java.util.Scanner;

/**
 * Main - the entry point of the Mini OS Simulator.
 *
 * This program is an EDUCATIONAL DEMONSTRATION of selected operating-system
 * concepts (CPU scheduling, deadlock avoidance, memory management, disk
 * scheduling...). It is NOT a real operating system: it does not touch real
 * hardware, real memory, or real processes - it only simulates the arithmetic
 * and bookkeeping that a real OS would do.
 *
 * STRUCTURE OF THE PROGRAM
 *   Main  -> shows the main menu and starts whichever feature you pick.
 *   Each feature lives in its own class (CPUScheduler, BankersAlgorithm, ...)
 *   and shows a small submenu of its own. All input is done through
 *   InputHelper, which never lets a typo crash the program.
 *
 * HOW TO RUN IT (Eclipse)
 *   1. File > Import > General > Existing Projects into Workspace > Next
 *   2. Select this folder (Mini_OS-Project) as the root and press Finish
 *   3. Open src/minios/Main.java and press Ctrl+F11 (Run)
 *
 * HOW TO RUN IT (command line, from the project folder)
 *   javac -d bin src/minios/*.java
 *   java -cp bin minios.Main
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputHelper in = new InputHelper(scanner);

        System.out.println("=====================================================");
        System.out.println("   MINI OS SIMULATOR  --  educational demonstration");
        System.out.println("   (This is NOT a real operating system.)");
        System.out.println("=====================================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = in.readIntInRange("Your choice: ", 0, 8);
            System.out.println();

            switch (choice) {
                case 1: CPUScheduler.run(in);            break;
                case 2: BankersAlgorithm.run(in);         break;
                case 3: PagingSimulator.run(in);          break;
                case 4: SegmentationSimulator.run(in);    break;
                case 5: FragmentationDemo.run(in);        break;
                case 6: PageReplacementSimulator.run(in); break;
                case 7: DiskScheduler.run(in);            break;
                case 8: MultiprogrammingSimulator.run(in); break;
                case 0: running = false;                  break;
                default: break; // cannot happen: readIntInRange already limits 0..8
            }
            System.out.println();
        }

        System.out.println("Goodbye!");
    }

    /** Prints the main menu. */
    private static void printMenu() {
        System.out.println("---------------- MAIN MENU --------------------");
        System.out.println("  1) CPU scheduling   (FCFS, SJF, Round Robin)");
        System.out.println("  2) Banker's algorithm (deadlock avoidance)");
        System.out.println("  3) Paging           (logical -> physical)");
        System.out.println("  4) Segmentation     (logical -> physical)");
        System.out.println("  5) Fragmentation    (internal & external)");
        System.out.println("  6) Page replacement  (FIFO, LRU, Optimal)");
        System.out.println("  7) Disk scheduling  (FCFS, SSTF, SCAN, C-SCAN)");
        System.out.println("  8) Multiprogramming (jobs sharing CPU+memory)");
        System.out.println("  0) Exit");
        System.out.println("----------------------------------------------");
    }
}
