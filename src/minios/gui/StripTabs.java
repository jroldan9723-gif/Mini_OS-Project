package minios.gui;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

// A tab strip drawn by hand: a thin top bar with the eight module names, the
// open one marked by a dark underline. The stock Swing tabs are tall, rounded
// and gradient filled, which reads as a 1990s dialog box.
final class StripTabs extends JTabbedPane {

    private static final long serialVersionUID = 1L;

    private static final int STRIP_HEIGHT = 32;

    StripTabs() {
        setUI(new StripUi());
        setFont(Theme.TAB);
        setOpaque(true);
        setBackground(Theme.STRIP_BG);
        setForeground(Theme.MUTED);
        setBorder(null);
        setFocusable(false);
    }

    private static final class StripUi extends BasicTabbedPaneUI {
        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return STRIP_HEIGHT;
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            // roomy left and right padding, like a toolbar button
            return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 22;
        }

        @Override
        protected int calculateTabAreaHeight(int tabPlacement, int runCount, int maxTabHeight) {
            return maxTabHeight;
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(isSelected ? Theme.SURFACE : Theme.STRIP_BG);
            g2.fillRect(x, y, w, h);
            if (isSelected) {
                // a dark line along the whole top edge marks the open module
                g2.setColor(Theme.ACCENT);
                g2.fillRect(x, 0, w, 2);
                g2.setColor(Theme.SURFACE);
                g2.fillRect(x, y + h - 1, w, 1);
            } else {
                g2.setColor(Theme.BORDER);
                g2.drawLine(x, 0, x + w - 1, 0);
            }
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                int x, int y, int w, int h, boolean isSelected) {
            // no outline around the tab; a divider line does the separating
            g.setColor(Theme.BORDER);
            g.drawLine(x + w - 1, y + 7, x + w - 1, y + h - 8);
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // keyboard focus ring not drawn, the underline already shows the tab
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            g.setColor(Theme.STRIP_BG);
            g.fillRect(0, 0, tabPane.getWidth(), maxTabHeight + 1);
            g.setColor(Theme.BORDER);
            g.drawLine(0, maxTabHeight, tabPane.getWidth(), maxTabHeight);
            super.paintTabArea(g, tabPlacement, selectedIndex);
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // the page below the strip paints its own background
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, java.awt.Font font,
                FontMetrics metrics, int tabIndex, String title, Rectangle textRect,
                boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(isSelected ? Theme.TAB_ACTIVE : Theme.TAB);
            g2.setColor(isSelected ? Theme.INK : Theme.MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int x = textRect.x + (textRect.width - fm.stringWidth(title)) / 2;
            int y = textRect.y + (textRect.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(title, x, y);
            g2.dispose();
        }
    }

}
