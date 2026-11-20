package minios.gui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

// Helpers for the form fields and the input checks, so every tab validates
// numbers the same way.
public final class Ui {

    public static class InputError extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputError(String message) {
            super(message);
        }
    }

    public static JTextField field(int columns) {
        return new JTextField(columns);
    }

    public static JTextArea area(int rows, int columns) {
        JTextArea a = new JTextArea(rows, columns);
        a.setLineWrap(false);
        a.setTabSize(4);
        return a;
    }

    public static JTextArea outputArea() {
        JTextArea out = new JTextArea(18, 90);
        out.setEditable(false);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        return out;
    }

    public static JScrollPane scrolled(JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(area.getPreferredSize());
        return pane;
    }

    public static int parseInt(String what, String text, int min, int max) {
        String t = text == null ? "" : text.trim();
        try {
            int value = Integer.parseInt(t);
            if (value < min || value > max) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            if (t.isEmpty()) {
                throw new InputError(what + ": nothing was entered. " + rangeHint(min, max));
            }
            throw new InputError(what + ": \"" + t + "\" is not valid. " + rangeHint(min, max));
        }
    }

    public static int[] parseIntList(String what, String text) {
        return parseIntList(what, text, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int[] parseIntList(String what, String text, int min, int max) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new InputError(what + ": nothing was entered. Type at least one whole number.");
        }
        String[] parts = t.split("\\s+");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                values[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                throw new InputError(what + ": \"" + parts[i] + "\" is not a whole number.");
            }
            if (values[i] < min || values[i] > max) {
                throw new InputError(what + ": " + values[i] + " is out of range. "
                        + rangeHint(min, max) + ".");
            }
        }
        return values;
    }

    public static String[] lines(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0];
        }
        String[] raw = text.trim().split("\\r?\\n");
        java.util.ArrayList<String> kept = new java.util.ArrayList<>();
        for (String line : raw) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                kept.add(trimmed);
            }
        }
        return kept.toArray(new String[0]);
    }

    private static String rangeHint(int min, int max) {
        if (max == Integer.MAX_VALUE) {
            return "Enter a whole number of at least " + min + ".";
        }
        if (min == Integer.MIN_VALUE) {
            return "Enter a whole number of at most " + max + ".";
        }
        return "Enter a whole number between " + min + " and " + max + ".";
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Check the input", JOptionPane.WARNING_MESSAGE);
    }

    private Ui() {
    }
}
