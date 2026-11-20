package minios.gui;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.CPUScheduler;

// Tab 1: CPU scheduling. The number of processes follows from the burst
// times the user types, so any list length works.
public class CpuPanel extends ModulePanel {

    // Combo box order matches the console menu numbers.
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
    private final JTextArea bursts = Ui.area(5, 34);
    private final JTextArea arrivals = Ui.area(5, 34);
    private final JTextArea priorities = Ui.area(5, 34);

    public CpuPanel() {
        super("CPU scheduling",
                "Enter burst times as a list (spaces or one per line). SRTF also needs one "
                + "arrival time per process; Priority and the comparison need one priority "
                + "per process (smaller number = higher priority).",
                CPUScheduler::run);
        addRow("Algorithm:", algorithm);
        addRow("Time quantum (Round Robin / comparison):", quantum);
        addWideRow(new JLabel("<html><b>Burst times</b> (each 1..1000)</html>"));
        addWideRow(Ui.scrolled(bursts));
        addWideRow(new JLabel("<html><b>Arrival times</b> - SRTF only (0 or more)</html>"));
        addWideRow(Ui.scrolled(arrivals));
        addWideRow(new JLabel("<html><b>Priorities</b> - Priority and comparison only</html>"));
        addWideRow(Ui.scrolled(priorities));
    }

    @Override
    public String buildScript() {
        int choice = CHOICES[algorithm.getSelectedIndex()];
        int[] burstTimes = Ui.parseIntList("Burst times", bursts.getText(), 1, 1000);
        int n = burstTimes.length;

        StringBuilder script = new StringBuilder();
        script.append(choice).append('\n');

        if (choice == 5) {
            // SRTF: the console asks for arrival, then burst, for each process.
            int[] arrivalTimes = Ui.parseIntList("Arrival times", arrivals.getText(), 0, 1000);
            if (arrivalTimes.length != n) {
                throw new Ui.InputError("SRTF needs one arrival time per burst time (expected "
                        + n + ", saw " + arrivalTimes.length + ").");
            }
            script.append(n).append('\n');
            for (int i = 0; i < n; i++) {
                script.append(arrivalTimes[i]).append('\n');
                script.append(burstTimes[i]).append('\n');
            }
        } else {
            script.append(n).append('\n');
            for (int burst : burstTimes) {
                script.append(burst).append('\n');
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
        }

        // Final 0 = "back to main menu", so the module returns cleanly.
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        algorithm.setSelectedIndex(4);
        bursts.setText("6 3 8 4 5");
        arrivals.setText("0 2 4 1 3");
        priorities.setText("3 1 2 4 2");
        quantum.setText("2");
    }
}
