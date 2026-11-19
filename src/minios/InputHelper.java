package minios;

import java.util.Scanner;

public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("No more input received. Goodbye!");
            System.exit(0);
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

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  [!] Please enter a whole number between " + min + " and " + max + ".");
        }
    }

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
