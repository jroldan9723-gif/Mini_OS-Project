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
 *   - The RAM size is entered first so every segment can be checked while it
 *     is entered: its whole range must fit in RAM and must not overlap a
 *     segment that is already in the table (the program re-asks otherwise).
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
        int ramSize = in.readIntInRange("Physical memory (RAM) size in bytes: ", 1, 1_000_000_000);

        int[] base = new int[n];
        int[] limit = new int[n];
        for (int s = 0; s < n; s++) {
            // Ask again until this segment is valid: basic values, whole range
            // inside RAM, and no overlap with an already entered segment.
            while (true) {
                base[s] = in.readInt("  Base address of segment " + s + ": ");
                limit[s] = in.readInt("  Limit (size) of segment " + s + ": ");

                if (base[s] < 0 || limit[s] <= 0) {
                    System.out.println("  [!] Base must be >= 0 and limit must be > 0. Try again.");
                    continue;
                }
                // long so base + limit cannot overflow int
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
                break; // this segment is valid
            }
        }

        // Show the segment table back to the user.
        System.out.println();
        System.out.println("Segment table in use:");
        System.out.println("  Segment |   Base | Limit | Ends at");
        System.out.println("  --------|--------|-------|--------");
        for (int s = 0; s < n; s++) {
            System.out.printf("  %7d | %6d | %5d | %7d%n",
                    s, base[s], limit[s], (long) base[s] + limit[s] - 1);
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

            long physical = (long) base[seg] + offset; // long so base + offset cannot overflow
            if (physical >= ramSize) {
                System.out.println("  " + seg + ":" + offset + " -> SEGMENTATION FAULT: physical address "
                        + physical + " is outside RAM (RAM = " + ramSize + " bytes).");
                continue;
            }
            System.out.println("  " + seg + ":" + offset + " -> base " + base[seg] + " + offset "
                    + offset + " = physical address " + physical);
        }
    }

    /**
     * Returns the index of an already entered segment whose range [base, base+limit)
     * overlaps the candidate range, or -1 if there is no overlap. Segments 0..count-1
     * are the ones already in the table.
     */
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
