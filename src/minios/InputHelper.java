package minios;

import java.util.Scanner;

// Reads and checks whatever the user types. Every read line is a prompt
// plus one value; invalid values are re-asked instead of crashing, which
// keeps both the console menu and the GUI scripts on the same rules.
public class InputHelper {

    // Thrown when the input stream ends while the program still needs data.
    // The console app catches it and exits cleanly; the GUI catches it too.
    public static class InputEndedException extends RuntimeException {
        public InputEndedException(String message) {
            super(message);
        }
    }

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("No more input received. Goodbye!");
            throw new InputEndedException("end of input reached");
        }
        return scanner.nextLine();
    }

    // Reads a whole number and tries again until it parses.
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

    // For counts of items: only a lower bound is enforced, so the user can
    // enter as many entries as they want (no arbitrary upper limit).
    public int readIntAtLeast(String prompt, int min) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min) {
                return value;
            }
            System.out.println("  [!] Please enter a whole number of at least " + min + ".");
        }
    }

    // Reads a whole number that must fall inside a fixed range
    // (used for menu choices and for bounded values like a burst time).
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  [!] Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    // Reads exactly count whole numbers from one line, e.g. "10 4 7".
    // Re-asks if the count or any of the numbers is wrong.
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
}
