package minios.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    private final List<JComponent> internalRows = new ArrayList<>();
    private final List<JComponent> externalRows = new ArrayList<>();

    public FragmentationPanel() {
        super("Fragmentation",
                "Pick a demo. Greyed boxes belong to the other demo.",
                FragmentationDemo::run);
        Ui.styleCombo(kind);
        Ui.styleCombo(strategy);
        addSetting("Demo:", kind);
        addSetting("Strategy:", strategy);

        externalRows.add(addSetting("Total memory (KB):", memorySize));

        JPanel group = addGroup("Demos");
        group.setLayout(new BorderLayout());
        JPanel lists = new JPanel(new GridLayout(2, 2, 8, 6));
        lists.setOpaque(false);
        lists.add(labelled("Partition sizes", partitions));
        lists.add(labelled("Process sizes", processes));
        lists.add(labelled("Jobs, \"name size\" (external demo)", jobs));
        lists.add(labelled("Jobs to free (optional)", frees));
        group.add(lists, BorderLayout.CENTER);
        externalRows.add((JComponent) lists.getComponent(2));
        internalRows.add((JComponent) lists.getComponent(0));
        internalRows.add((JComponent) lists.getComponent(1));
        externalRows.add((JComponent) lists.getComponent(3));
        externalRows.add(addSetting("New request (KB):", request));

        kind.addActionListener(e -> updateEnabledRows());
        updateEnabledRows();
    }

    private void updateEnabledRows() {
        boolean internal = kind.getSelectedIndex() == 0;
        for (JComponent row : internalRows) {
            setRowEnabled(row, internal);
        }
        for (JComponent row : externalRows) {
            setRowEnabled(row, !internal);
        }
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
