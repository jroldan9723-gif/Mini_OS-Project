package minios;

// Segmentation: every segment has a base and a limit, and an offset that
// reaches past the limit is a segmentation fault.
public class SegmentationSimulator {

    public static void run(InputHelper in) {
        if (!InputHelper.isQuiet()) {
            System.out.println("SEGMENTATION - logical to physical address translation");
        }
        while (true) {
            if (!InputHelper.isQuiet()) {
                System.out.println();
                System.out.println("  1) Build a segment table and translate addresses");
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
        int n = in.readIntAtLeast("How many segments? ", 1);
        int ramSize = in.readIntInRange("Physical memory (RAM) size in bytes: ", 1, 1_000_000_000);

        int[] base = new int[n];
        int[] limit = new int[n];
        for (int s = 0; s < n; s++) {

            while (true) {
                base[s] = in.readInt("  Base address of segment " + s + ": ");
                limit[s] = in.readInt("  Limit (size) of segment " + s + ": ");

                if (base[s] < 0 || limit[s] <= 0) {
                    System.out.println("  [!] Base must be >= 0 and limit must be > 0. Try again.");
                    continue;
                }

                long end = (long) base[s] + limit[s] - 1;
                if (end >= ramSize) {
                    System.out.println("  [!] Segment " + s + " ends at " + end
                            + ", which is outside RAM (" + ramSize
                            + " bytes). Enter this segment again.");
                    continue;
                }
                int overlap = overlapWith(base, limit, s, base[s], limit[s]);
                if (overlap >= 0) {
                    System.out.println("  [!] Segment " + s + " [" + base[s] + ".." + end
                            + "] overlaps segment " + overlap + " ["
                            + base[overlap] + ".." + ((long) base[overlap] + limit[overlap] - 1)
                            + "]. Enter this segment again.");
                    continue;
                }
                break;
            }
        }

        System.out.println();
        System.out.println("Segment table in use:");
        System.out.println("  Segment |   Base | Limit | Ends at");
        System.out.println("  --------|--------|-------|--------");
        for (int s = 0; s < n; s++) {
            System.out.printf("  %7d | %6d | %5d | %7d%n",
                    s, base[s], limit[s], (long) base[s] + limit[s] - 1);
        }
        System.out.println();

        while (true) {
            int seg = in.readInt("Segment number (-1 to stop): ");
            if (seg == -1) {
                return;
            }
            int offset = in.readInt("  Offset inside that segment: ");

            if (seg < 0 || seg >= n) {
                System.out.println("  " + seg + ":" + offset + " -> INVALID ADDRESS: segment "
                        + seg + " does not exist (valid segments are 0.." + (n - 1) + ").");
                continue;
            }
            if (offset < 0) {
                System.out.println("  " + seg + ":" + offset + " -> INVALID ADDRESS: offset is negative.");
                continue;
            }
            if (offset >= limit[seg]) {
                System.out.println("  " + seg + ":" + offset + " -> SEGMENTATION FAULT: offset "
                        + offset + " is past the end of segment " + seg + " (limit = " + limit[seg] + ").");
                continue;
            }

            long physical = (long) base[seg] + offset;
            if (physical >= ramSize) {
                System.out.println("  " + seg + ":" + offset + " -> SEGMENTATION FAULT: physical address "
                        + physical + " is outside RAM (RAM = " + ramSize + " bytes).");
                continue;
            }
            System.out.println("  " + seg + ":" + offset + " -> base " + base[seg] + " + offset "
                    + offset + " = physical address " + physical);
        }
    }

    // Two segments clash when neither one ends before the other begins.
    private static int overlapWith(int[] base, int[] limit, int count, int candBase, int candLimit) {
        long candEnd = (long) candBase + candLimit;
        for (int i = 0; i < count; i++) {
            long end = (long) base[i] + limit[i];
            if ((long) candBase < end && (long) base[i] < candEnd) {
                return i;
            }
        }
        return -1;
    }
}
