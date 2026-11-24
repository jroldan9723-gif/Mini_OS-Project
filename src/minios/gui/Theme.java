package minios.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

// Colours and fonts for the whole window, kept in one place so every screen
// matches. Nothing here depends on the operating system look and feel.
final class Theme {

    // Page and cards
    static final Color PAGE = new Color(0xF5, 0xF7, 0xFB);
    static final Color CARD = Color.WHITE;
    static final Color LINE = new Color(0xE2, 0xE8, 0xF0);
    static final Color SOFT = new Color(0xF1, 0xF5, 0xFA);
    static final Color SHADOW = new Color(15, 26, 43, 20);

    // Dark rail on the left
    static final Color RAIL = new Color(0x0E, 0x1A, 0x2B);
    static final Color RAIL_SOFT = new Color(0x18, 0x28, 0x3D);
    static final Color RAIL_TEXT = new Color(0xA7, 0xB6, 0xCA);
    static final Color RAIL_DIM = new Color(0x6B, 0x7C, 0x95);

    // Text
    static final Color INK = new Color(0x0F, 0x1A, 0x2B);
    static final Color TEXT_CAPTION = new Color(0x3C, 0x4A, 0x5E);
    static final Color MUTED = new Color(0x64, 0x74, 0x8B);
    static final Color DISABLED = new Color(0xA3, 0xAE, 0xBF);

    // Accents
    static final Color ACCENT = new Color(0x2F, 0x6F, 0xEB);
    static final Color ACCENT_DARK = new Color(0x1D, 0x4E, 0xD8);
    static final Color ACCENT_SOFT = new Color(0xE8, 0xF0, 0xFE);
    static final Color GREEN = new Color(0x0F, 0x8B, 0x5A);
    static final Color GREEN_SOFT = new Color(0xE6, 0xF6, 0xEE);
    static final Color AMBER = new Color(0xB4, 0x53, 0x09);
    static final Color AMBER_SOFT = new Color(0xFD, 0xF3, 0xE3);
    static final Color RED = new Color(0xB4, 0x23, 0x18);

    private static final String FAMILY = pickFamily();
    private static final String MONO_FAMILY = pickMono();

    static final Font TITLE = new Font(FAMILY, Font.BOLD, 19);
    static final Font H1 = new Font(FAMILY, Font.BOLD, 14);
    static final Font H2 = new Font(FAMILY, Font.BOLD, 12);
    static final Font BODY = new Font(FAMILY, Font.PLAIN, 12);
    static final Font BODY_BOLD = new Font(FAMILY, Font.BOLD, 12);
    static final Font SMALL = new Font(FAMILY, Font.PLAIN, 11);
    static final Font TINY = new Font(FAMILY, Font.BOLD, 9);
    static final Font VALUE = new Font(FAMILY, Font.BOLD, 15);
    static final Font MONO = new Font(MONO_FAMILY, Font.PLAIN, 12);
    static final Font MONO_BOLD = new Font(MONO_FAMILY, Font.BOLD, 12);

    // Segoe UI on Windows, then whatever the system offers. Asking the
    // GraphicsEnvironment avoids a Font lookup that silently falls back.
    private static String pickFamily() {
        return firstAvailable("Segoe UI", "Calibri", "Arial");
    }

    private static String pickMono() {
        return firstAvailable("Consolas", "Cascadia Mono", "Menlo", "DejaVu Sans Mono");
    }

    private static String firstAvailable(String... wanted) {
        try {
            String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();
            for (String want : wanted) {
                for (String have : installed) {
                    if (have.equalsIgnoreCase(want)) {
                        return have;
                    }
                }
            }
        } catch (RuntimeException e) {
            // no font list available, the logical fonts below are fine
        }
        return Font.SANS_SERIF;
    }

    private Theme() {
    }
}
