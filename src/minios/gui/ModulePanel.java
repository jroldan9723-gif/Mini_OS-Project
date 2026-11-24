package minios.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

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

// One module screen. The form sits in a card on the left, the report in a card
// on the right, and a strip of summary tiles sits above the report. Subclasses
// only add fields and build the input; everything visual is here.
abstract class ModulePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ReportView report = new ReportView();

    private final JPanel form = new JPanel();
    private final JLabel status = new JLabel("Ready");
    private final JLabel meta = new JLabel(" ");
    private final MetricTile tileTime = new MetricTile("Compute time");
    private final MetricTile tileLines = new MetricTile("Input lines");
    private final MetricTile tileRows = new MetricTile("Report lines");

    ModulePanel(String title, String hint, Runner.Module module) {
        setLayout(new BorderLayout(16, 12));
        setBackground(Theme.PAGE);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(372, 100));
        left.add(buildFormCard(title, hint), BorderLayout.CENTER);
        left.add(buildButtons(module), BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);
        add(buildReportColumn(), BorderLayout.CENTER);
    }

    private JPanel buildFormCard(String title, String hint) {
        ShadowCard card = new ShadowCard(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 14, 16));

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);

        JLabel kicker = new JLabel("INPUT");
        kicker.setFont(Theme.TINY);
        kicker.setForeground(Theme.ACCENT);
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(kicker);
        head.add(Box.createVerticalStrut(4));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.H1);
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(heading);

        if (hint != null) {
            head.add(Box.createVerticalStrut(4));
            JLabel note = new JLabel("<html><body style='width: 272px'>" + hint + "</body></html>");
            note.setFont(Theme.SMALL);
            note.setForeground(Theme.MUTED);
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            head.add(note);
        }
        head.add(Box.createVerticalStrut(14));
        card.add(head, BorderLayout.NORTH);

        // Rows stack from the top and never stretch to fill the card.
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(form, BorderLayout.NORTH);

        JScrollPane scroller = new JScrollPane(top);
        scroller.setBorder(null);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroller, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtons(Runner.Module module) {
        JButton run = Ui.primary("Run module");
        run.addActionListener(e -> runModule(module));
        JButton example = Ui.secondary("Load example");
        example.addActionListener(e -> loadExample());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(run);
        row.add(example);
        return row;
    }

    private JPanel buildReportColumn() {
        JPanel column = new JPanel(new BorderLayout(0, 12));
        column.setOpaque(false);
        column.add(buildSummaryStrip(), BorderLayout.NORTH);
        column.add(buildReportCard(), BorderLayout.CENTER);
        return column;
    }

    private JPanel buildSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 10, 0));
        strip.setOpaque(false);
        strip.add(tileTime);
        strip.add(tileLines);
        strip.add(tileRows);
        return strip;
    }

    private JPanel buildReportCard() {
        ShadowCard card = new ShadowCard(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 3, 0));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(14, 18, 10, 18));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel kicker = new JLabel("OUTPUT");
        kicker.setFont(Theme.TINY);
        kicker.setForeground(Theme.ACCENT);
        JLabel title = new JLabel("Report");
        title.setFont(Theme.H1);
        title.setForeground(Theme.INK);
        left.add(kicker);
        left.add(Box.createVerticalStrut(3));
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        meta.setFont(Theme.SMALL);
        meta.setForeground(Theme.MUTED);
        status.setFont(Theme.SMALL);
        status.setForeground(Theme.MUTED);
        right.add(meta);
        right.add(Ui.pill(status));

        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);

        report.setBorder(new EmptyBorder(0, 10, 6, 8));

        card.add(head, BorderLayout.NORTH);
        card.add(report, BorderLayout.CENTER);
        return card;
    }

    // A field with its caption above it, so long captions stay readable and the
    // field can use the full width of the card.
    final JLabel addRow(String labelText, JComponent field) {
        return addRow(labelText, field, 28);
    }

    final JLabel addRow(String labelText, JComponent field, int fieldHeight) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.SMALL);
        label.setForeground(Theme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 1, 4, 0));
        form.add(label);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(field, BorderLayout.CENTER);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        holder.setPreferredSize(new Dimension(200, fieldHeight));
        form.add(holder);
        form.add(Box.createVerticalStrut(11));
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
            caption.setForeground(Theme.TEXT_CAPTION);
            wrapCaption(caption);
            height = Math.max(rowHeight, 40);
        }
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(component, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setPreferredSize(new Dimension(200, height));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(row);
        form.add(Box.createVerticalStrut(9));
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
        label.setText("<html><body style='width: 268px'>" + inner + "</body></html>");
    }

    final JComponent addLastWideRow(JTextArea area) {
        JScrollPane pane = Ui.scrolled(area);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(pane, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        row.setPreferredSize(new Dimension(100, 92));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(row);
        form.add(Box.createVerticalStrut(9));
        return pane;
    }

    // Greys out a whole row, including the caption and the field text.
    static void setRowEnabled(Component row, boolean on) {
        if (row instanceof JLabel) {
            ((JLabel) row).setForeground(on ? Theme.TEXT_CAPTION : Theme.DISABLED);
        }
        if (row instanceof javax.swing.text.JTextComponent) {
            javax.swing.text.JTextComponent t = (javax.swing.text.JTextComponent) row;
            t.setBackground(on ? Color.WHITE : Theme.SOFT);
            t.setForeground(on ? Theme.INK : Theme.DISABLED);
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
        status.setText("Running...");
        status.setForeground(Theme.MUTED);
        long started = System.nanoTime();
        try {
            String script = buildScript();
            String text = Runner.run(module, script);
            report.show(text);
            long ms = Math.max(1, (System.nanoTime() - started) / 1_000_000L);

            int inputLines = script.split("\n", -1).length;
            int reportLines = text.split("\n", -1).length;
            tileTime.set(ms + " ms", "same code as console", Theme.GREEN);
            tileLines.set(String.valueOf(inputLines), "sent to the module", Theme.ACCENT);
            tileRows.set(String.valueOf(reportLines), "shown on the right", Theme.AMBER);

            status.setText("Done");
            status.setForeground(Theme.GREEN);
            meta.setText("Checked " + inputLines + " input lines");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    report.getVerticalScrollBar().setValue(0);
                }
            });
        } catch (Ui.InputError e) {
            status.setText("Needs fixing");
            status.setForeground(Theme.RED);
            meta.setText(" ");
            Ui.showError(this, e.getMessage());
        }
    }

    public abstract String buildScript();

    public abstract void loadExample();
}
