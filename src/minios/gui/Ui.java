package minios.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;

// The shared look: tab strip, group boxes, flat fields and buttons, input
// checks. Every screen builds from these, so a change here changes all eight.
public final class Ui {

    public static class InputError extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputError(String message) {
            super(message);
        }
    }

    // A field whose width is chosen by the caller. Ui.field() caps the width
    // so a short number does not stretch, which is wrong for a column of
    // several numbers such as an allocation vector.
    public static JTextField field(int columns, int width) {
        JTextField f = field(columns);
        f.setPreferredSize(new java.awt.Dimension(width, 26));
        f.setMinimumSize(new java.awt.Dimension(width, 26));
        return f;
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(Theme.VALUE);
        f.setForeground(Theme.INK);
        f.setBackground(Color.WHITE);
        f.setBorder(fieldBorder(false));
        f.setMargin(new Insets(4, 6, 4, 6));
        // never narrower than a couple of digits, so a short value is readable
        f.setPreferredSize(new java.awt.Dimension(
                Math.max(64, f.getPreferredSize().width), 26));
        return f;
    }

    // Small text areas holding one number per line.
    public static JTextArea area(int rows, int columns) {
        JTextArea a = new JTextArea(rows, columns);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        a.setTabSize(4);
        a.setFont(Theme.VALUE);
        a.setForeground(Theme.INK);
        a.setBackground(Color.WHITE);
        a.setMargin(new Insets(4, 6, 4, 6));
        return a;
    }

    public static JScrollPane scrolled(JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        pane.getVerticalScrollBar().setUnitIncrement(14);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setPreferredSize(new java.awt.Dimension(120, area.getPreferredSize().height + 4));
        return pane;
    }

    // A single hairline that darkens slightly while the field has the cursor,
    // so it is obvious where typing will go.
    static AbstractBorder fieldBorder(final boolean focused) {
        return new AbstractBorder() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                g.setColor(c.hasFocus() ? Theme.ACCENT_LIGHT : Theme.BORDER);
                g.drawRect(x, y, w - 1, h - 1);
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets in) {
                in.set(1, 1, 1, 1);
                return in;
            }
        };
    }

    // Padding is compact on purpose: the parameter strip has to fit one line.
    // The stock combo box is tall and 3D; give it the same flat frame and
    // height as a text field so a line of controls lines up.
    public static void styleCombo(javax.swing.JComboBox<?> combo) {
        combo.setFont(Theme.VALUE);
        combo.setForeground(Theme.INK);
        combo.setBackground(Color.WHITE);
        combo.setBorder(fieldBorder(false));
        combo.setFocusable(false);
        java.awt.Dimension d = combo.getPreferredSize();
        combo.setPreferredSize(new java.awt.Dimension(Math.min(d.width, 300), 26));
    }

    public static JButton button(String text) {
        JButton b = new FlatButton(text, Theme.SURFACE, Theme.BORDER, Theme.BORDER);
        b.setFont(Theme.BUTTON);
        b.setForeground(Theme.INK);
        return b;
    }

    public static JButton primary(String text) {
        JButton b = new FlatButton(text, Theme.ACCENT, Theme.ACCENT_HOVER, Theme.ACCENT);
        b.setFont(Theme.BUTTON);
        b.setForeground(Color.WHITE);
        return b;
    }

    // A button drawn by hand: square corners, a one pixel frame, a darker fill
    // while pressed. The stock look and feel gradient is what makes a Swing
    // window look its age.
    static final class FlatButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color base;
        private final Color pressed;
        private final Color frame;

        FlatButton(String text, Color base, Color pressed, Color frame) {
            super(text);
            this.base = base;
            this.pressed = pressed;
            this.frame = frame;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(5, 12, 5, 12));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        @Override
        public Insets getInsets() {
            return new Insets(5, 12, 5, 12);
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            java.awt.FontMetrics fm = getFontMetrics(getFont());
            return new java.awt.Dimension(fm.stringWidth(getText()) + 26, 28);
        }

        @Override
        protected void paintComponent(Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            boolean down = getModel().isPressed();
            boolean over = getModel().isRollover();
            Color fill = down ? pressed : (over ? blend(base) : base);
            g2.setColor(fill);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (frame != null) {
                g2.setColor(frame == base ? Theme.ACCENT_HOVER : frame);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            java.awt.Color text = isEnabled() ? getForeground() : Theme.DISABLED;
            g2.setColor(text);
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        private static Color blend(Color c) {
            int r = (c.getRed() + 255) / 2;
            int g = (c.getGreen() + 255) / 2;
            int b = (c.getBlue() + 255) / 2;
            return new Color(r, g, b);
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

    // A plain dialog instead of the default message box, so a mistake looks
    // like the rest of the window.
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
