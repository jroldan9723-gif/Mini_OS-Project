package minios.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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

// Layout every tab shares: the form on top, the results in the middle, the
// buttons at the bottom. The subclasses only add fields and build the input.
abstract class ModulePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    final JTextArea output = Ui.outputArea();
    private final JPanel form = new JPanel(new GridBagLayout());
    private int formRows = 0;

    ModulePanel(String title, String hint, Runner.Module module) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        form.setBorder(BorderFactory.createTitledBorder(title));
        if (hint != null) {
            addWideRow(new JLabel("<html><body style='width: 700px'>" + hint + "</body></html>"));
        }

        output.setBorder(BorderFactory.createTitledBorder("Results"));

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

    final JLabel addRow(String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = formRows;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        JLabel label = new JLabel(labelText);
        form.add(label, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, c);
        formRows++;
        return label;
    }

    final JComponent addWideRow(JComponent component) {
        return addWideRow(component, GridBagConstraints.HORIZONTAL);
    }

    final JComponent addWideRow(JComponent component, int fill) {
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
        return component;
    }

    // Greys out a whole row, including the label and the text inside a scroll
    // pane. Used by the fragmentation tab, where only one demo is active.
    static void setRowEnabled(Component row, boolean on) {
        row.setEnabled(on);
        if (row instanceof Container) {
            for (Component child : ((Container) row).getComponents()) {
                setRowEnabled(child, on);
            }
        }
    }

    final JComponent addLastWideRow(JTextArea area) {
        return addWideRow(Ui.scrolled(area), GridBagConstraints.BOTH);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension mine = super.getPreferredSize();
        Container parent = getParent();
        if (parent != null) {
            // Leave room for the results below, but never cut off the last field.
            int cap = Math.max(320, parent.getHeight() - 250);
            mine.height = Math.min(mine.height, cap);
        }
        return mine;
    }

    // Turns the form into input for the module, then shows what came back. If a
    // field is wrong nothing runs and a message box explains why.
    private void runModule(Runner.Module module) {
        try {
            String script = buildScript();
            String text = Runner.run(module, script);
            output.setText(text + "\n");
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

    public abstract String buildScript();

    public abstract void loadExample();
}
