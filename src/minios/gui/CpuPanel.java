package minios.gui;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.CPUScheduler;

// Tab 1. The lists are read from the text boxes, so the number of processes is
// whatever the user typed, and the quantum and priority rows only light up for
// the algorithms that use them.
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
    private final JTextArea names = Ui.area(3, 34);
    private final JTextArea arrivals = Ui.area(3, 34);
    private final JTextArea bursts = Ui.area(3, 34);
    private final JTextArea priorities = Ui.area(3, 34);

    private JComponent quantumRow;
    private JComponent priorityRow;

    public CpuPanel() {
        super("CPU Scheduling",
                "One line per process: ID (optional), arrival time, burst time. Greyed-out boxes are unused.",
                CPUScheduler::run);
        addRow("Algorithm:", algorithm);
        quantumRow = addRow("Time quantum (Round Robin / comparison):", quantum);
        addWideRow(new JLabel("<html><b>Process IDs</b> - optional, one per line (defaults P1, P2, ...)</html>"));
        addWideRow(Ui.scrolled(names));
        addWideRow(new JLabel("<html><b>Arrival times</b> - one per process (0 or more)</html>"));
        addWideRow(Ui.scrolled(arrivals));
        addWideRow(new JLabel("<html><b>Burst times</b> - one per process (each 1..1000)</html>"));
        addWideRow(Ui.scrolled(bursts));
        priorityRow = addRow("Priorities (Priority / comparison):", Ui.scrolled(priorities));

        algorithm.addActionListener(e -> updateOptionalRows());
        updateOptionalRows();
    }

    // 3 is Round Robin, 4 is the comparison, 6 is Priority.
    private void updateOptionalRows() {
        int choice = CHOICES[algorithm.getSelectedIndex()];
        setRowEnabled(quantumRow, choice == 3 || choice == 4);
        setRowEnabled(priorityRow, choice == 4 || choice == 6);
    }

    @Override
    public String buildScript() {
        int choice = CHOICES[algorithm.getSelectedIndex()];
        int[] burstTimes = Ui.parseIntList("Burst times", bursts.getText(), 1, 1000);
        int n = burstTimes.length;

        int[] arrivalTimes = Ui.parseIntList("Arrival times", arrivals.getText(), 0, 1000);
        if (arrivalTimes.length != n) {
            throw new Ui.InputError("Need one arrival time per process (expected "
                    + n + ", saw " + arrivalTimes.length + ").");
        }

        String[] nameRows = Ui.lines(names.getText());
        String[] processNames = new String[n];
        if (nameRows.length == 0) {
            for (int i = 0; i < n; i++) {
                processNames[i] = "";
            }
        } else {
            if (nameRows.length != n) {
                throw new Ui.InputError("Either leave the names empty or give one name per "
                        + "process (expected " + n + ", saw " + nameRows.length + ").");
            }
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < n; i++) {
                String name = nameRows[i];
                if (name.length() > 10) {
                    throw new Ui.InputError("Keep process names to 10 characters: \"" + name + "\".");
                }
                if (!seen.add(name.toLowerCase())) {
                    throw new Ui.InputError("There is already a process called " + name + ".");
                }
                processNames[i] = name;
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
            int[] pri = Ui.parseIntList("Priorities", priorities.getText());
            if (pri.length != n) {
                throw new Ui.InputError("Expected one priority per process (expected "
                        + n + ", saw " + pri.length + ").");
            }
            for (int value : pri) {
                script.append(value).append('\n');
            }
        }

        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        algorithm.setSelectedIndex(4);
        names.setText("Web\nDB\nMail\nCache\nLog");
        arrivals.setText("0\n1\n2\n1\n3");
        bursts.setText("6 3 8 4 5");
        priorities.setText("3 1 2 4 2");
        quantum.setText("2");
    }
}
