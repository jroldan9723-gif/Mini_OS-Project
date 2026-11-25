package minios.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

// The first screen. Eight cards, each naming a module and what it does, so a
// visitor can pick one without reading the tabs first. Clicking a card opens
// that tab, exactly like clicking the tab itself.
final class HomePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] TITLES = {
        "CPU Scheduling", "Banker's Algorithm", "Paging", "Segmentation",
        "Fragmentation", "Page Replacement", "Disk Scheduling", "Multiprogramming"
    };

    private static final String[] BLURBS = {
        "FCFS, SJF, Round Robin, SRTF and Priority scheduling on the same processes.",
        "Deadlock avoidance: safety check and resource requests by process.",
        "Logical to physical address translation with a page table.",
        "Segment table lookup with bounds checking and overlap detection.",
        "First Fit, Best Fit and Worst Fit on fixed and variable memory.",
        "FIFO, LRU, Optimal and LFU page replacement on one reference string.",
        "FCFS, SSTF, SCAN, C-SCAN, LOOK and C-LOOK head movement.",
        "Jobs sharing one CPU and one block of memory, tick by tick."
    };

    private final Component[] cards = new Component[TITLES.length];
    private int hovered = -1;

    HomePanel(final StripTabs tabs) {
        super(new BorderLayout());
        setBackground(Theme.WINDOW);

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.WINDOW);
        head.setBorder(new EmptyBorder(16, 26, 10, 26));

        JPanel text = new JPanel(new BorderLayout(0, 6));
        text.setOpaque(false);
        JLabel title = new JLabel("Operating System Simulator");
        title.setFont(Theme.TITLE_BIG);
        title.setForeground(Theme.INK);
        JLabel sub = new JLabel("Pick a module below or use the tabs above. "
                + "Each one runs a classic operating system algorithm on data you enter.");
        sub.setFont(Theme.LABEL);
        sub.setForeground(Theme.MUTED);
        text.add(title, BorderLayout.NORTH);
        text.add(sub, BorderLayout.CENTER);

        JLabel note = new JLabel("Educational demonstration. This is not a real operating system.");
        note.setFont(Theme.HINT);
        note.setForeground(Theme.MUTED);
        note.setBorder(new EmptyBorder(6, 0, 0, 0));
        text.add(note, BorderLayout.SOUTH);

        head.add(text, BorderLayout.CENTER);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        // cards keep a fixed height: eight of them then fill a normal window
        // with a little air left over, and a small window just scrolls
        grid.setPreferredSize(new Dimension(600, 4 * 108 + 3 * 14));
        grid.setBorder(new EmptyBorder(0, 26, 16, 26));

        for (int i = 0; i < TITLES.length; i++) {
            final int index = i;
            JPanel card = buildCard(i);
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // tab 0 is Home, so module i sits at tab i + 1
                    tabs.setSelectedIndex(index + 1);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = index;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = -1;
                    repaint();
                }
            });
            cards[i] = card;
            grid.add(card);
        }

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(grid, BorderLayout.NORTH);

        JScrollPane scroller = new JScrollPane(holder);
        scroller.setBorder(null);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(18);

        add(head, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);
    }

    private JPanel buildCard(final int index) {
        final JPanel card = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                boolean on = hovered == index;
                g2.setColor(on ? Theme.SELECT : Theme.SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(on ? Theme.ACCENT : Theme.BORDER);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                // a short accent bar down the left edge marks the card
                g2.setColor(on ? Theme.ACCENT : Theme.ACCENT_LIGHT);
                g2.fillRect(0, 0, 3, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setOpaque(false);

        JLabel name = new JLabel((index + 1) + ".  " + TITLES[index]);
        name.setFont(Theme.CARD_TITLE);
        name.setForeground(Theme.INK);

        // A text area wraps to whatever width the card gets, so the sentence
        // never runs off the card edge when the window is narrow.
        JTextArea blurb = new JTextArea(BLURBS[index]);
        blurb.setFont(Theme.LABEL);
        blurb.setForeground(Theme.MUTED);
        blurb.setOpaque(false);
        blurb.setEditable(false);
        blurb.setFocusable(false);
        blurb.setLineWrap(true);
        blurb.setWrapStyleWord(true);
        blurb.setBorder(null);
        blurb.setMargin(new Insets(1, 0, 0, 0));

        body.add(name, BorderLayout.NORTH);
        body.add(blurb, BorderLayout.CENTER);

        JLabel open = new JLabel("Open  >");
        open.setFont(Theme.HINT);
        open.setForeground(Theme.ACCENT);
        open.setBorder(new EmptyBorder(10, 0, 0, 0));

        card.add(body, BorderLayout.CENTER);
        card.add(open, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(300, 108));
        return card;
    }

    // Draws the three little bars used beside the window title.
    static JPanel badge(int size) {
        JPanel p = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_LIGHT);
                int h = getHeight();
                g2.fillRoundRect(0, 0, getWidth(), h, 3, 3);
                g2.setColor(Theme.SURFACE);
                int bar = Math.max(2, h / 6);
                int gap = Math.max(1, h / 8);
                int y = (h - (3 * bar + 2 * gap)) / 2;
                for (int i = 0; i < 3; i++) {
                    g2.fillRect(h / 3, y + i * (bar + gap), getWidth() - 2 * (h / 3), bar);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(size, size));
        return p;
    }

}
