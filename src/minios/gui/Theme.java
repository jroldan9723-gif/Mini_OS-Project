package minios.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

// Colours and fonts for the whole window, kept in one place so the eight
// module screens match each other. Flat surfaces, one accent colour, and a
// neutral grey scale that keeps the report readable.
final class Theme {

    // Window and surfaces
    static final Color WINDOW = new Color(0xF2, 0xF4, 0xF7);
    static final Color SURFACE = Color.WHITE;
    static final Color BAR = new Color(0x1C, 0x25, 0x33);
    static final Color BAR_TEXT = new Color(0x93, 0xA2, 0xB5);
    static final Color HEADER_BG = new Color(0xF7, 0xF9, 0xFB);
    static final Color STRIP_BG = new Color(0xE8, 0xEC, 0xF1);
    static final Color ZEBRA = new Color(0xF7, 0xF9, 0xFB);
    static final Color SELECT = new Color(0xDC, 0xE7, 0xF3);

    // Lines
    static final Color BORDER = new Color(0xCB, 0xD2, 0xDC);
    static final Color BORDER_DARK = new Color(0xAF, 0xB9, 0xC6);
    static final Color HAIRLINE = new Color(0xE4, 0xE8, 0xEE);

    // Text
    static final Color INK = new Color(0x18, 0x21, 0x2E);
    static final Color HEADING = new Color(0x3A, 0x47, 0x5A);
    static final Color MUTED = new Color(0x5B, 0x66, 0x75);
    static final Color DISABLED = new Color(0x9E, 0xA8, 0xB5);

    // One accent, used for the open tab, the primary button and the focus
    // ring. Nothing else is coloured, which keeps the window calm.
    static final Color ACCENT = new Color(0x1E, 0x4E, 0x7A);
    static final Color ACCENT_HOVER = new Color(0x17, 0x3D, 0x60);
    static final Color ACCENT_LIGHT = new Color(0x4A, 0x87, 0xC4);
    static final Color GREEN = new Color(0x1B, 0x6E, 0x40);
    static final Color AMBER = new Color(0x8A, 0x52, 0x0A);
    static final Color RED = new Color(0x9C, 0x22, 0x1A);

    private static final String FAMILY = pick("Segoe UI", "Calibri", "Arial");
    private static final String MONO_FAMILY = pick("Consolas", "Cascadia Mono", "Menlo", "DejaVu Sans Mono");

    static final Font APP_TITLE = new Font(FAMILY, Font.BOLD, 13);
    static final Font TITLE_BIG = new Font(FAMILY, Font.BOLD, 22);
    static final Font CARD_TITLE = new Font(FAMILY, Font.BOLD, 13);
    static final Font TAB = new Font(FAMILY, Font.PLAIN, 12);
    static final Font TAB_ACTIVE = new Font(FAMILY, Font.BOLD, 12);
    static final Font GROUP_TITLE = new Font(FAMILY, Font.BOLD, 10);
    static final Font LABEL = new Font(FAMILY, Font.PLAIN, 12);
    static final Font LABEL_BOLD = new Font(FAMILY, Font.BOLD, 11);
    static final Font HINT = new Font(FAMILY, Font.PLAIN, 11);
    static final Font VALUE = new Font(FAMILY, Font.PLAIN, 12);
    static final Font TABLE = new Font(FAMILY, Font.PLAIN, 12);
    static final Font TABLE_HEAD = new Font(FAMILY, Font.BOLD, 11);
    static final Font BUTTON = new Font(FAMILY, Font.BOLD, 12);
    static final Font STATUS = new Font(FAMILY, Font.PLAIN, 11);
    static final Font MONO = new Font(MONO_FAMILY, Font.PLAIN, 12);

    // Asking the GraphicsEnvironment avoids a silent fallback to a font that
    // does not exist on this machine.
    private static String pick(String... wanted) {
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
            // no font list, the logical font below is fine
        }
        return Font.SANS_SERIF;
    }

    private Theme() {
    }
}
