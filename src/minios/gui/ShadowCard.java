package minios.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

// A white panel with a soft drop shadow and a hairline border. Doing this by
// hand gives the flat, even look that the standard border factory cannot.
class ShadowCard extends JPanel {

    private static final long serialVersionUID = 1L;

    ShadowCard(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // two faint shadow passes, then the card itself
        g2.setColor(new Color(15, 26, 43, 12));
        g2.fillRoundRect(1, 2, w - 2, h - 3, 12, 12);
        g2.setColor(new Color(15, 26, 43, 16));
        g2.fillRoundRect(0, 1, w - 1, h - 2, 12, 12);

        g2.setColor(Theme.CARD);
        g2.fillRoundRect(0, 0, w - 1, h - 3, 11, 11);
        g2.setColor(Theme.LINE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 3, 11, 11);
        g2.dispose();
        super.paintComponent(g);
    }
}
