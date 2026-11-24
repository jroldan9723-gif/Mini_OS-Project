package minios.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import minios.PageReplacementSimulator;

// Tab 6. One reference string and one frame count. All four
// algorithms run on the same data.
public class PageReplacePanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextField frames = Ui.field(6);

    private final RecordEditor references = new RecordEditor(null, 1,
            RecordEditor.Column.number("Page", 90, 0, Integer.MAX_VALUE));

    public PageReplacePanel() {
        super("Page Replacement",
                "Add the page numbers in the order they are asked for.",
                PageReplacementSimulator::run);
        addSetting("Number of frames:", frames);

        JPanel group = addGroup("Reference string");
        group.setLayout(new BorderLayout());
        group.add(references, BorderLayout.CENTER);
    }

    @Override
    public String buildScript() {
        List<String[]> rows = references.rows();
        if (rows.isEmpty()) {
            throw new Ui.InputError("Add at least one page to the reference string.");
        }
        int frameCount = Ui.parseInt("Number of frames", frames.getText(), 1,
                Integer.MAX_VALUE);

        StringBuilder line = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                line.append(' ');
            }
            line.append(rows.get(i)[0]);
        }

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(rows.size()).append('\n');
        script.append(line).append('\n');
        script.append(frameCount).append('\n');
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        frames.setText("3");
        references.setRows(new String[][]{
            {"7"}, {"0"}, {"1"}, {"2"}, {"0"}, {"3"}, {"0"}, {"4"}, {"2"}, {"3"},
            {"0"}, {"3"}, {"2"}, {"1"}, {"2"}, {"0"}, {"1"}, {"7"}, {"0"}, {"1"}
        });
    }
}
