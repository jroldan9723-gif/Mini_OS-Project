// File: src/minios/PagingSimulator.java
package minios;

/**
 * PagingSimulator - shows how a logical (virtual) address is translated into
 * a physical address using a PAGE TABLE.
 *
 * THE TRANSLATION (the classic formula):
 *     page number   = logicalAddress / pageSize     (integer division)
 *     offset        = logicalAddress % pageSize     (the remainder)
 *     physical      = frameNumber * pageSize + offset
 *
 * If the chosen page is not currently in RAM (frame number = -1 in our page
 * table) we report a PAGE FAULT - in a real OS the kernel would then load the
 * page from disk. If the address is larger than the logical address space we
 * report an invalid address.
 *
 * ASSUMPTIONS
 *   - The logical address space has "numberOfPages" pages, numbered from 0.
 *   - The page table is entered manually (frame number per page).
 *   - Frame -1 means "page is on disk, not in RAM" (page fault).
 *   - Pages and frames are the same size (that is what makes paging simple).
 */
public class PagingSimulator {

    public static void run(InputHelper in) {
        System.out.println("PAGING - logical to physical address translation");
        while (true) {
            System.out.println();
            System.out.println("  1) Build a page table and translate addresses");
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
        int pageSize = in.readIntInRange("Page size (bytes, e.g. 1024): ", 1, 1_000_000);
        int numPages = in.readIntInRange("Number of pages in the logical address space: ", 1, 1000);
        int logicalSize = pageSize * numPages; // total bytes of logical memory

        System.out.println("Logical address space = " + numPages + " pages x "
                + pageSize + " bytes = " + logicalSize + " bytes (addresses 0.."
                + (logicalSize - 1) + ")");
        System.out.println("Enter the frame number for each page.");
        System.out.println("Use -1 for a page that is NOT in memory (it is on disk).");

        int[] frameOfPage = new int[numPages];
        for (int p = 0; p < numPages; p++) {
            int frame;
            while (true) {
                frame = in.readInt("  Frame for page " + p + " (-1 .. 100000): ");
                if (frame >= -1 && frame <= 100000) {
                    break;
                }
                System.out.println("  [!] Frame must be -1 or a non-negative number.");
            }
            frameOfPage[p] = frame;
        }

        // Show the page table back to the user (like the OS would keep it).
        System.out.println();
        System.out.println("Page table in use:");
        System.out.println("  Page | Frame");
        System.out.println("  -----|------");
        for (int p = 0; p < numPages; p++) {
            System.out.printf("  %4d | %5s%n", p, frameOfPage[p] == -1 ? "on disk" : String.valueOf(frameOfPage[p]));
        }
        System.out.println();

        // Query loop: translate addresses until the user types -1.
        while (true) {
            int addr = in.readInt("Logical address to translate (-1 to stop): ");
            if (addr == -1) {
                return;
            }
            if (addr < 0) {
                System.out.println("  [!] A logical address cannot be negative (the address was "
                        + addr + ").");
                continue;
            }
            if (addr >= logicalSize) {
                System.out.println("  " + addr + " -> INVALID ADDRESS: outside the logical address"
                        + " space (valid addresses are 0.." + (logicalSize - 1) + ").");
                continue;
            }

            int pageNo = addr / pageSize;
            int offset = addr % pageSize;

            if (frameOfPage[pageNo] < 0) {
                System.out.println("  " + addr + " -> page " + pageNo + ", offset " + offset
                        + " -> PAGE FAULT: page " + pageNo + " is not in memory;"
                        + " the OS would load it from disk first.");
                continue;
            }

            int frame = frameOfPage[pageNo];
            int physical = frame * pageSize + offset;
            System.out.println("  " + addr + " -> page " + pageNo + ", offset " + offset
                    + " -> frame " + frame + " -> physical address " + physical
                    + "   (" + frame + " x " + pageSize + " + " + offset + ")");
        }
    }
}
