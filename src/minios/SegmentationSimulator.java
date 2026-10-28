// File: src/minios/SegmentationSimulator.java
package minios;

/**
 * SegmentationSimulator - shows how a logical address is translated with a
 * SEGMENT TABLE (segment number + base address + segment limit).
 *
 * THE TRANSLATION
 *   The programmer thinks of memory as named chunks (code, data, stack...).
 *   A logical address here is given as (segment number, offset):
 *
 *     1) If the segment number does not exist            -> invalid address.
 *     2) If offset >= limit of that segment              -> SEGMENTATION FAULT
 *        (the program tried to read/write past the end of its segment).
 *     3) Otherwise physical address = base + offset.
 *     4) Extra safety check: if base+offset is outside RAM -> SEGMENTATION FAULT.
 *
 * ASSUMPTIONS
 *   - The logical address is supplied as two numbers: segment number, offset.
 *     (Some books instead squeeze both into one number; the idea is the same.)
 *   - Segment tables are entered manually; segments do not overlap in RAM
 *     (we assume the OS loaded them properly).
 */
public class SegmentationSimulator {

    public static void run(InputHelper in) {
        System.out.println("SEGMENTATION - logical to physical address translation");
        while (true) {
            System.out.println();
            System.out.println("  1) Build a segment table and translate addresses");
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
        int n = in.readIntInRange("How many segments? ", 1, 20);

        int[] base = new int[n];
        int[] limit = new int[n];
        for (int s = 0; s < n; s++) {
            while (true) {
                base[s] = in.readInt("  Base address of segment " + s + ": ");
                limit[s] = in.readInt("  Limit (size) of segment " + s + ": ");
                if (base[s] >= 0 && limit[s] > 0) {
                    break;
                }
                System.out.println("  [!] Base must be >= 0 and limit must be > 0. Try again.");
            }
        }

        int ramSize = in.readIntInRange("Physical memory (RAM) size in bytes: ", 1, 1_000_000_000);

        // Show the segment table back to the user.
        System.out.println();
        System.out.println("Segment table in use:");
        System.out.println("  Segment |   Base | Limit | Ends at");
        System.out.println("  --------|--------|-------|--------");
        for (int s = 0; s < n; s++) {
            System.out.printf("  %7d | %6d | %5d | %7d%n", s, base[s], limit[s], base[s] + limit[s] - 1);
        }
        System.out.println();

        // Query loop.
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

            int physical = base[seg] + offset;
            if (physical >= ramSize) {
                System.out.println("  " + seg + ":" + offset + " -> SEGMENTATION FAULT: physical address "
                        + physical + " is outside RAM (RAM = " + ramSize + " bytes).");
                continue;
            }
            System.out.println("  " + seg + ":" + offset + " -> base " + base[seg] + " + offset "
                    + offset + " = physical address " + physical);
        }
    }
}
