package minios.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.PagingSimulator;

// Tab 3. One frame number per page, -1 meaning the page is on disk.
public class PagingPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextField pageSize = Ui.field(10);
    private final JTextArea frames = Ui.area(4, 30);
    private final JTextArea addresses = Ui.area(4, 30);

    public PagingPanel() {
        super("Paging",
                "One frame number per page, -1 if the page is on disk.",
                PagingSimulator::run);
        addSetting("Page size in bytes:", pageSize);
        JPanel group = addGroup("Page table and addresses");
        group.setLayout(new BorderLayout());
        JPanel lists = new JPanel(new GridLayout(2, 1, 0, 6));
        lists.setOpaque(false);
        lists.add(labelled("Frame number per page", frames));
        lists.add(labelled("Logical addresses (optional)", addresses));
        group.add(lists, BorderLayout.CENTER);
    }

    @Override
    public String buildScript() {
        int size = Ui.parseInt("Page size", pageSize.getText(), 1, 1_000_000);
        int[] table = Ui.parseIntList("Page table", frames.getText(), -1, 100000);
        int[] addrs = addresses.getText().trim().isEmpty()
                ? new int[0]
                : Ui.parseIntList("Logical addresses", addresses.getText());

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(size).append('\n');
        script.append(table.length).append('\n');
        for (int frame : table) {
            script.append(frame).append('\n');
        }
        for (int addr : addrs) {
            script.append(addr).append('\n');
        }
        script.append("-1\n0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        pageSize.setText("1024");
        frames.setText("-1 0 1 -1 3 20 5 12");
        addresses.setText("0 1024 5000 8192 20000 999999");
    }
}
