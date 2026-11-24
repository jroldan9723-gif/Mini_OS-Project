package minios.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

// Colours and fonts for the whole window, kept in one place so every panel
// matches. Nothing here depends on the operating system look and feel.
final class Theme {

    // Backgrounds and lines
    static final Color PAGE = new Color(0xF4, 0xF6, 0xF9);
    static final Color CARD = Color.WHITE;
    static final Color LINE = new Color(0xDD, 0xE3, 0xEA);
    static final Color SOFT = new Color(0xF1, 0xF4, 0xF8);

    // Text
    static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    static final Color DISABLED = new Color(0xA3, 0xAA, 0xB5);

    // One accent colour, used for the selected item and the main button
    static final Color ACCENT = new Color(0x1F, 0x4E, 0x79);
    static final Color ACCENT_DARK = new Color(0x17, 0x3B, 0x5C);
    static final Color ACCENT_SOFT = new Color(0xE7, 0xEE, 0xF6);

    static final Color OK = new Color(0x15, 0x6B, 0x3A);
    static final Color WARN = new Color(0xA1, 0x5A, 0x08);

    private static final String FAMILY = pickFamily();

    static final Font APP = new Font(FAMILY, Font.BOLD, 18);
    static final Font APP_SUB = new Font(FAMILY, Font.PLAIN, 12);
    static final Font SECTION = new Font(FAMILY, Font.BOLD, 14);
    static final Font BODY = new Font(FAMILY, Font.PLAIN, 12);
    static final Font BODY_BOLD = new Font(FAMILY, Font.BOLD, 12);
    static final Font SMALL = new Font(FAMILY, Font.PLAIN, 11);
    static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 11);

    // Segoe UI on Windows, then whatever the system offers. Asking the
    // GraphicsEnvironment avoids a Font lookup that silently falls back.
    private static String pickFamily() {
        String[] candidates = {"Segoe UI", "Calibri", "Arial"};
        try {
            String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();
            for (String want : candidates) {
                for (String have : installed) {
                    if (have.equalsIgnoreCase(want)) {
                        return have;
                    }
                }
            }
        } catch (RuntimeException e) {
            // no font list available, the logical font below is fine
        }
        return Font.SANS_SERIF;
    }

    private Theme() {
    }
}
