package minios.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Toolkit;

// The window that opens when the program starts. One tab per module, in the
// same order as the console menu.
public final class GuiMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mini OS Simulator");

            JLabel header = new JLabel("  Choose a module, enter your data, then click Run.");
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            JLabel note = new JLabel(
                    "  This program simulates the algorithms for our OS class; it does not "
                    + "control real hardware.");

            JPanel top = new JPanel(new GridLayout(2, 1));
            top.add(header);
            top.add(note);

            JTabbedPane tabs = new JTabbedPane();
            // Scroll the tab strip instead of wrapping it onto a second row.
            tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            tabs.addTab("1 CPU Scheduling", new CpuPanel());
            tabs.addTab("2 Banker's Algorithm", new BankerPanel());
            tabs.addTab("3 Paging", new PagingPanel());
            tabs.addTab("4 Segmentation", new SegmentationPanel());
            tabs.addTab("5 Fragmentation", new FragmentationPanel());
            tabs.addTab("6 Page Replacement", new PageReplacePanel());
            tabs.addTab("7 Disk Scheduling", new DiskPanel());
            tabs.addTab("8 Multiprogramming", new MultiPanel());

            frame.setLayout(new BorderLayout());
            frame.add(top, BorderLayout.NORTH);
            frame.add(tabs, BorderLayout.CENTER);
            // 1180 x 740 is enough for the widest table. On a smaller screen the
            // window is cut down so the buttons are not off the bottom edge.
            int width = 1180;
            int height = 740;
            try {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                width = Math.min(width, screen.width - 40);
                height = Math.min(height, screen.height - 80);
            } catch (RuntimeException e) {
                // no screen size to ask for, so keep the normal size
            }
            frame.setSize(width, height);
            frame.setMinimumSize(new Dimension(820, 540));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private GuiMain() {
    }
}
