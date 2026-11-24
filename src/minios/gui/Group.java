package minios.gui;

import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

// A titled group: a one pixel frame with the title sitting in a gap in the top
// line. That is the ordinary way a desktop form groups its fields, and it
// needs no shadow or rounded corner to read as a group.
final class Group extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String title;

    Group(String title) {
        this(title, new BorderLayout());
    }

    Group(String title, java.awt.LayoutManager layout) {
        super(layout);
        this.title = title;
        setOpaque(true);
        setBackground(Theme.SURFACE);
        if (!(getBorder() instanceof EmptyBorder)) {
            setBorder(new EmptyBorder(20, 10, 10, 10));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int lineY = 7;
        int w = getWidth();

        // frame, drawn in three pieces so the title has a gap to sit in
        g2.setColor(Theme.BORDER_DARK);
        int textWidth = 0;
        if (title != null && !title.isEmpty()) {
            g2.setFont(Theme.GROUP_TITLE);
            FontMetrics fm = g2.getFontMetrics();
            textWidth = fm.stringWidth(title) + 10;
            g2.drawLine(0, lineY, 8, lineY);
            g2.drawLine(8 + textWidth, lineY, w - 1, lineY);
            g2.setColor(Theme.HEADING);
            g2.drawString(title, 13, lineY + 4);
        } else {
            g2.drawLine(0, lineY, w - 1, lineY);
        }
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, lineY, 0, getHeight() - 1);
        g2.drawLine(w - 1, lineY, w - 1, getHeight() - 1);
        g2.drawLine(0, getHeight() - 1, w - 1, getHeight() - 1);
        g2.dispose();
    }

}
