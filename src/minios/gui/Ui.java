package minios.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

// Helpers for the form fields and the input checks, so every screen validates
// numbers the same way. The colours all come from Theme.
public final class Ui {

    public static class InputError extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputError(String message) {
            super(message);
        }
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(Theme.BODY);
        f.setForeground(Theme.INK);
        f.setBackground(Color.WHITE);
        f.setBorder(fieldBorder());
        f.setMargin(new Insets(5, 8, 5, 8));
        return f;
    }

    public static JTextArea area(int rows, int columns) {
        JTextArea a = new JTextArea(rows, 1);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        a.setTabSize(4);
        a.setFont(Theme.BODY);
        a.setForeground(Theme.INK);
        a.setBackground(Color.WHITE);
        a.setMargin(new Insets(4, 6, 4, 6));
        return a;
    }

    public static JTextArea outputArea() {
        JTextArea out = new JTextArea();
        out.setEditable(false);
        out.setFont(Theme.MONO);
        out.setForeground(Theme.INK);
        out.setBackground(Theme.CARD);
        out.setTabSize(4);
        return out;
    }

    // Only the height is fixed, so the box widens with the card instead of
    // leaving a gap on the right.
    public static JScrollPane scrolled(JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        pane.getVerticalScrollBar().setUnitIncrement(14);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setPreferredSize(new java.awt.Dimension(120, area.getPreferredSize().height + 4));
        return pane;
    }

    // A flat, single-pixel border. The default look and feel border is what
    // makes a Swing window look dated.
    static Border fieldBorder() {
        return BorderFactory.createLineBorder(Theme.LINE);
    }

    // The stock combo box is short and 3D; give it the same flat frame and
    // height as a text field so a row of controls lines up.
    public static void styleCombo(javax.swing.JComboBox<?> combo) {
        combo.setFont(Theme.BODY);
        combo.setForeground(Theme.INK);
        combo.setBackground(Color.WHITE);
        combo.setBorder(fieldBorder());
        combo.setPreferredSize(new java.awt.Dimension(200, 28));
        ((javax.swing.JComponent) combo).putClientProperty("JComboBox.isTableCellEditor", Boolean.FALSE);
    }

    // Puts a rounded tinted chip behind a label, used for the small status
    // text in the report header.
    static JComponent pill(final javax.swing.JLabel label) {
        return new JComponent() {
            private static final long serialVersionUID = 1L;
            {
                setLayout(new java.awt.BorderLayout());
                setBorder(new javax.swing.border.EmptyBorder(3, 8, 3, 8));
                add(label, java.awt.BorderLayout.CENTER);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.Color c = label.getForeground();
                g2.setColor(new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), 26));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
    }

    static JButton primary(String text) {
        return new FlatButton(text, Theme.ACCENT, Theme.ACCENT_DARK, Color.WHITE);
    }

    static JButton secondary(String text) {
        return new FlatButton(text, Theme.CARD, Theme.SOFT, Theme.ACCENT);
    }

    // A button drawn by hand: rounded corners, a darker shade while pressed,
    // no gradient and no 3D bevel.
    static final class FlatButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color base;
        private final Color pressed;
        private final Color textColour;

        FlatButton(String text, Color base, Color pressed, Color textColour) {
            super(text);
            this.base = base;
            this.pressed = pressed;
            this.textColour = textColour;
            setFont(Theme.BODY_BOLD);
            setForeground(textColour);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(7, 16, 7, 16));
            setBorder(new MatteBorder(1, 1, 1, 1,
                    base == Theme.CARD ? Theme.LINE : base));
        }

        @Override
        public Insets getInsets() {
            return new Insets(7, 16, 7, 16);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = base;
            if (getModel().isPressed()) {
                fill = pressed;
            } else if (getModel().isRollover() && base == Theme.CARD) {
                fill = Theme.SOFT;
            } else if (getModel().isRollover()) {
                fill = pressed;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            if (base == Theme.CARD) {
                g2.setColor(Theme.LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? textColour : Theme.DISABLED);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
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
                        + rangeHint(min, max));
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

    // A plain dialog instead of the default Swing message box, so an error
    // looks like the rest of the window.
    public static void showError(Component parent, String message) {
        JOptionPane pane = new JOptionPane(
                "<html><body style='width: 320px'>" + message + "</body></html>",
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.DEFAULT_OPTION);
        javax.swing.JDialog dialog = pane.createDialog(parent, "Check the input");
        dialog.setVisible(true);
        dialog.dispose();
    }

    private Ui() {
    }
}
