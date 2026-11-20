package minios.gui;

import java.awt.BorderLayout;
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
            addWideRow(new JLabel("<html>" + hint + "</html>"));
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
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = formRows;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(2, 4, 2, 4);
        form.add(component, c);
        formRows++;
    }

    // Turns the current form into input lines and feeds them to the module.
    private void runModule(Runner.Module module) {
        try {
            String script = buildScript();
            String text = Runner.run(module, script);
            output.setText(text + "\n");
            output.setCaretPosition(0);
        } catch (Ui.InputError e) {
            Ui.showError(this, e.getMessage());
        }
    }

    // The exact lines the module will read (validated before we start).
    public abstract String buildScript();

    // Puts a small working dataset into the form, for demos and quick tests.
    public abstract void loadExample();
}
