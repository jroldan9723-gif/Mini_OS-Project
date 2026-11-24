package minios.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
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
                "One reference string, one frame count. All four algorithms use them.",
                PageReplacementSimulator::run);
        addSetting("Number of frames:", frames);
        JPanel group = addGroup("Reference string");
        group.setLayout(new BorderLayout());
        group.add(labelled("Page numbers, any length", references, 170), BorderLayout.CENTER);
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
