package minios.gui;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.BankersAlgorithm;

// Tab 2. The banker module keeps the data it was given last, so this
// panel remembers what it sent and only resends the matrices when
// they change.
public class BankerPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    final JComboBox<String> mode = new JComboBox<>(new String[]{
        "Safety check (enter the system data)",
        "Resource request (give a process more resources)"
    });
    private final JTextArea allocation = Ui.area(4, 34);
    private final JTextArea maximum = Ui.area(4, 34);
    final JTextField available = Ui.field(24);
    private final JTextField process = Ui.field(6);
    private final JTextField request = Ui.field(24);

    private String loadedData = null;

    public BankerPanel() {
        super("Banker's Algorithm",
                "One row per process. Allocation and Max need the same number of columns; Available is one row.",
                BankersAlgorithm::run);
        addRow("Mode:", mode);
        addWideRow(new JLabel("<html><b>Allocation matrix</b> - one row per process</html>"));
        addWideRow(Ui.scrolled(allocation));
        addWideRow(new JLabel("<html><b>Maximum-need matrix</b> - same shape as allocation</html>"));
        addLastWideRow(maximum);
        addRow("Available resources:", available);
        addRow("Process making the request (1..n):", process);
        addRow("Request vector:", request);
    }

    @Override
    public String buildScript() {
        String data = systemDataScript();
        String snapshot = data;

        StringBuilder script = new StringBuilder();
        if (mode.getSelectedIndex() == 0) {
            script.append("1\n").append(data).append("0\n");
            loadedData = snapshot;
            return script.toString();
        }

        int n = Ui.lines(allocation.getText()).length;
        int proc = Ui.parseInt("Process making the request", process.getText(), 1, n);
        int[] req = Ui.parseIntList("Request vector", request.getText());
        int m = Ui.parseIntList("Available resources", available.getText()).length;
        if (req.length != m) {
            throw new Ui.InputError("The request needs exactly " + m
                    + " number(s), one per resource type.");
        }

        if (snapshot.equals(loadedData)) {
            script.append("2\n").append(proc).append('\n');
            appendList(script, req);
            script.append("0\n");
        } else if (loadedData == null) {
            script.append("2\n").append(data).append(proc).append('\n');
            appendList(script, req);
            script.append("0\n");
            loadedData = snapshot;
        } else {
            script.append("1\n").append(data);
            script.append("2\n").append(proc).append('\n');
            appendList(script, req);
            script.append("0\n");
            loadedData = snapshot;
        }
        return script.toString();
    }

    private String systemDataScript() {
        String[] allocRows = Ui.lines(allocation.getText());
        String[] maxRows = Ui.lines(maximum.getText());
        if (allocRows.length == 0) {
            throw new Ui.InputError("The allocation matrix is empty - enter at least one row.");
        }
        if (maxRows.length != allocRows.length) {
            throw new Ui.InputError("Allocation has " + allocRows.length
                    + " row(s) but Max has " + maxRows.length + ". They must match.");
        }

        int[][] alloc = parseMatrix("Allocation", allocRows);
        int[][] max = parseMatrix("Maximum", maxRows);
        int[] avail = Ui.parseIntList("Available resources", available.getText(), 0,
                Integer.MAX_VALUE);
        int n = alloc.length;
        int m = alloc[0].length;
        if (avail.length != m) {
            throw new Ui.InputError("Available needs exactly " + m
                    + " number(s), one per resource type.");
        }

        StringBuilder data = new StringBuilder();
        data.append(n).append('\n').append(m).append('\n');
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (max[i][j] < alloc[i][j]) {
                    throw new Ui.InputError("Process P" + (i + 1) + " already holds "
                            + alloc[i][j] + " of R" + (j + 1) + " but its Max is only "
                            + max[i][j] + " (Max must be >= Allocation).");
                }
            }
        }
        for (int i = 0; i < n; i++) {
            appendList(data, alloc[i]);
        }
        for (int i = 0; i < n; i++) {
            appendList(data, max[i]);
        }
        appendList(data, avail);
        return data.toString();
    }

    private int[][] parseMatrix(String label, String[] rows) {
        int[] first = Ui.parseIntList(label + " row 1", rows[0], 0, Integer.MAX_VALUE);
        int[][] matrix = new int[rows.length][first.length];
        matrix[0] = first;
        for (int i = 1; i < rows.length; i++) {
            int[] row = Ui.parseIntList(label + " row " + (i + 1), rows[i], 0, Integer.MAX_VALUE);
            if (row.length != first.length) {
                throw new Ui.InputError(label + " row " + (i + 1) + " has " + row.length
                        + " number(s) but row 1 has " + first.length + ". All rows must match.");
            }
            matrix[i] = row;
        }
        return matrix;
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
        allocation.setText("0 1 0\n2 0 0\n3 0 2\n2 1 1\n0 0 2");
        maximum.setText("7 5 3\n3 2 2\n9 0 2\n2 2 2\n4 3 3");
        available.setText("3 3 2");
        process.setText("2");
        request.setText("1 0 2");
    }
}
