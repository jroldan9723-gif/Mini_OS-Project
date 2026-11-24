package minios.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

// Colours and fonts for the whole window, kept in one place so the eight
// module screens match each other. Everything is flat: no bevels, no
// gradients, no sticker shadows.
final class Theme {

    // Window and surfaces
    static final Color WINDOW = new Color(0xEC, 0xEF, 0xF3);
    static final Color SURFACE = Color.WHITE;
    static final Color BAR = new Color(0x1F, 0x2A, 0x37);
    static final Color BAR_TEXT = new Color(0x9F, 0xAD, 0xBF);
    static final Color HEADER_BG = new Color(0xF4, 0xF6, 0xF9);
    static final Color STRIP_BG = new Color(0xE4, 0xE8, 0xEE);

    // Lines
    static final Color BORDER = new Color(0xC2, 0xC9, 0xD4);
    static final Color HAIRLINE = new Color(0xDE, 0xE3, 0xEA);

    // Text
    static final Color INK = new Color(0x1B, 0x24, 0x30);
    static final Color HEADING = new Color(0x46, 0x53, 0x6B);
    static final Color MUTED = new Color(0x5C, 0x67, 0x76);
    static final Color DISABLED = new Color(0xA6, 0xAF, 0xBC);

    // One accent, used for the active tab, the primary button and the status
    // dot. Nothing else is coloured, which keeps the window calm.
    static final Color ACCENT = new Color(0x2B, 0x4C, 0x6F);
    static final Color ACCENT_HOVER = new Color(0x22, 0x3D, 0x59);
    static final Color ACCENT_TEXT = new Color(0x1F, 0x4E, 0x79);
    static final Color GREEN = new Color(0x1E, 0x7A, 0x46);
    static final Color AMBER = new Color(0x97, 0x5A, 0x0B);
    static final Color RED = new Color(0xA3, 0x25, 0x1C);

    private static final String FAMILY = pick("Segoe UI", "Calibri", "Arial");
    private static final String MONO_FAMILY = pick("Consolas", "Cascadia Mono", "Menlo", "DejaVu Sans Mono");

    static final Font APP_TITLE = new Font(FAMILY, Font.BOLD, 13);
    static final Font TAB = new Font(FAMILY, Font.PLAIN, 12);
    static final Font TAB_ACTIVE = new Font(FAMILY, Font.BOLD, 12);
    static final Font GROUP_TITLE = new Font(FAMILY, Font.BOLD, 10);
    static final Font LABEL = new Font(FAMILY, Font.PLAIN, 12);
    static final Font LABEL_BOLD = new Font(FAMILY, Font.BOLD, 11);
    static final Font HINT = new Font(FAMILY, Font.PLAIN, 11);
    static final Font VALUE = new Font(FAMILY, Font.PLAIN, 12);
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
