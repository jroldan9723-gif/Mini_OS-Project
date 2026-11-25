package minios.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.FragmentationDemo;

// Tab 5. Two demos share this screen. Internal uses a list of partition sizes
// and a list of process sizes; external uses named jobs, the names to free,
// and one new request. Greyed boxes belong to the demo that is not picked.
public class FragmentationPanel extends ModulePanel {

    private final ResultChart chart = new ResultChart();

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
    private final JTextField request = Ui.field(10);

    private final RecordEditor partitions = new RecordEditor(null, 1,
            RecordEditor.Column.number("Size (KB)", 100, 1, 100000));
    private final RecordEditor processes = new RecordEditor(null, 1,
            RecordEditor.Column.number("Size (KB)", 100, 1, 100000));
    private final RecordEditor jobs = new RecordEditor(null, 1,
            RecordEditor.Column.name("Job", 90),
            RecordEditor.Column.number("Size (KB)", 90, 1, 100000));
    private final RecordEditor frees = new RecordEditor(null, 1,
            RecordEditor.Column.name("Job to free", 110));

    private final java.awt.CardLayout cards = new java.awt.CardLayout();
    private JPanel demoBox;

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
        externalRows.add(addSetting("New request (KB):", request));

        // Only one demo is in play at a time, so the editors swap places
        // instead of being squeezed four into a row.
        demoBox = new JPanel(cards);
        demoBox.setOpaque(false);

        JPanel internal = new JPanel(new GridLayout(1, 2, 8, 0));
        internal.setOpaque(false);
        internal.add(cell("Partitions (internal demo)", partitions));
        internal.add(cell("Processes (internal demo)", processes));

        JPanel external = new JPanel(new GridLayout(1, 2, 8, 0));
        external.setOpaque(false);
        external.add(cell("Jobs to load: name and size", jobs));
        external.add(cell("Jobs to free (optional)", frees));

        demoBox.add(internal, "internal");
        demoBox.add(external, "external");

        chart.setEmptyHint("Run a demo to see the memory map.");
        addPreview(chart, "MEMORY MAP", 118);

        JPanel group = addGroup("Demos");
        group.setLayout(new BorderLayout());
        group.add(demoBox, BorderLayout.CENTER);

        internalRows.add(internal);
        externalRows.add(external);

        kind.addActionListener(e -> updateEnabledRows());
        updateEnabledRows();
    }

    private void updateEnabledRows() {
        boolean internal = kind.getSelectedIndex() == 0;
        cards.show(demoBox, internal ? "internal" : "external");
        // the hidden demo is not just disabled, it is out of the way, so the
        // visible editors get the full width of the group
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
            List<String[]> parts = partitions.rows();
            List<String[]> procs = processes.rows();
            if (parts.isEmpty() || procs.isEmpty()) {
                throw new Ui.InputError("The internal demo needs at least one partition and one process.");
            }
            script.append("1\n").append(strat).append('\n');
            script.append(parts.size()).append('\n');
            for (String[] row : parts) {
                script.append(row[0]).append('\n');
            }
            script.append(procs.size()).append('\n');
            for (String[] row : procs) {
                script.append(row[0]).append('\n');
            }
            script.append("0\n");
            return script.toString();
        }

        List<String[]> jobRows = jobs.rows();
        if (jobRows.isEmpty()) {
            throw new Ui.InputError("The external demo needs at least one job.");
        }
        int mem = Ui.parseInt("Total memory", memorySize.getText(), 1, 1000000);
        int ask = Ui.parseInt("New request", request.getText(), 1, mem);

        script.append("2\n").append(strat).append('\n');
        script.append(mem).append('\n');
        script.append(jobRows.size()).append('\n');
        for (String[] row : jobRows) {
            int size = Integer.parseInt(row[1]);
            if (size > mem) {
                throw new Ui.InputError("Job " + row[0] + " wants " + size
                        + " KB but memory is only " + mem + " KB.");
            }
            script.append(row[0]).append('\n');
            script.append(size).append('\n');
        }
        List<String[]> freeRows = frees.rows();
        script.append(freeRows.size()).append('\n');
        for (String[] row : freeRows) {
            script.append(row[0]).append('\n');
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
        request.setText("180");
        partitions.setRows(new String[][]{{"100"}, {"150"}, {"250"}, {"300"}});
        processes.setRows(new String[][]{{"80"}, {"140"}, {"200"}, {"60"}, {"90"}});
        jobs.setRows(new String[][]{
            {"A", "200"}, {"B", "150"}, {"C", "300"}, {"D", "100"}, {"E", "250"}
        });
        frees.setRows(new String[][]{{"B"}});
    }

    @Override
    void afterRun(String reportText) {
        if (reportText != null) {
            chart.loadMemoryMap(lastMemoryMap(reportText));
        }
        setPreviewVisible(reportText != null && chart.hasChart());
    }

    // The demos print several maps; the last one shows the end state.
    private static String lastMemoryMap(String report) {
        String last = null;
        for (String line : report.split("\n")) {
            if (line.contains("Memory map:")) {
                last = line.substring(line.indexOf("Memory map:"));
            }
        }
        return last;
    }
}
