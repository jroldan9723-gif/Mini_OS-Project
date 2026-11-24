package minios.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.SegmentationSimulator;

// Tab 4. Segments are checked here too, because an overlap would only be
// reported after the module had already started.
public class SegmentationPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextField ramSize = Ui.field(12);

    private final RecordEditor segments = new RecordEditor("Seg ", 0,
            RecordEditor.Column.number("Base", 90, 0, Integer.MAX_VALUE),
            RecordEditor.Column.number("Limit", 90, 1, Integer.MAX_VALUE));
    private final RecordEditor addresses = new RecordEditor(null, 1,
            RecordEditor.Column.number("Segment", 90, 0, Integer.MAX_VALUE),
            RecordEditor.Column.number("Offset", 90, 0, Integer.MAX_VALUE));

    public SegmentationPanel() {
        super("Segmentation",
                "Add one row per segment, then the addresses to look up.",
                SegmentationSimulator::run);
        addSetting("Physical memory (bytes):", ramSize);

        JPanel group = addGroup("Segment table and addresses");
        group.setLayout(new BorderLayout());
        JPanel lists = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        lists.setOpaque(false);
        lists.add(cell("Segments", segments));
        lists.add(cell("Addresses (optional)", addresses));
        group.add(lists, BorderLayout.CENTER);
    }

    @Override
    public String buildScript() {
        List<String[]> rows = segments.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one segment row.");
        }
        long ram = Ui.parseInt("RAM size", ramSize.getText(), 1, 1_000_000_000);

        int n = rows.size();
        long[] base = new long[n];
        long[] limit = new long[n];
        for (int i = 0; i < n; i++) {
            String[] row = rows.get(i);
            base[i] = Long.parseLong(row[0]);
            limit[i] = Long.parseLong(row[1]);
            long end = base[i] + limit[i] - 1;
            if (end >= ram) {
                throw new Ui.InputError("Segment " + i + " ends at " + end
                        + ", which is outside RAM (" + ram + " bytes).");
            }
            for (int j = 0; j < i; j++) {
                if (base[i] < base[j] + limit[j] && base[j] < base[i] + limit[i]) {
                    throw new Ui.InputError("Segment " + i + " [" + base[i] + ".." + end
                            + "] overlaps segment " + j + " [" + base[j] + ".."
                            + (base[j] + limit[j] - 1) + "].");
                }
            }
        }

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(n).append('\n');
        script.append(ram).append('\n');
        for (int i = 0; i < n; i++) {
            script.append(base[i]).append('\n');
            script.append(limit[i]).append('\n');
        }

        for (String[] row : addresses.rows()) {
            int segment = Integer.parseInt(row[0]);
            if (segment == -1) {
                throw new Ui.InputError("-1 is reserved for stopping the address loop; do not enter it as a segment.");
            }
            script.append(segment).append('\n');
            script.append(row[1]).append('\n');
        }
        script.append("-1\n0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        ramSize.setText("1024");
        segments.setRows(new String[][]{
            {"0", "100"}, {"100", "200"}, {"400", "150"}
        });
        addresses.setRows(new String[][]{
            {"0", "50"}, {"1", "999"}, {"2", "0"}, {"5", "0"}
        });
    }
}
