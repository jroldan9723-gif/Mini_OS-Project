package minios.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.PagingSimulator;

// Tab 3. One row per page: the page number is filled in by the table, the
// frame is what the user types. -1 means the page is on disk.
public class PagingPanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextField pageSize = Ui.field(10);

    private final RecordEditor pages = new RecordEditor("Page ", 0,
            RecordEditor.Column.number("Frame", 80, -1, 100000));
    private final RecordEditor addresses = new RecordEditor(null, 1,
            RecordEditor.Column.number("Address", 110, 0, Integer.MAX_VALUE));

    public PagingPanel() {
        super("Paging",
                "Add one row per page. Frame -1 means the page is on disk.",
                PagingSimulator::run);
        addSetting("Page size in bytes:", pageSize);

        JPanel group = addGroup("Page table and addresses");
        group.setLayout(new BorderLayout());
        JPanel lists = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        lists.setOpaque(false);
        lists.add(cell("Page table", pages));
        lists.add(cell("Addresses to translate (optional)", addresses));
        group.add(lists, BorderLayout.CENTER);
    }

    @Override
    public String buildScript() {
        List<String[]> rows = pages.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one page row.");
        }
        int size = Ui.parseInt("Page size", pageSize.getText(), 1, 1_000_000);
        List<String[]> addrRows = addresses.rows();

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(size).append('\n');
        script.append(rows.size()).append('\n');
        for (String[] row : rows) {
            script.append(row[0]).append('\n');
        }
        for (String[] row : addrRows) {
            script.append(row[0]).append('\n');
        }
        script.append("-1\n0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        pageSize.setText("1024");
        pages.setRows(new String[][]{
            {"-1"}, {"0"}, {"1"}, {"-1"}, {"3"}, {"20"}, {"5"}, {"12"}
        });
        addresses.setRows(new String[][]{
            {"0"}, {"1024"}, {"5000"}, {"8192"}, {"20000"}, {"999999"}
        });
    }
}
