package minios.gui;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.FragmentationDemo;

public class FragmentationPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JComboBox<String> kind = new JComboBox<>(new String[]{
        "Internal (fixed partitions)",
        "External (variable memory, holes)"
    });
    private final JComboBox<String> strategy = new JComboBox<>(new String[]{
        "First-Fit (first hole that fits)",
        "Best-Fit (smallest hole that fits)",
        "Worst-Fit (largest hole that fits)",
        "Compare all three on the same data"
    });
    private final JTextField memorySize = Ui.field(10);
    private final JTextArea partitions = Ui.area(3, 30);
    private final JTextArea processes = Ui.area(3, 30);
    private final JTextArea jobs = Ui.area(3, 30);
    private final JTextArea frees = Ui.area(3, 30);
    private final JTextField request = Ui.field(10);

    public FragmentationPanel() {
        super("Fragmentation",
                "Pick a demo, then fill only the boxes that demo uses.",
                FragmentationDemo::run);
        addRow("Demo:", kind);
        addRow("Allocation strategy:", strategy);
        addRow("Total memory in KB (external demo):", memorySize);
        addWideRow(new JLabel("<html><b>Partition sizes in KB</b> - internal demo (one list)</html>"));
        addWideRow(Ui.scrolled(partitions));
        addWideRow(new JLabel("<html><b>Process sizes in KB</b> - internal demo (one list)</html>"));
        addWideRow(Ui.scrolled(processes));
        addWideRow(new JLabel("<html><b>Jobs</b> - external demo: \"name size\" per line</html>"));
        addWideRow(Ui.scrolled(jobs));
        addWideRow(new JLabel("<html><b>Jobs to free</b> - external demo: one name per line (optional)</html>"));
        addLastWideRow(frees);
        addRow("New request in KB (external demo):", request);
    }

    @Override
    public String buildScript() {
        int strat = strategy.getSelectedIndex() + 1;
        StringBuilder script = new StringBuilder();

        if (kind.getSelectedIndex() == 0) {
            int[] partSizes = Ui.parseIntList("Partition sizes", partitions.getText(), 1, 100000);
            int[] procSizes = Ui.parseIntList("Process sizes", processes.getText(), 1, 100000);
            script.append("1\n").append(strat).append('\n');
            script.append(partSizes.length).append('\n');
            for (int size : partSizes) {
                script.append(size).append('\n');
            }
            script.append(procSizes.length).append('\n');
            for (int size : procSizes) {
                script.append(size).append('\n');
            }
            script.append("0\n");
            return script.toString();
        }

        int mem = Ui.parseInt("Total memory", memorySize.getText(), 1, 1000000);
        String[] jobRows = Ui.lines(jobs.getText());
        if (jobRows.length == 0) {
            throw new Ui.InputError("Enter at least one job as \"name size\".");
        }
        String[] freeRows = Ui.lines(frees.getText());
        int ask = Ui.parseInt("New request", request.getText(), 1, mem);

        script.append("2\n").append(strat).append('\n');
        script.append(mem).append('\n');
        script.append(jobRows.length).append('\n');
        for (int i = 0; i < jobRows.length; i++) {
            String[] parts = jobRows[i].split("\\s+");
            if (parts.length != 2) {
                throw new Ui.InputError("Job " + (i + 1) + " (\"" + jobRows[i]
                        + "\") needs exactly \"name size\".");
            }
            int size = Ui.parseInt("Size of job " + parts[0], parts[1], 1, mem);
            script.append(parts[0]).append('\n');
            script.append(size).append('\n');
        }
        script.append(freeRows.length).append('\n');
        for (String name : freeRows) {
            if (name.split("\\s+").length != 1) {
                throw new Ui.InputError("Job names to free must be a single word: \"" + name + "\".");
            }
            script.append(name).append('\n');
        }
        script.append(ask).append('\n');
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        kind.setSelectedIndex(1);
        strategy.setSelectedIndex(3);
        memorySize.setText("1000");
        partitions.setText("100 150 250 300");
        processes.setText("80 140 200 60 90");
        jobs.setText("A 200\nB 150\nC 300\nD 100\nE 250");
        frees.setText("B");
        request.setText("180");
    }
}
