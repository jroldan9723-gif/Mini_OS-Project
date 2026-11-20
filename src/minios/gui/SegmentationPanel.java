package minios.gui;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.SegmentationSimulator;

// Tab 4. Segments are checked here too, because an overlap would only
// be reported after the module had already started.
public class SegmentationPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextField ramSize = Ui.field(12);
    private final JTextArea segments = Ui.area(4, 30);
    private final JTextArea addresses = Ui.area(4, 30);

    public SegmentationPanel() {
        super("Segmentation",
                "One segment per line as base and limit. Addresses are entered as segment and offset.",
                SegmentationSimulator::run);
        addRow("Physical memory (RAM) size in bytes:", ramSize);
        addWideRow(new JLabel("<html><b>Segment table</b> - \"base limit\" per line</html>"));
        addWideRow(Ui.scrolled(segments));
        addWideRow(new JLabel("<html><b>Addresses</b> - \"segment offset\" per line</html>"));
        addLastWideRow(addresses);
    }

    @Override
    public String buildScript() {
        long ram = Ui.parseInt("RAM size", ramSize.getText(), 1, 1_000_000_000);
        String[] rows = Ui.lines(segments.getText());
        if (rows.length == 0) {
            throw new Ui.InputError("The segment table is empty - enter at least one \"base limit\" line.");
        }

        long[] base = new long[rows.length];
        long[] limit = new long[rows.length];
        for (int i = 0; i < rows.length; i++) {
            int[] pair = Ui.parseIntList("Segment " + i, rows[i]);
            if (pair.length != 2) {
                throw new Ui.InputError("Segment " + i + " needs exactly two numbers: \"base limit\".");
            }
            base[i] = pair[0];
            limit[i] = pair[1];
            if (base[i] < 0 || limit[i] <= 0) {
                throw new Ui.InputError("Segment " + i + ": base must be >= 0 and limit must be > 0.");
            }
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
        script.append(rows.length).append('\n');
        script.append(ram).append('\n');
        for (int i = 0; i < rows.length; i++) {
            script.append(base[i]).append('\n');
            script.append(limit[i]).append('\n');
        }

        String[] addrRows = Ui.lines(addresses.getText());
        for (String row : addrRows) {
            int[] pair = Ui.parseIntList("Address", row);
            if (pair.length != 2) {
                throw new Ui.InputError("Address \"" + row + "\" needs exactly two numbers: \"segment offset\".");
            }
            if (pair[0] == -1) {
                throw new Ui.InputError("-1 is reserved for stopping the address loop; do not enter it as a segment.");
            }
            script.append(pair[0]).append('\n');
            script.append(pair[1]).append('\n');
        }
        script.append("-1\n0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        ramSize.setText("1024");
        segments.setText("0 100\n100 200\n400 150");
        addresses.setText("0 50\n1 999\n2 0\n5 0");
    }
}
