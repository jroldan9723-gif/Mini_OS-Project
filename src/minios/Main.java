package minios;

import java.util.Scanner;

// Entry point of the console version. Shows the main menu and hands the
// chosen module to its own class. The GUI version (minios.gui.GuiMain)
// runs the very same module classes, only the input method differs.
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputHelper in = new InputHelper(scanner);

        System.out.println("=====================================================");
        System.out.println("   MINI OS SIMULATOR  --  educational demonstration");
        System.out.println("   (This is NOT a real operating system.)");
        System.out.println("=====================================================");

        // Keep asking for a module until the user types 0.
        // If the input stream ends early, stop cleanly instead of crashing.
        boolean running = true;
        try {
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
                    default: break;
                }
                System.out.println();
            }
        } catch (InputHelper.InputEndedException e) {
            // The input stream ended in the middle of the program; stop cleanly.
        }

        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("---------------- MAIN MENU --------------------");
            System.out.println("  1) CPU scheduling   (FCFS, SJF, Round Robin, Priority)");
            System.out.println("  2) Banker's algorithm (deadlock avoidance)");
            System.out.println("  3) Paging           (logical -> physical)");
            System.out.println("  4) Segmentation     (logical -> physical)");
            System.out.println("  5) Fragmentation    (internal & external)");
            System.out.println("  6) Page replacement  (FIFO, LRU, Optimal, LFU)");
            System.out.println("  7) Disk scheduling  (FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK)");
        System.out.println("  8) Multiprogramming (jobs sharing CPU+memory)");
        System.out.println("  0) Exit");
        System.out.println("----------------------------------------------");
    }
}
