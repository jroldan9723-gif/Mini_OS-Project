package minios.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

// One module screen. The form sits in a card on the left, the report in a card
// on the right, and the two buttons are under the form. Subclasses only add
// fields and build the input; everything visual is here.
abstract class ModulePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    final JTextArea output = Ui.outputArea();

    private final JPanel form = new JPanel();
    private final JLabel status = new JLabel("Ready");
    private final JLabel meta = new JLabel(" ");
    private final JLabel summary = new JLabel(" ");

    ModulePanel(String title, String hint, Runner.Module module) {
        setLayout(new BorderLayout(14, 0));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(340, 100));
        left.add(buildFormCard(title, hint), BorderLayout.CENTER);
        left.add(buildButtons(module), BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);
        add(buildOutputCard(), BorderLayout.CENTER);
    }

    private JPanel buildFormCard(String title, String hint) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(14, 14, 12, 14)));

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.SECTION);
        heading.setForeground(Theme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(heading);
        if (hint != null) {
            head.add(Box.createVerticalStrut(3));
            JLabel note = new JLabel("<html><body style='width: 330px'>" + hint + "</body></html>");
            note.setFont(Theme.SMALL);
            note.setForeground(Theme.MUTED);
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            head.add(note);
        }
        head.add(Box.createVerticalStrut(12));
        card.add(head, BorderLayout.NORTH);

        // Rows stack from the top and never stretch to fill the card, which is
        // what made the first version look empty in the middle.
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(form, BorderLayout.NORTH);
        form.setAlignmentY(Component.TOP_ALIGNMENT);

        JScrollPane scroller = new JScrollPane(top);
        scroller.setBorder(null);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroller, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtons(Runner.Module module) {
        JButton run = Ui.primary("Run");
        run.addActionListener(e -> runModule(module));
        JButton example = Ui.secondary("Load example");
        example.addActionListener(e -> loadExample());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(run);
        row.add(example);
        return row;
    }

    private JPanel buildOutputCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createLineBorder(Theme.LINE));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.CARD);
        head.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel title = new JLabel("Results");
        title.setFont(Theme.SECTION);
        title.setForeground(Theme.TEXT);

        status.setFont(Theme.SMALL);
        status.setForeground(Theme.MUTED);
        meta.setFont(Theme.SMALL);
        meta.setForeground(Theme.MUTED);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(meta);
        right.add(status);

        head.add(title, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);

        output.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        output.setBackground(Theme.CARD);
        JScrollPane scroller = new JScrollPane(output);
        scroller.setBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(14);
        scroller.getHorizontalScrollBar().setUnitIncrement(14);

        summary.setFont(Theme.SMALL);
        summary.setForeground(Theme.MUTED);
        summary.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));

        card.add(head, BorderLayout.NORTH);
        card.add(scroller, BorderLayout.CENTER);
        card.add(summary, BorderLayout.SOUTH);
        return card;
    }

    // A labelled row: the caption keeps a fixed width, the field takes the
    // rest. Fixed caption width is what keeps every screen lined up.
    // A field with its caption above it. Stacking keeps long captions readable
    // and lets the field use the full width of the card.
    final JLabel addRow(String labelText, JComponent field) {
        return addRow(labelText, field, 26);
    }

    final JLabel addRow(String labelText, JComponent field, int fieldHeight) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.SMALL);
        label.setForeground(Theme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 1, 3, 0));
        form.add(label);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(field, BorderLayout.CENTER);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        holder.setPreferredSize(new Dimension(200, fieldHeight));
        form.add(holder);
        form.add(Box.createVerticalStrut(10));
        return label;
    }

    final JComponent addWideRow(JComponent component) {
        return addWideRow(component, 34);
    }

    final JComponent addWideRow(JComponent component, int rowHeight) {
        int height = rowHeight;
        if (component instanceof JLabel) {
            JLabel caption = (JLabel) component;
            caption.setFont(Theme.SMALL);
            caption.setForeground(Theme.TEXT);
            wrapCaption(caption);
            // two wrapped lines need room, otherwise one of them is cut off
            height = Math.max(rowHeight, 46);
        }
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(component, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setPreferredSize(new Dimension(200, height));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(row);
        form.add(Box.createVerticalStrut(8));
        return component;
    }

    // Captions in the left column get a fixed HTML width, so a long one wraps
    // onto a second line instead of being cut off at the card edge.
    private static void wrapCaption(JLabel label) {
        String text = label.getText();
        String inner = text.startsWith("<html>") ? text.substring(6) : text;
        if (inner.endsWith("</html>")) {
            inner = inner.substring(0, inner.length() - 7);
        }
        label.setText("<html><body style='width: 250px'>" + inner + "</body></html>");
    }

    final JComponent addLastWideRow(JTextArea area) {
        JScrollPane pane = Ui.scrolled(area);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(pane, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        row.setPreferredSize(new Dimension(100, 96));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(row);
        form.add(Box.createVerticalStrut(6));
        return pane;
    }

    // Greys out a whole row, including the caption and the field text.
    static void setRowEnabled(Component row, boolean on) {
        if (row instanceof JLabel) {
            ((JLabel) row).setForeground(on ? Theme.TEXT : Theme.DISABLED);
        }
        if (row instanceof JTextArea || row instanceof javax.swing.JTextField) {
            row.setBackground(on ? Color.WHITE : Theme.SOFT);
        }
        if (row instanceof JTextArea || row instanceof javax.swing.text.JTextComponent) {
            ((javax.swing.text.JTextComponent) row).setForeground(on ? Theme.TEXT : Theme.DISABLED);
        }
        if (row instanceof JComponent) {
            ((JComponent) row).setEnabled(on);
        }
        if (row instanceof Container) {
            for (Component child : ((Container) row).getComponents()) {
                setRowEnabled(child, on);
            }
        }
    }

    // Turns the form into input for the module, then shows what came back. If a
    // field is wrong nothing runs and a message box explains why.
    private void runModule(Runner.Module module) {
        status.setText("Running");
        status.setForeground(Theme.MUTED);
        long started = System.nanoTime();
        try {
            String script = buildScript();
            String text = Runner.run(module, script);
            output.setText(text + "\n");
            long ms = Math.max(1, (System.nanoTime() - started) / 1_000_000L);
            status.setText("Done in " + ms + " ms");
            status.setForeground(Theme.OK);
            meta.setText(script.split("\n", -1).length + " input lines");
            summary.setText(summarise(text));
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    output.setCaretPosition(0);
                }
            });
        } catch (Ui.InputError e) {
            status.setText("Check the input");
            status.setForeground(Theme.WARN);
            meta.setText(" ");
            summary.setText("The report was not changed, because the form was not valid.");
            Ui.showError(this, e.getMessage());
        }
    }

    // The last meaningful line of a report is the verdict in every module, so
    // it makes a useful one-line summary under the output.
    private static String summarise(String text) {
        String last = "";
        for (String line : text.split("\n")) {
            String t = line.trim();
            if (!t.isEmpty()) {
                last = t;
            }
        }
        if (last.length() > 110) {
            last = last.substring(0, 107) + "...";
        }
        return last.isEmpty() ? " " : last;
    }

    public abstract String buildScript();

    public abstract void loadExample();
}
