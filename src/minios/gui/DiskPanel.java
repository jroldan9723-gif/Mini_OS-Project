package minios.gui;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.DiskScheduler;

// Tab 7. The queue is checked against the disk size here, otherwise
// the module would ask for the whole problem again.
public class DiskPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JComboBox<String> algorithm = new JComboBox<>(new String[]{
        "FCFS (First-Come, First-Served)",
        "SSTF (Shortest Seek Time First)",
        "SCAN (elevator)",
        "C-SCAN (circular elevator)",
        "LOOK (SCAN without the boundary trip)",
        "C-LOOK (circular LOOK)",
        "Compare all six on the same requests"
    });
    private final JTextField maxCylinder = Ui.field(8);
    private final JTextField head = Ui.field(8);
    private final JTextArea requests = Ui.area(4, 34);
    private final JComboBox<String> direction = new JComboBox<>(new String[]{
        "Toward higher cylinders",
        "Toward lower cylinders"
    });

    public DiskPanel() {
        super("Disk Scheduling",
                "Cylinder range, head position, and the list of requests.",
                DiskScheduler::run);
        addRow("Algorithm:", algorithm);
        addRow("Highest cylinder number (disk = 0..max):", maxCylinder);
        addRow("Initial head position:", head);
        addRow("Initial direction for SCAN / C-SCAN / LOOK / C-LOOK:", direction);
        addWideRow(new JLabel("<html><b>Request queue</b> - cylinder numbers, any length</html>"));
        addLastWideRow(requests);
    }

    @Override
    public String buildScript() {
        int choice = algorithm.getSelectedIndex() + 1;
        int max = Ui.parseInt("Highest cylinder number", maxCylinder.getText(), 1, 1_000_000);
        int start = Ui.parseInt("Initial head position", head.getText(), 0, max);
        int[] queue = Ui.parseIntList("Request queue", requests.getText(), 0, max);
        int dir = direction.getSelectedIndex() + 1;

        StringBuilder script = new StringBuilder();
        script.append(choice).append('\n');
        script.append(max).append('\n');
        script.append(start).append('\n');
        script.append(queue.length).append('\n');
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < queue.length; i++) {
            if (i > 0) {
                line.append(' ');
            }
            line.append(queue[i]);
        }
        script.append(line).append('\n');
        script.append(dir).append('\n');
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        algorithm.setSelectedIndex(6);
        maxCylinder.setText("199");
        head.setText("53");
        requests.setText("98 183 37 122 14 124 65 67");
        direction.setSelectedIndex(0);
    }
}
