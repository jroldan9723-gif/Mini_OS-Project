package minios.gui;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.PagingSimulator;

// Tab 3: paging. The page table is typed as one frame number per page, so
// the page count is simply how many numbers were entered.
public class PagingPanel extends ModulePanel {

    private final JTextField pageSize = Ui.field(10);
    private final JTextArea frames = Ui.area(6, 30);
    private final JTextArea addresses = Ui.area(6, 30);

    public PagingPanel() {
        super("Paging (logical to physical)",
                "Page table: one frame number per page, in page order. Use -1 for a page that "
                + "is not in memory. Logical addresses: a list of addresses to translate "
                + "(the table is printed first, then each address is answered).",
                PagingSimulator::run);
        addRow("Page size in bytes (e.g. 1024):", pageSize);
        addWideRow(new JLabel("<html><b>Page table</b> - frame number per page (space or line separated)</html>"));
        addWideRow(Ui.scrolled(frames));
        addWideRow(new JLabel("<html><b>Logical addresses</b> to translate (optional list)</html>"));
        addWideRow(Ui.scrolled(addresses));
    }

    // Validates the form and writes the console script: module menu, page
    // size, page count, one frame per line, then the addresses to translate.
    @Override
    public String buildScript() {
        int size = Ui.parseInt("Page size", pageSize.getText(), 1, 1_000_000);
        int[] table = Ui.parseIntList("Page table", frames.getText(), -1, 100000);
        // The address list is optional: an empty box just prints the table.
        int[] addrs = addresses.getText().trim().isEmpty()
                ? new int[0]
                : Ui.parseIntList("Logical addresses", addresses.getText());

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(size).append('\n');
        script.append(table.length).append('\n');
        for (int frame : table) {
            script.append(frame).append('\n');
        }
        for (int addr : addrs) {
            script.append(addr).append('\n');
        }
        // The address loop stops on -1, then the module menu returns.
        script.append("-1\n0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        pageSize.setText("1024");
        frames.setText("-1 0 1 -1 3 20 5 12");
        addresses.setText("0 1024 5000 8192 20000 999999");
    }
}
