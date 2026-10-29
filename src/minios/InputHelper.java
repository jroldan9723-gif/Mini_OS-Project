// File: src/minios/InputHelper.java
package minios;

import java.util.Scanner;

/**
 * InputHelper - wrapper around Scanner for reading input safely.
 *
 * Scanner.nextInt() throws an exception when the user types "abc" instead of
 * a number, which would crash the program. So instead of using nextInt() we
 * always read a whole line and parse it ourselves; if the line is not valid
 * we simply ask again. That way a typo never crashes the simulator.
 */
public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads one whole line of text (without the trailing newline).
     * If the input ends unexpectedly (for example a piped test file finished),
     * the program says goodbye and exits politely instead of crashing.
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("No more input received. Goodbye!");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    /** Reads a whole number. Keeps asking until the user types a valid integer. */
    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt).trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] \"" + line + "\" is not a whole number. Please try again.");
            }
        }
    }

    /** Reads an integer that must be between min and max (inclusive). */
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  [!] Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    /** Reads exactly "count" whole numbers separated by spaces on ONE line. */
    public int[] readIntArray(String prompt, int count) {
        while (true) {
            String line = readLine(prompt).trim();
            if (line.isEmpty()) {
                System.out.println("  [!] Nothing was typed. Please enter " + count
                        + " whole number(s) separated by spaces.");
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != count) {
                System.out.println("  [!] Expected exactly " + count + " number(s) but saw "
                        + parts.length + ". Please try again.");
                continue;
            }
            int[] values = new int[count];
            boolean ok = true;
            for (int i = 0; i < count; i++) {
                try {
                    values[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    System.out.println("  [!] \"" + parts[i] + "\" is not a whole number. Please try again.");
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return values;
            }
        }
    }

    /** Asks a yes/no question. Keeps asking until the answer starts with 'y' or 'n'. */
    public boolean readYesNo(String prompt) {
        while (true) {
            String line = readLine(prompt).trim().toLowerCase();
            if (line.startsWith("y")) {
                return true;
            }
            if (line.startsWith("n")) {
                return false;
            }
            System.out.println("  [!] Please answer with y (yes) or n (no).");
        }
    }
}
