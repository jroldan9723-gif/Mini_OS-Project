package minios.gui;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.CPUScheduler;

// Tab 1. Processes are added one at a time with the Add row button, so a long
// list does not mean keeping four text boxes in step by hand. The quantum row
// only lights up for the algorithms that use it.
public class CpuPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private static final int[] CHOICES = {1, 2, 3, 4, 5, 6};

    private final JComboBox<String> algorithm = new JComboBox<>(new String[]{
        "FCFS (First-Come, First-Served)",
        "SJF (Shortest Job First, non-preemptive)",
        "Round Robin (preemptive, fixed time quantum)",
        "Compare FCFS, SJF, Round Robin and Priority",
        "SRTF (Shortest Remaining Time First, preemptive)",
        "Priority (non-preemptive, smaller number = higher priority)"
    });
    private final JTextField quantum = Ui.field(6);

    private final RecordEditor processes = new RecordEditor(null, 1,
            RecordEditor.Column.optionalName("Process", 92),
            RecordEditor.Column.number("Arrival", 74, 0, 1000),
            RecordEditor.Column.number("Burst", 74, 1, 1000),
            RecordEditor.Column.optionalNumber("Priority", 74, 0, 999));

    private JComponent quantumRow;
    private final GanttChart chart = new GanttChart();

    public CpuPanel() {
        super("CPU Scheduling",
                "Add one row per process. Burst is the work it needs.",
                CPUScheduler::run);
        Ui.styleCombo(algorithm);
        quantum.setColumns(5);
        addSetting("Algorithm:", algorithm);
        quantumRow = addSetting("Time quantum:", quantum);

        JPanel group = addGroup("Processes");
        group.setLayout(new BorderLayout());
        group.add(processes, BorderLayout.CENTER);

        addPreview(chart);

        algorithm.addActionListener(e -> updateOptionalRows());
        updateOptionalRows();
    }

    // 3 is Round Robin, 4 is the comparison, 6 is Priority.
    private void updateOptionalRows() {
        int choice = CHOICES[algorithm.getSelectedIndex()];
        setRowEnabled(quantumRow, choice == 3 || choice == 4);
        processes.setColumnEnabled(3, choice == 4 || choice == 6);
    }

    @Override
    public String buildScript() {
        int choice = CHOICES[algorithm.getSelectedIndex()];
        List<String[]> rows = processes.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one process with the Add row button.");
        }

        int n = rows.size();
        String[] processNames = new String[n];
        int[] arrivalTimes = new int[n];
        int[] burstTimes = new int[n];
        int[] pri = new int[n];
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < n; i++) {
            String[] row = rows.get(i);
            String name = row[0].isEmpty() ? "P" + (i + 1) : row[0];
            if (!seen.add(name.toLowerCase())) {
                throw new Ui.InputError("There are two processes called " + name + ".");
            }
            processNames[i] = name;
            arrivalTimes[i] = Integer.parseInt(row[1]);
            burstTimes[i] = Integer.parseInt(row[2]);
            if (choice == 4 || choice == 6) {
                if (row[3].isEmpty()) {
                    throw new Ui.InputError("Process " + name + " needs a priority for this option.");
                }
                pri[i] = Integer.parseInt(row[3]);
            }
        }

        StringBuilder script = new StringBuilder();
        script.append(choice).append('\n');
        script.append(n).append('\n');
        for (int i = 0; i < n; i++) {
            script.append(processNames[i]).append('\n');
            script.append(arrivalTimes[i]).append('\n');
            script.append(burstTimes[i]).append('\n');
        }

        if (choice == 3 || choice == 4) {
            script.append(Ui.parseInt("Time quantum", quantum.getText(), 1, 100)).append('\n');
        }
        if (choice == 4 || choice == 6) {
            for (int value : pri) {
                script.append(value).append('\n');
            }
        }

        script.append("0\n");
        return script.toString();
    }

    @Override
    void afterRun(String reportText) {
        if (reportText != null) {
            chart.load(reportText);
        }
        setPreviewVisible(reportText != null && chart.hasChart());
    }

    @Override
    public void loadExample() {
        algorithm.setSelectedIndex(4);
        quantum.setText("2");
        processes.setRows(new String[][]{
            {"Web", "0", "6", "3"},
            {"DB", "1", "3", "1"},
            {"Mail", "2", "8", "2"},
            {"Cache", "1", "4", "4"},
            {"Log", "3", "5", "2"}
        });
    }
}
