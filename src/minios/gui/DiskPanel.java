package minios.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.DiskScheduler;

// Tab 7. The request queue is built one cylinder at a time with Add row, so
// a long queue is easy to trim or correct before running.
public class DiskPanel extends ModulePanel {

    private final ResultChart chart = new ResultChart();

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
    private final JComboBox<String> direction = new JComboBox<>(new String[]{
        "Toward higher cylinders",
        "Toward lower cylinders"
    });

    private final RecordEditor requests = new RecordEditor(null, 1,
            RecordEditor.Column.number("Cylinder", 100, 0, Integer.MAX_VALUE));

    private JComponent directionRow;

    public DiskPanel() {
        super("Disk Scheduling",
                "Cylinder range, head position and the request queue.",
                DiskScheduler::run);
        Ui.styleCombo(algorithm);
        Ui.styleCombo(direction);
        addSetting("Algorithm:", algorithm);
        addSetting("Highest cylinder:", maxCylinder);
        addSetting("Initial head:", head);
        directionRow = addSetting("Initial direction:", direction);

                chart.setEmptyHint("Run a schedule to see the head movement.");
        addPreview(chart, "HEAD MOVEMENT", 124);

JPanel group = addGroup("Request queue");
        group.setLayout(new BorderLayout());
        group.add(requests, BorderLayout.CENTER);

        algorithm.addActionListener(e -> updateDirectionRow());
        updateDirectionRow();
    }

    // Choice 1 is FCFS and 2 is SSTF; neither scans in a direction.
    private void updateDirectionRow() {
        int choice = algorithm.getSelectedIndex() + 1;
        setRowEnabled(directionRow, choice >= 3);
    }

    @Override
    public String buildScript() {
        List<String[]> rows = requests.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one cylinder to the request queue.");
        }
        int choice = algorithm.getSelectedIndex() + 1;
        int max = Ui.parseInt("Highest cylinder number", maxCylinder.getText(), 1, 1_000_000);
        int start = Ui.parseInt("Initial head position", head.getText(), 0, max);
        int dir = direction.getSelectedIndex() + 1;

        StringBuilder line = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            int cylinder = Integer.parseInt(rows.get(i)[0]);
            if (cylinder > max) {
                throw new Ui.InputError("Cylinder " + cylinder + " is past the highest cylinder ("
                        + max + ").");
            }
            if (i > 0) {
                line.append(' ');
            }
            line.append(cylinder);
        }

        StringBuilder script = new StringBuilder();
        script.append(choice).append('\n');
        script.append(max).append('\n');
        script.append(start).append('\n');
        script.append(rows.size()).append('\n');
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
        direction.setSelectedIndex(0);
        requests.setRows(new String[][]{
            {"98"}, {"183"}, {"37"}, {"122"}, {"14"}, {"124"}, {"65"}, {"67"}
        });
    }

    @Override
    void afterRun(String reportText) {
        if (reportText != null) {
            chart.loadDisk(reportText);
        }
        setPreviewVisible(reportText != null && chart.hasChart());
    }
}
