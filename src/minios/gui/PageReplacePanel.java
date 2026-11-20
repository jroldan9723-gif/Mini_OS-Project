package minios.gui;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.PageReplacementSimulator;

// Tab 6. One reference string and one frame count. All four
// algorithms run on the same data.
public class PageReplacePanel extends ModulePanel {

    private static final long serialVersionUID = 1L;

    private final JTextArea references = Ui.area(4, 40);
    private final JTextField frames = Ui.field(6);

    public PageReplacePanel() {
        super("Page Replacement",
                "Reference string and number of frames. All four algorithms run on the same data.",
                PageReplacementSimulator::run);
        addRow("Number of frames in RAM:", frames);
        addWideRow(new JLabel("<html><b>Reference string</b> - page numbers, any length</html>"));
        addLastWideRow(references);
    }

    @Override
    public String buildScript() {
        int[] refs = Ui.parseIntList("Reference string", references.getText(), 0,
                Integer.MAX_VALUE);
        int frameCount = Ui.parseInt("Number of frames", frames.getText(), 1,
                Integer.MAX_VALUE);

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(refs.length).append('\n');
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < refs.length; i++) {
            if (i > 0) {
                line.append(' ');
            }
            line.append(refs[i]);
        }
        script.append(line).append('\n');
        script.append(frameCount).append('\n');
        script.append("0\n");
        return script.toString();
    }

    @Override
    public void loadExample() {
        frames.setText("3");
        references.setText("7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1");
    }
}
