package minios.gui;

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
import java.awt.GridLayout;
import java.awt.Insets;
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

// The window. A sidebar on the left picks the module, the chosen screen fills
// the rest. One card per module, all created up front so switching is instant.
public final class GuiMain {

    private static final String[] NAMES = {
        "CPU Scheduling", "Banker's Algorithm", "Paging", "Segmentation",
        "Fragmentation", "Page Replacement", "Disk Scheduling", "Multiprogramming"
    };

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the defaults are close enough if this fails
        }
        // a few global colours the standard components pick up
        UIManager.put("Panel.background", Theme.PAGE);
        UIManager.put("OptionPane.background", Theme.CARD);
        UIManager.put("ComboBox.font", Theme.BODY);
        UIManager.put("TextField.font", Theme.BODY);
        UIManager.put("ScrollBar.width", 12);

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

        Sidebar[] buttons = new Sidebar[panels.length];
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.CARD);
        nav.setBorder(new EmptyBorder(6, 10, 12, 10));

        for (int i = 0; i < panels.length; i++) {
            final int index = i;
            final Sidebar item = new Sidebar(i + 1, NAMES[i], index == 0);
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (Sidebar other : buttons) {
                        other.setSelected(false);
                    }
                    item.setSelected(true);
                    cards.show(pages, String.valueOf(index));
                }
            });
            buttons[i] = item;
            nav.add(item);
        }

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildSidebar(panels, nav), BorderLayout.WEST);
        frame.add(pages, BorderLayout.CENTER);

        int width = 1240;
        int height = 820;
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            width = Math.min(width, screen.width - 60);
            height = Math.min(height, screen.height - 90);
        } catch (RuntimeException e) {
            // no screen to ask, keep the normal size
        }
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(980, 620));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Title on the left, the "this is not a real OS" note on the right.
    private static JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Operating System Simulator");
        title.setFont(Theme.APP);
        title.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Eight modules. Enter data on the left, read the report on the right.");
        sub.setFont(Theme.APP_SUB);
        sub.setForeground(Theme.MUTED);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(sub);

        JLabel badge = new JLabel("CLASSROOM PROJECT");
        badge.setFont(new Font(Theme.SMALL.getFontName(), Font.BOLD, 10));
        badge.setForeground(Theme.ACCENT);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_SOFT),
                new EmptyBorder(4, 8, 4, 8)));

        header.add(text, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private static JPanel buildSidebar(ModulePanel[] panels, JPanel nav) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Theme.CARD);
        column.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE));
        column.setPreferredSize(new Dimension(224, 100));

        JLabel heading = new JLabel("MODULES");
        heading.setFont(new Font(Theme.SMALL.getFontName(), Font.BOLD, 10));
        heading.setForeground(Theme.MUTED);
        heading.setBorder(new EmptyBorder(16, 20, 8, 20));
        column.add(heading, BorderLayout.NORTH);
        column.add(nav, BorderLayout.CENTER);

        JLabel footnote = new JLabel("<html>Simulation only.<br>No real hardware.</html>");
        footnote.setFont(Theme.SMALL);
        footnote.setForeground(Theme.MUTED);
        footnote.setBorder(new EmptyBorder(10, 20, 16, 20));
        column.add(footnote, BorderLayout.SOUTH);
        return column;
    }

    // One row of the sidebar. Drawn by hand so the selected item is a solid
    // block with a left marker, not the platform's default highlight.
    static final class Sidebar extends JPanel {
        private static final long serialVersionUID = 1L;

        private final String label;
        private final String number;
        private boolean selected;

        Sidebar(int number, String label, boolean selected) {
            this.number = String.valueOf(number);
            this.label = label;
            this.selected = selected;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(9, 12, 9, 10));
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
                g2.setColor(Theme.ACCENT_SOFT);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 4, 3, h - 8, 3, 3);
            }
            g2.setFont(Theme.SMALL);
            g2.setColor(selected ? Theme.ACCENT : Theme.MUTED);
            g2.drawString(number, 12, h / 2 + 4);

            g2.setFont(selected ? Theme.BODY_BOLD : Theme.BODY);
            g2.setColor(selected ? Theme.ACCENT : Theme.TEXT);
            g2.drawString(label, 30, h / 2 + 4);
            g2.dispose();
        }
    }

    private GuiMain() {
    }
}
