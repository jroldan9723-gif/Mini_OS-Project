package minios.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.BankersAlgorithm;

// Tab 2. Each process is one row holding everything the algorithm needs:
// what it already holds and the most it will ever ask for. Rows are added
// with the Add row button like every other module.
public class BankerPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    final JComboBox<String> mode = new JComboBox<>(new String[]{
        "Safety check (enter the system data)",
        "Resource request (give a process more resources)"
    });
    final JTextField available = Ui.field(24);
    private final JTextField process = Ui.field(6);
    private final JTextField request = Ui.field(24);

    private final RecordEditor table = new RecordEditor("P", 1,
            RecordEditor.Column.vector("Allocation", 120),
            RecordEditor.Column.vector("Maximum", 120));

    private JComponent processRow;
    private JComponent requestRow;

    private String loadedData = null;

    public BankerPanel() {
        super("Banker's Algorithm",
                "One row per process: what it holds now, then the most it may ask for.",
                BankersAlgorithm::run);
        Ui.styleCombo(mode);
        addSetting("Mode:", mode);

        JPanel group = addGroup("Processes");
        group.setLayout(new BorderLayout());
        group.add(table, BorderLayout.CENTER);

        available.setPreferredSize(new java.awt.Dimension(120, 26));
        addSetting("Available:", available);
        processRow = addSetting("Request from process:", process);
        requestRow = addSetting("Request vector:", request);

        mode.addActionListener(e -> updateRequestRows());
        updateRequestRows();
    }

    private void updateRequestRows() {
        boolean asking = mode.getSelectedIndex() == 1;
        setRowEnabled(processRow, asking);
        setRowEnabled(requestRow, asking);
    }

    @Override
    public String buildScript() {
        String data = systemDataScript();

        StringBuilder script = new StringBuilder();
        if (mode.getSelectedIndex() == 0) {
            script.append("1\n").append(data).append("0\n");
            loadedData = data;
            return script.toString();
        }

        List<String[]> rows = table.rows();
        int proc = Ui.parseInt("Process making the request", process.getText(), 1, rows.size());
        int[] req = Ui.parseIntList("Request vector", request.getText());
        int[] avail = Ui.parseIntList("Available resources", available.getText());
        if (req.length != avail.length) {
            throw new Ui.InputError("The request needs exactly " + avail.length
                    + " number(s), one per resource type.");
        }

        if (data.equals(loadedData)) {
            script.append("2\n").append(proc).append('\n');
            appendList(script, req);
            script.append("0\n");
        } else if (loadedData == null) {
            script.append("2\n").append(data).append(proc).append('\n');
            appendList(script, req);
            script.append("0\n");
        } else {
            script.append("1\n").append(data);
            script.append("2\n").append(proc).append('\n');
            appendList(script, req);
            script.append("0\n");
        }
        loadedData = data;
        return script.toString();
    }

    private String systemDataScript() {
        List<String[]> rows = table.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one process with the Add row button.");
        }

        int n = rows.size();
        int m = -1;
        int[][] alloc = new int[n][];
        int[][] max = new int[n][];
        for (int i = 0; i < n; i++) {
            int[] a = parseRow("Allocation", rows.get(i)[0], i);
            int[] x = parseRow("Maximum", rows.get(i)[1], i);
            if (m < 0) {
                m = a.length;
            }
            alloc[i] = a;
            max[i] = x;
        }

        int[] avail = Ui.parseIntList("Available resources", available.getText(), 0,
                Integer.MAX_VALUE);
        if (avail.length != m) {
            throw new Ui.InputError("Available needs exactly " + m
                    + " number(s), one per resource type.");
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (max[i][j] < alloc[i][j]) {
                    throw new Ui.InputError("Row P" + (i + 1) + " already holds "
                            + alloc[i][j] + " but its maximum is only " + max[i][j]
                            + " (the maximum must be at least the allocation).");
                }
            }
        }

        StringBuilder data = new StringBuilder();
        data.append(n).append('\n').append(m).append('\n');
        for (int i = 0; i < n; i++) {
            appendList(data, alloc[i]);
        }
        for (int i = 0; i < n; i++) {
            appendList(data, max[i]);
        }
        appendList(data, avail);
        return data.toString();
    }

    private int[] parseRow(String label, String text, int row) {
        int[] values = Ui.parseIntList(label + " for row P" + (row + 1), text, 0,
                Integer.MAX_VALUE);
        int width = Ui.parseIntList("Allocation for row P1", table.rows().get(0)[0], 0,
                Integer.MAX_VALUE).length;
        if (values.length != width) {
            throw new Ui.InputError(label + " for row P" + (row + 1) + " has " + values.length
                    + " number(s) but row P1 has " + width + ". Every row needs the same count.");
        }
        return values;
    }

    private static void appendList(StringBuilder script, int[] values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(' ');
            }
            line.append(values[i]);
        }
        script.append(line).append('\n');
    }

    @Override
    public void loadExample() {
        mode.setSelectedIndex(0);
        available.setText("3 3 2");
        process.setText("2");
        request.setText("1 0 2");
        table.setRows(new String[][]{
            {"0 1 0", "7 5 3"},
            {"2 0 0", "3 2 2"},
            {"3 0 2", "9 0 2"},
            {"2 1 1", "2 2 2"},
            {"0 0 2", "4 3 3"}
        });
    }
}
