package minios.gui;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.MultiprogrammingSimulator;

// Tab 8. One row per job. The table keeps the four values of a job together,
// which is easier to check than four lists that must line up.
public class MultiPanel extends ModulePanel {

    private final ResultChart chart = new ResultChart();

    private static final long serialVersionUID = 1L;

    private final JTextField memorySize = Ui.field(10);
    private final JTextField quantum = Ui.field(6);

    private final RecordEditor jobs = new RecordEditor(null, 1,
            RecordEditor.Column.name("Job", 80),
            RecordEditor.Column.number("Memory", 80, 1, 1_000_000),
            RecordEditor.Column.number("Burst", 74, 1, 1000),
            RecordEditor.Column.number("Arrival", 74, 0, 10000));

    public MultiPanel() {
        super("Multiprogramming",
                "Add one row per job: how much memory it needs, its burst and when it arrives.",
                MultiprogrammingSimulator::run);
        addSetting("Main memory (KB):", memorySize);
        addSetting("Time quantum (ticks):", quantum);

                chart.setEmptyHint("Run the simulation to see the final memory map.");
        addPreview(chart, "FINAL MEMORY MAP", 96);

JPanel group = addGroup("Jobs");
        group.setLayout(new BorderLayout());
        group.add(jobs, BorderLayout.CENTER);
    }

    @Override
    public String buildScript() {
        List<String[]> rows = jobs.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one job with the Add row button.");
        }
        int mem = Ui.parseInt("Main memory size", memorySize.getText(), 1, 1_000_000);
        int timeQuantum = Ui.parseInt("Time quantum", quantum.getText(), 1, 100);

        Set<String> seen = new HashSet<>();
        int[] jobMem = new int[rows.size()];
        int[] burst = new int[rows.size()];
        int[] arrival = new int[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String name = rows.get(i)[0];
            if (name.isEmpty()) {
                throw new Ui.InputError("Job " + (i + 1) + ": the name cannot be empty.");
            }
            if (!seen.add(name.toLowerCase())) {
                throw new Ui.InputError("There is already a job called " + name + ".");
            }
            jobMem[i] = Integer.parseInt(rows.get(i)[1]);
            burst[i] = Integer.parseInt(rows.get(i)[2]);
            arrival[i] = Integer.parseInt(rows.get(i)[3]);
            if (jobMem[i] > mem) {
                throw new Ui.InputError("Job " + name + " wants " + jobMem[i]
                        + " KB but the machine only has " + mem + " KB.");
            }
        }

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(mem).append('\n');
        script.append(rows.size()).append('\n');
        for (int i = 0; i < rows.size(); i++) {
            script.append(rows.get(i)[0]).append('\n');
            script.append(jobMem[i]).append('\n');
            script.append(burst[i]).append('\n');
            script.append(arrival[i]).append('\n');
        }
        script.append(timeQuantum).append('\n');
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        memorySize.setText("100");
        quantum.setText("2");
        jobs.setRows(new String[][]{
            {"J1", "40", "7", "0"},
            {"J2", "30", "4", "2"},
            {"J3", "25", "5", "0"},
            {"J4", "20", "3", "5"}
        });
    }

    @Override
    void afterRun(String reportText) {
        if (reportText != null) {
            chart.loadMulti(reportText);
        }
        setPreviewVisible(reportText != null && chart.hasChart());
    }
}
