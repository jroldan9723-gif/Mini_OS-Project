package minios.gui;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import minios.PageReplacementSimulator;

// Tab 6: page replacement. All four algorithms run on the same reference
// string and frame count, so only those two things are asked for.
public class PageReplacePanel extends ModulePanel {

    // Swing components are serializable by inheritance, but these panels are
    // never written to a stream; the field only silences the compiler warning.
    private static final long serialVersionUID = 1L;

    private final JTextArea references = Ui.area(4, 40);
    private final JTextField frames = Ui.field(6);

    public PageReplacePanel() {
        super("Page replacement (FIFO, LRU, Optimal, LFU)",
                "Reference string: any number of page numbers (spaces or one per line), "
                + "each >= 0. Frame count: how many frames the RAM has. Every algorithm "
                + "runs on the same data and the results are compared at the end.",
                PageReplacementSimulator::run);
        addRow("Number of frames in RAM:", frames);
        addWideRow(new JLabel("<html><b>Reference string</b> - page numbers, any length</html>"));
        addLastWideRow(references);
    }

    // The console expects the reference string on a single line with the
    // count in front; everything else is one value per line.
    @Override
    public String buildScript() {
        int[] refs = Ui.parseIntList("Reference string", references.getText(), 0,
                Integer.MAX_VALUE);
        int frameCount = Ui.parseInt("Number of frames", frames.getText(), 1,
                Integer.MAX_VALUE);

        StringBuilder script = new StringBuilder();
        script.append("1\n");
        script.append(refs.length).append('\n');
        // The console reads the whole reference string from one line.
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
