package minios;

// Paging: splits the logical address space into fixed-size pages that map
// onto frames of the same size. A logical address becomes page number
// (address / pageSize) plus offset (address % pageSize), and the page
// table translates the page number into a frame number.
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

    // One session: page size and page table are entered first (long
    // arithmetic keeps large address spaces from overflowing), then every
    // typed address is translated until the user enters -1.
    private static void runOnce(InputHelper in) {
        int pageSize = in.readIntInRange("Page size (bytes, e.g. 1024): ", 1, 1_000_000);
        int numPages = in.readIntAtLeast("Number of pages in the logical address space: ", 1);

        long logicalSize = (long) pageSize * numPages;

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

        System.out.println();
        System.out.println("Page table in use:");
        System.out.println("  Page | Frame");
        System.out.println("  -----|------");
        for (int p = 0; p < numPages; p++) {
            System.out.printf("  %4d | %5s%n", p, frameOfPage[p] == -1 ? "on disk" : String.valueOf(frameOfPage[p]));
        }
        System.out.println();

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
            long physical = (long) frame * pageSize + offset;
            System.out.println("  " + addr + " -> page " + pageNo + ", offset " + offset
                    + " -> frame " + frame + " -> physical address " + physical
                    + "   (" + frame + " x " + pageSize + " + " + offset + ")");
        }
    }
}
