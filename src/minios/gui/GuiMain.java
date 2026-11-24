package minios.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

// The window. A dark rail on the left picks the module, the chosen screen fills
// the rest. One card per module, all created up front so switching is instant.
public final class GuiMain {

    private static final String[] NAMES = {
        "CPU Scheduling", "Banker's Algorithm", "Paging", "Segmentation",
        "Fragmentation", "Page Replacement", "Disk Scheduling", "Multiprogramming"
    };

    private static final String[] ICONS = {
        "cpu", "banker", "paging", "segment", "fragment", "replace", "disk", "multi"
    };

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the defaults are close enough if this fails
        }
        UIManager.put("Panel.background", Theme.PAGE);
        UIManager.put("OptionPane.background", Theme.CARD);
        UIManager.put("ComboBox.font", Theme.BODY);
        UIManager.put("TextField.font", Theme.BODY);
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("ScrollBar.thumb", Theme.LINE);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                build();
            }
        });
    }

    private static void build() {
        JFrame frame = new JFrame("Mini OS Simulator");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Theme.PAGE);

        CardLayout cards = new CardLayout();
        JPanel pages = new JPanel(cards);
        pages.setBackground(Theme.PAGE);

        ModulePanel[] panels = {
            new CpuPanel(), new BankerPanel(), new PagingPanel(), new SegmentationPanel(),
            new FragmentationPanel(), new PageReplacePanel(), new DiskPanel(), new MultiPanel()
        };
        for (int i = 0; i < panels.length; i++) {
            pages.add(panels[i], String.valueOf(i));
        }

        RailItem[] items = new RailItem[panels.length];
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.RAIL);
        nav.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < panels.length; i++) {
            final int index = i;
            final RailItem item = new RailItem(i + 1, NAMES[i], ICONS[i], i == 0);
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (RailItem other : items) {
                        other.setSelected(false);
                    }
                    item.setSelected(true);
                    cards.show(pages, String.valueOf(index));
                }
            });
            items[i] = item;
            nav.add(item);
            nav.add(Box.createVerticalStrut(3));
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.PAGE);
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(pages, BorderLayout.CENTER);

        frame.add(buildRail(nav), BorderLayout.WEST);
        frame.add(content, BorderLayout.CENTER);

        int width = 1280;
        int height = 840;
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            width = Math.min(width, screen.width - 50);
            height = Math.min(height, screen.height - 80);
        } catch (RuntimeException e) {
            // no screen to ask, keep the normal size
        }
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(1000, 640));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Logo, title and the "not a real OS" note.
    private static JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.RAIL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.RAIL_SOFT),
                new EmptyBorder(14, 20, 14, 20)));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel title = new JLabel("Mini OS Simulator");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Pick a module, enter data, read the report");
        sub.setFont(Theme.SMALL);
        sub.setForeground(Theme.RAIL_TEXT);
        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(sub);

        left.add(Icons.logo(), BorderLayout.WEST);
        left.add(text, BorderLayout.CENTER);

        JLabel badge = new JLabel("  CLASSROOM PROJECT  ");
        badge.setFont(Theme.TINY);
        badge.setForeground(new Color(0xBF, 0xD4, 0xFF));
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_DARK),
                new EmptyBorder(5, 8, 5, 8)));
        badge.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 0, 0, 4));
        right.add(badge, BorderLayout.EAST);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private static JPanel buildRail(JPanel nav) {
        JPanel rail = new JPanel(new BorderLayout());
        rail.setBackground(Theme.RAIL);
        rail.setPreferredSize(new Dimension(236, 100));

        JLabel heading = new JLabel("MODULES");
        heading.setFont(Theme.TINY);
        heading.setForeground(Theme.RAIL_DIM);
        heading.setBorder(new EmptyBorder(16, 22, 8, 20));
        rail.add(heading, BorderLayout.NORTH);
        rail.add(nav, BorderLayout.CENTER);

        JLabel footnote = new JLabel("<html>Simulation only.<br>The algorithms run in Java, not on real hardware.</html>");
        footnote.setFont(Theme.SMALL);
        footnote.setForeground(Theme.RAIL_DIM);
        footnote.setBorder(new EmptyBorder(10, 22, 18, 16));
        rail.add(footnote, BorderLayout.SOUTH);
        return rail;
    }

    // One row of the rail: icon, number, name. The selected row is a lighter
    // block with an accent bar, so it reads at a glance.
    static final class RailItem extends JPanel {
        private static final long serialVersionUID = 1L;

        private final int number;
        private final String label;
        private final String icon;
        private boolean selected;

        RailItem(int number, String label, String icon, boolean selected) {
            this.number = number;
            this.label = label;
            this.icon = icon;
            this.selected = selected;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(9, 10, 9, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            int w = getWidth();

            if (selected) {
                g2.setColor(Theme.RAIL_SOFT);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 8, 3, h - 16, 3, 3);
            }

            Color iconColour = selected ? Color.WHITE : Theme.RAIL_TEXT;
            Icons.paint(g2, icon, 14, (h - 16) / 2, iconColour);

            g2.setFont(Theme.TINY);
            g2.setColor(selected ? new Color(0x8F, 0xB2, 0xFF) : Theme.RAIL_DIM);
            FontMetrics fm = g2.getFontMetrics();
            String num = String.valueOf(number);
            int numX = 36;
            g2.drawString(num, numX, h / 2 + fm.getAscent() / 2 - 1);

            g2.setFont(selected ? Theme.BODY_BOLD : Theme.BODY);
            g2.setColor(selected ? Color.WHITE : Theme.RAIL_TEXT);
            g2.drawString(label, 52, h / 2 + 4);
            g2.dispose();
        }
    }

    private GuiMain() {
    }
}
