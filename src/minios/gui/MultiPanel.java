package minios.gui;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.MultiprogrammingSimulator;

// Tab 8. One line per job in the form, four lines per job in the
// input that is built.
public class MultiPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextField memorySize = Ui.field(10);
    private final JTextArea jobs = Ui.area(5, 34);
    private final JTextField quantum = Ui.field(6);

    public MultiPanel() {
        super("Multiprogramming",
                "One job per line: name, memory in KB, CPU burst, arrival time. Waiting time counts the ticks in the job, ready and I/O queues, not the time running or on the device.",
                MultiprogrammingSimulator::run);
        addRow("Main memory size (KB):", memorySize);
        addRow("Time quantum for the CPU (ticks):", quantum);
        addWideRow(new JLabel("<html><b>Jobs</b> - \"name mem burst arrival\" per line</html>"));
        addLastWideRow(jobs);
    }

    @Override
    public String buildScript() {
        int mem = Ui.parseInt("Main memory size", memorySize.getText(), 1, 1_000_000);
        int timeQuantum = Ui.parseInt("Time quantum", quantum.getText(), 1, 100);
        String[] rows = Ui.lines(jobs.getText());
        if (rows.length == 0) {
            throw new Ui.InputError("Enter at least one job as \"name mem burst arrival\".");
        }

        Set<String> seen = new HashSet<>();
        int[][] numbers = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] parts = rows[i].split("\\s+");
            if (parts.length != 4) {
                throw new Ui.InputError("Job " + (i + 1) + " (\"" + rows[i]
                        + "\") needs four values: name mem burst arrival.");
            }
            String name = parts[0];
            if (name.isEmpty()) {
                throw new Ui.InputError("Job " + (i + 1) + ": the name cannot be empty.");
            }
            if (!seen.add(name.toLowerCase())) {
                throw new Ui.InputError("There is already a job called " + name + ".");
            }
            int jobMem = Ui.parseInt("Memory of " + name, parts[1], 1, mem);
            int burst = Ui.parseInt("CPU burst of " + name, parts[2], 1, 1000);
            int arrival = Ui.parseInt("Arrival of " + name, parts[3], 0, 10000);
            numbers[i] = new int[]{jobMem, burst, arrival};
        }

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(mem).append('\n');
        script.append(rows.length).append('\n');
        for (int i = 0; i < rows.length; i++) {
            String[] parts = rows[i].split("\\s+");
            script.append(parts[0]).append('\n');
            script.append(numbers[i][0]).append('\n');
            script.append(numbers[i][1]).append('\n');
            script.append(numbers[i][2]).append('\n');
        }
        script.append(timeQuantum).append('\n');
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        memorySize.setText("100");
        quantum.setText("2");
        jobs.setText("J1 40 7 0\nJ2 30 4 2\nJ3 25 5 0\nJ4 20 3 5");
    }
}
