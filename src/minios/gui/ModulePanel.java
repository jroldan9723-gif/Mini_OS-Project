package minios.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

// The shared shape of every module tab: a form on top, a Run button and a
// "Load example" button at the bottom, and the captured console output in
// the middle. Subclasses add their fields and build the input script.
abstract class ModulePanel extends JPanel {

    final JTextArea output = Ui.outputArea();
    private final JPanel form = new JPanel(new GridBagLayout());
    private int formRows = 0;

    ModulePanel(String title, String hint, Runner.Module module) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        form.setBorder(BorderFactory.createTitledBorder(title));
        if (hint != null) {
            // The width stops the label from stretching the whole form wider
            // than the window (which also squeezed the input boxes flat).
            addWideRow(new JLabel("<html><body style='width: 700px'>" + hint + "</body></html>"));
        }

        output.setBorder(BorderFactory.createTitledBorder(
                "Output (the same engine as the console version)"));

        JButton run = new JButton("Run");
        run.addActionListener(e -> runModule(module));
        JButton example = new JButton("Load example");
        example.addActionListener(e -> loadExample());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(run);
        buttons.add(example);

        // The form sits NORTH but is still allowed to shrink a little, so the
        // output pane always keeps a usable height.
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    // Adds one "label + field" row to the form.
    final void addRow(String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = formRows;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        form.add(new JLabel(labelText), c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, c);
        formRows++;
    }

    // Adds a full-width row (a hint label or a text area).
    final void addWideRow(JComponent component) {
        addWideRow(component, GridBagConstraints.HORIZONTAL);
    }

    // Same, with a chosen fill. Panels whose form has nothing else stretchable
    // give the last input box VERTICAL fill, so spare height goes there
    // instead of leaving a gap under the form.
    final void addWideRow(JComponent component, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = formRows;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = fill;
        c.weightx = 1.0;
        c.insets = new Insets(2, 4, 2, 4);
        form.add(component, c);
        formRows++;
    }

    // Adds the last input box so it takes whatever height is left in the form;
    // without this the form would leave a gap under its fields.
    final void addLastWideRow(JTextArea area) {
        addWideRow(Ui.scrolled(area), GridBagConstraints.BOTH);
    }

    // The form may grow, but only up to half of the tab, so the output pane
    // keeps most of the window (the tables and reports are the point).
    @Override
    public Dimension getPreferredSize() {
        Dimension mine = super.getPreferredSize();
        Container parent = getParent();
        if (parent != null) {
            int cap = Math.max(320, parent.getHeight() / 2);
            mine.height = Math.min(mine.height, cap);
        }
        return mine;
    }

    // Turns the current form into input lines and feeds them to the module.
    private void runModule(Runner.Module module) {
        try {
            String script = buildScript();
            String text = Runner.run(module, script);
            output.setText(text + "\n");
            // Scroll back to the top once the new text has been laid out,
            // so long reports start at the menu instead of the last page.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    output.setCaretPosition(0);
                }
            });
        } catch (Ui.InputError e) {
            Ui.showError(this, e.getMessage());
        }
    }

    // The exact lines the module will read (validated before we start).
    public abstract String buildScript();

    // Puts a small working dataset into the form, for demos and quick tests.
    public abstract void loadExample();
}
