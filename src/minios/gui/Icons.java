package minios.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.JComponent;

// Small line drawings used on the module rail. Drawing them here keeps the
// project free of image files, and they stay sharp at any size.
final class Icons {

    // Each name is drawn inside a 16 by 16 box starting at x, y.
    static void paint(Graphics2D g, String name, int x, int y, Color colour) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(colour);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.translate(x, y);

        if ("cpu".equals(name)) {
            g2.drawRoundRect(2, 2, 12, 12, 3, 3);
            g2.drawLine(6, 6, 10, 6);
            g2.drawLine(6, 9, 10, 9);
        } else if ("banker".equals(name)) {
            g2.drawRoundRect(1, 4, 14, 9, 2, 2);
            g2.drawLine(8, 1, 8, 4);
            g2.drawLine(5, 1, 11, 1);
            g2.drawOval(6, 7, 4, 4);
        } else if ("paging".equals(name)) {
            g2.drawRect(1, 2, 14, 12);
            g2.drawLine(1, 6, 15, 6);
            g2.drawLine(6, 6, 6, 14);
        } else if ("segment".equals(name)) {
            g2.drawRect(1, 3, 14, 3);
            g2.drawRect(1, 8, 9, 3);
            g2.drawRect(11, 8, 4, 3);
        } else if ("fragment".equals(name)) {
            g2.drawRect(1, 3, 5, 10);
            g2.drawRect(7, 3, 3, 6);
            g2.drawRect(11, 3, 4, 10);
        } else if ("replace".equals(name)) {
            g2.drawArc(2, 3, 11, 10, 30, 300);
            Path2D arrow = new Path2D.Double();
            arrow.moveTo(13, 3);
            arrow.lineTo(13, 7);
            arrow.lineTo(9.5, 5);
            arrow.closePath();
            g2.fill(arrow);
        } else if ("disk".equals(name)) {
            g2.drawOval(2, 2, 12, 12);
            g2.drawOval(6, 6, 4, 4);
            g2.drawLine(12, 4, 14, 2);
        } else {
            // multiprogramming: three stacked bars
            g2.drawRoundRect(1, 2, 14, 3, 2, 2);
            g2.drawRoundRect(1, 7, 10, 3, 2, 2);
            g2.drawRoundRect(1, 12, 12, 3, 2, 2);
        }
        g2.dispose();
    }

    // The tiny mark in the top-left corner of the window.
    static JComponent logo() {
        return new JComponent() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(34, 34);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 0, 34, 34, 9, 9);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(9, 12, 25, 12);
                g2.drawLine(9, 18, 20, 18);
                g2.drawLine(9, 24, 16, 24);
                g2.dispose();
            }
        };
    }

    private Icons() {
    }
}
