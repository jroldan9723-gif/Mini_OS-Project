package minios.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import minios.CPUScheduler;

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
                "One row per process. Burst times are required.",
                CPUScheduler::run);
        Ui.styleCombo(algorithm);
        quantum.setColumns(5);
        addSetting("Algorithm:", algorithm);
        quantumRow = addSetting("Time quantum:", quantum);
        JPanel group = addGroup("Processes / Processes");
        group.setLayout(new BorderLayout());
        JPanel lists = new JPanel(new GridLayout(2, 2, 8, 6));
        lists.setOpaque(false);
        lists.setBorder(new EmptyBorder(0, 0, 0, 0));
        lists.add(labelled("Process IDs (optional)", names));
        lists.add(labelled("Arrival times", arrivals));
        lists.add(labelled("Burst times", bursts));
        lists.add(labelled("Priorities (optional)", priorities));
        group.add(lists, BorderLayout.CENTER);
        priorityRow = (JComponent) lists.getComponent(3);

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
