package minios.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

// Entry point of the GUI application: one window, one tab per module.
public final class GuiMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default look and feel is fine if this fails.
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(
                    "Mini OS Simulator - educational demonstration (not a real OS)");

            JLabel header = new JLabel(
                    "  Mini OS Simulator: pick a module, fill in the input, press Run. "
                    + "This is NOT a real operating system.  ");
            header.setFont(header.getFont().deriveFont(Font.BOLD));

            JPanel top = new JPanel(new BorderLayout());
            top.add(header, BorderLayout.CENTER);

            JTabbedPane tabs = new JTabbedPane();
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
            frame.setSize(1040, 740);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private GuiMain() {
    }
}
