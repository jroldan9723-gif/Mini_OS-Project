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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

// One module screen. A strip of one line settings runs across the top, the
// input groups fill the left column, and the report takes the right column.
// Subclasses only add fields and build the input; the frame is the same on
// every tab.
abstract class ModulePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Used by the window footer so a run can report up to the status line.
    interface StatusSink {
        void update(String message, int tone);
    }

    private final ReportView report = new ReportView();
    private final JLabel resultLabel = new JLabel("No run yet");

    private final JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
    private final JPanel groupRow = new JPanel(new GridLayout(1, 1, 10, 0));

    private StatusSink sink;

    ModulePanel(String title, String hintText, Runner.Module module) {
        setLayout(new BorderLayout());
        setBackground(Theme.WINDOW);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        strip.setBackground(Theme.HEADER_BG);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(2, 10, 2, 10)));
        // one line only: a wrapped strip would be clipped by the layout
        strip.setLayout(new javax.swing.BoxLayout(strip, javax.swing.BoxLayout.LINE_AXIS));

        groupRow.setBackground(Theme.WINDOW);
        groupRow.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Head, then the settings strip across the whole width, then the two
        // columns: input groups on the left, report on the right.
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.WINDOW);
        body.add(strip, BorderLayout.NORTH);
        body.add(buildLeft(), BorderLayout.WEST);
        body.add(buildRight(), BorderLayout.CENTER);

        add(buildHeading(title, hintText, module), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildHeading(String title, String hintText, Runner.Module module) {
        JPanel head = new JPanel(new BorderLayout(12, 0));
        head.setBackground(Theme.WINDOW);
        head.setBorder(new EmptyBorder(9, 12, 8, 10));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.APP_TITLE);
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(heading);
        if (hintText != null) {
            JLabel hint = new JLabel(hintText);
            hint.setFont(Theme.HINT);
            hint.setForeground(Theme.MUTED);
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(hint);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        JButton run = Ui.primary("Run module");
        run.addActionListener(e -> runModule(module));
        JButton example = Ui.button("Load example");
        example.addActionListener(e -> loadExample());
        buttons.add(run);
        buttons.add(example);

        head.add(text, BorderLayout.WEST);
        head.add(buttons, BorderLayout.EAST);
        return head;
    }

    private JPanel buildLeft() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Theme.WINDOW);
        left.setPreferredSize(new Dimension(500, 100));
        left.setBorder(new EmptyBorder(0, 10, 10, 6));
        left.add(groupRow, BorderLayout.CENTER);
        return left;
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setBackground(Theme.WINDOW);
        right.setBorder(new EmptyBorder(0, 0, 10, 10));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.HEADER_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(3, 8, 3, 8)));
        JLabel cap = new JLabel("RESULT");
        cap.setFont(Theme.GROUP_TITLE);
        cap.setForeground(Theme.HEADING);
        resultLabel.setFont(Theme.STATUS);
        resultLabel.setForeground(Theme.MUTED);
        bar.add(cap, BorderLayout.WEST);
        bar.add(resultLabel, BorderLayout.EAST);

        right.add(bar, BorderLayout.NORTH);
        right.add(report, BorderLayout.CENTER);
        return right;
    }

    // One line of the settings strip, caption then field.
    final JLabel addSetting(String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.LABEL);
        label.setForeground(Theme.INK);
        strip.add(label);
        strip.add(Box.createHorizontalStrut(6));
        strip.add(field);
        space();
        return label;
    }

    final void addSettingRow(JComponent extra) {
        strip.add(extra);
        space();
    }

    // A full width group frame. The editors go in here.
    // A group is a titled frame. Its content fills the frame, which lets the
    // table inside stretch to the height of the column.
    final JPanel addGroup(String title) {
        Group box = new Group(title);
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, 0, 0));
        box.add(content, BorderLayout.CENTER);
        groupRow.add(box);
        return content;
    }

    // The strip is a single row, so each setting is spaced by hand.
    private void space() {
        strip.add(Box.createHorizontalStrut(14));
    }

    // A caption above an editor, used to put two editors side by side.
    static JPanel cell(String caption, java.awt.Component body) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel label = new JLabel(caption);
        label.setFont(Theme.LABEL);
        label.setForeground(Theme.INK);
        p.add(label, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    // Greys out a whole row, including the caption and the field text, so an
    // option the chosen algorithm ignores is clearly not in play.
    static void setRowEnabled(Component row, boolean on) {
        if (row instanceof JLabel) {
            ((JLabel) row).setForeground(on ? Theme.INK : Theme.DISABLED);
        }
        if (row instanceof javax.swing.text.JTextComponent) {
            javax.swing.text.JTextComponent t = (javax.swing.text.JTextComponent) row;
            t.setBackground(on ? Color.WHITE : Theme.WINDOW);
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

    void setStatusSink(StatusSink sink) {
        this.sink = sink;
    }

    private void post(String message, int tone) {
        if (sink != null) {
            sink.update(message, tone);
        }
    }

    // Builds the module input, runs the same code the console mode runs, and
    // puts the text it returns in the result pane. A bad field stops the run
    // and a dialog says which one.
    private void runModule(Runner.Module module) {
        long started = System.nanoTime();
        try {
            String script = buildScript();
            String text = Runner.run(module, script);
            report.show(text);
            long ms = Math.max(0, (System.nanoTime() - started) / 1_000_000L);
            int inputLines = script.split("\n", -1).length;
            int reportLines = text.split("\n", -1).length;
            resultLabel.setText(reportLines + " lines of output  |  " + ms + " ms  |  "
                    + inputLines + " input lines");
            resultLabel.setForeground(Theme.MUTED);
            post("Last run: finished in " + ms + " ms", 1);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    report.getVerticalScrollBar().setValue(0);
                }
            });
        } catch (Ui.InputError e) {
            resultLabel.setText("not run: check the input");
            resultLabel.setForeground(Theme.RED);
            post("Input problem, nothing was run", 2);
            Ui.showError(this, e.getMessage());
        }
    }

    public abstract String buildScript();

    public abstract void loadExample();
}
