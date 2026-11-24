package minios.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

// One small tile in the summary strip: a caption, a big value and a hint about
// what the number means. The colour is picked by kind of value.
class MetricTile extends JComponent {

    private static final long serialVersionUID = 1L;

    private final String caption;
    private String value = "-";
    private String hint = "";
    private Color tone = Theme.ACCENT;

    MetricTile(String caption) {
        this.caption = caption;
        setPreferredSize(new Dimension(120, 58));
        setMinimumSize(new Dimension(90, 58));
    }

    void set(String value, String hint, Color tone) {
        this.value = value;
        this.hint = hint;
        this.tone = tone;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(Theme.SOFT);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
        g2.setColor(new Color(tone.getRed(), tone.getGreen(), tone.getBlue(), 40));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
        g2.setColor(tone);
        g2.fillRoundRect(0, 8, 3, h - 16, 3, 3);

        g2.setFont(Theme.TINY);
        g2.setColor(Theme.MUTED);
        g2.drawString(caption.toUpperCase(), 11, 15);

        g2.setFont(Theme.VALUE);
        g2.setColor(Theme.INK);
        g2.drawString(value, 10, 34);

        if (!hint.isEmpty()) {
            g2.setFont(Theme.SMALL);
            g2.setColor(tone);
            g2.drawString(hint, 11, 49);
        }
        g2.dispose();
    }
}
