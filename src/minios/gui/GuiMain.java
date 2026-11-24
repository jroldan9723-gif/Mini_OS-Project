package minios.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

// The window. A three pixel dark bar along the top, the eight module names as
// tabs, then the open module below. All eight screens are created up front so
// switching tabs is instant and nothing is lost when moving around.
public final class GuiMain {

    private static final String[] SHORT = {
        "CPU Scheduling", "Banker's Algorithm", "Paging", "Segmentation",
        "Fragmentation", "Page Replacement", "Disk Scheduling", "Multiprogramming"
    };

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the defaults are close enough if this fails
        }
        UIManager.put("Panel.background", Theme.WINDOW);
        UIManager.put("OptionPane.background", Theme.SURFACE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("TextField.font", Theme.VALUE);
        UIManager.put("TextArea.font", Theme.VALUE);
        UIManager.put("ComboBox.font", Theme.VALUE);
        UIManager.put("ScrollBar.width", 12);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                build();
            }
        });
    }

    private static void build() {
        JFrame frame = new JFrame("Operating System Simulator");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Theme.WINDOW);

        StripTabs tabs = new StripTabs();
        ModulePanel[] panels = {
            new CpuPanel(), new BankerPanel(), new PagingPanel(), new SegmentationPanel(),
            new FragmentationPanel(), new PageReplacePanel(), new DiskPanel(), new MultiPanel()
        };
        for (int i = 0; i < panels.length; i++) {
            tabs.addTab(SHORT[i], panels[i]);
        }
        tabs.setSelectedIndex(0);

        frame.add(buildTopBar(), BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(new WindowFooter(panels, tabs), BorderLayout.SOUTH);

        int width = 1280;
        int height = 860;
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            width = Math.min(width, screen.width - 60);
            height = Math.min(height, screen.height - 90);
        } catch (RuntimeException e) {
            // no screen to ask, keep the normal size
        }
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(1020, 660));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Thin bar with the program name. Deliberately shallow: the tabs below it
    // are the real navigation, so this is only a title.
    private static JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BAR);
        bar.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel name = new JLabel("Operating System Simulator");
        name.setFont(Theme.APP_TITLE);
        name.setForeground(Color.WHITE);
        bar.add(name, BorderLayout.WEST);

        JLabel note = new JLabel("classroom project  |  Java, no external libraries");
        note.setFont(Theme.HINT);
        note.setForeground(Theme.BAR_TEXT);
        bar.add(note, BorderLayout.EAST);
        return bar;
    }

    // A single status line under the tabs. It reports what the last run did,
    // so the report itself stays free of chatter.
    static final class WindowFooter extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JLabel text = new JLabel(" ");

        WindowFooter(ModulePanel[] panels, StripTabs tabs) {
            super(new BorderLayout(8, 0));
            setBackground(Theme.HEADER_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                    new EmptyBorder(5, 12, 5, 12)));
            text.setFont(Theme.STATUS);
            text.setForeground(Theme.MUTED);
            add(text, BorderLayout.WEST);
            add(new StatusDot(), BorderLayout.EAST);

            for (ModulePanel panel : panels) {
                panel.setStatusSink(new ModulePanel.StatusSink() {
                    @Override
                    public void update(String message, int tone) {
                        text.setText(message);
                        text.setForeground(tone == 1 ? Theme.GREEN
                                : tone == 2 ? Theme.RED : Theme.MUTED);
                    }
                });
            }
            tabs.addChangeListener(e -> text.setText(" "));
        }
    }

    // Green square that shows the window is idle. It is static on purpose:
    // nothing here talks to hardware.
    static final class StatusDot extends JPanel {
        private static final long serialVersionUID = 1L;

        StatusDot() {
            setOpaque(false);
            setPreferredSize(new Dimension(58, 14));
            JLabel l = new JLabel("ready");
            l.setFont(Theme.STATUS);
            l.setForeground(Theme.MUTED);
            setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            add(l);
            setBorder(new EmptyBorder(0, 12, 0, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.GREEN);
            g2.fillOval(0, getHeight() / 2 - 3, 6, 6);
            g2.dispose();
        }
    }

    private GuiMain() {
    }
}
