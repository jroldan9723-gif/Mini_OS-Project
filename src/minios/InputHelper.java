package minios;

import java.util.Scanner;

// Reads what the user types and checks it. Every prompt goes through here,
// so bad input always gets the same treatment: explain, then ask again.
public class InputHelper {

    public static class InputEndedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputEndedException(String message) {
            super(message);
        }
    }

    // The GUI switches this on so prompts and menus stay out of its output box.
    private static boolean quiet;

    public static void setQuiet(boolean value) {
        quiet = value;
    }

    public static boolean isQuiet() {
        return quiet;
    }

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        if (!quiet) {
            System.out.print(prompt);
        }
        if (!scanner.hasNextLine()) {
            if (!quiet) {
                System.out.println();
            }
            throw new InputEndedException("end of input reached");
        }
        return scanner.nextLine();
    }

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

    // For counts of items. Only a minimum, because the number of processes or
    // requests should not be limited by us.
    public int readIntAtLeast(String prompt, int min) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min) {
                return value;
            }
            System.out.println("  [!] Please enter a whole number of at least " + min + ".");
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  [!] Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    // Reads a whole line and splits it, for things like a reference string.
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
