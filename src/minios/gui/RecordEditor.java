package minios.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

// A table with a row of entry boxes underneath. Type the values, press Add
// row, and the row appears in the table while the boxes clear themselves for
// the next one. Clicking a row loads it back into the boxes so a mistake can
// be corrected or the row removed before anything runs.
final class RecordEditor extends JPanel {

    private static final long serialVersionUID = 1L;

    // How one column is checked and how wide it should be.
    static final class Column {
        private static final int TEXT = 0;
        private static final int NUMBER = 1;
        private static final int VECTOR = 2;

        final String title;
        final int width;
        final int kind;
        final boolean optional;
        final boolean unique;
        final int min;
        final int max;

        private Column(String title, int width, int kind, boolean optional,
                boolean unique, int min, int max) {
            this.title = title;
            this.width = width;
            this.kind = kind;
            this.optional = optional;
            this.unique = unique;
            this.min = min;
            this.max = max;
        }

        static Column number(String title, int width, int min, int max) {
            return new Column(title, width, NUMBER, false, false, min, max);
        }

        static Column optionalNumber(String title, int width, int min, int max) {
            return new Column(title, width, NUMBER, true, false, min, max);
        }

        static Column name(String title, int width) {
            return new Column(title, width, TEXT, false, true, 0, 0);
        }

        static Column optionalName(String title, int width) {
            return new Column(title, width, TEXT, true, true, 0, 0);
        }

        static Column vector(String title, int width) {
            return new Column(title, width, VECTOR, false, false, 0, 0);
        }
    }

    private final Column[] columns;
    private final String numberPrefix;
    private final int numberStart;

    private final List<String[]> data = new ArrayList<>();
    private final RowModel model = new RowModel();
    private final JTable table;
    private final JTextField[] boxes;
    private final JButton addButton = Ui.button("Add row");
    private final JButton cancelButton = Ui.button("Cancel");
    private final JButton removeButton = Ui.button("Remove");
    private final JButton clearButton = Ui.button("Clear all");
    private final JLabel message = new JLabel(" ");
    private final JLabel count = new JLabel("no rows yet");

    private JPanel boxesRow;
    private final int[] boxWidth = new int[8];
    private final int numberWidth;
    private final boolean[] columnEnabled = new boolean[8];
    private JScrollPane pane;
    private boolean syncing = false;

    private Runnable changed = null;
    private int editingRow = -1;

    RecordEditor(Column... columns) {
        this(null, 1, columns);
    }

    // numberPrefix labels each row on the left (P1, P2, ... or Page 0, ...).
    RecordEditor(String numberPrefix, int numberStart, Column... columns) {
        super(new BorderLayout(0, 4));
        for (int i = 0; i < columnEnabled.length; i++) {
            columnEnabled[i] = true;
        }
        this.columns = columns;
        this.numberPrefix = numberPrefix;
        this.numberStart = numberStart;
        this.numberWidth = numberPrefix == null ? 0 : Math.max(56, numberPrefix.length() * 9 + 26);
        this.boxes = new JTextField[columns.length];

        setOpaque(false);

        table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (data.isEmpty()) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(Theme.HINT);
                    g2.setColor(Theme.MUTED);
                    g2.drawString("No rows yet.", 14, 26);
                    g2.dispose();
                }
            }

            @Override
            public java.awt.Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int col) {
                java.awt.Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(Theme.SELECT);
                    c.setForeground(Theme.INK);
                } else {
                    c.setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.ZEBRA);
                    c.setForeground(Theme.INK);
                }
                return c;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(23);
        table.setFont(Theme.TABLE);
        table.setForeground(Theme.INK);
        table.setBackground(Theme.SURFACE);
        table.setGridColor(Theme.HAIRLINE);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // A header makes it obvious which column is which once the entry boxes
        // above have been cleared for the next row.
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(Theme.TABLE_HEAD);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setPreferredSize(new java.awt.Dimension(10, 24));
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, value,
                        false, false, row, col);
                c.setBackground(Theme.HEADER_BG);
                c.setForeground(Theme.HEADING);
                c.setFont(Theme.TABLE_HEAD);
                setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                        javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 6)));
                return c;
            }
        });
        for (int i = 0; i < columns.length; i++) {
            int width = columns[i].width;
            table.getColumnModel().getColumn(i + (numberPrefix == null ? 0 : 1))
                    .setPreferredWidth(width);
        }
        if (numberPrefix != null) {
            table.getColumnModel().getColumn(0).setPreferredWidth(numberWidth);
            table.getColumnModel().getColumn(0).setMinWidth(numberWidth);
        }
        table.getSelectionModel().addListSelectionListener(e -> rowPicked());

        pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        pane.getViewport().setBackground(Theme.SURFACE);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setColumnHeaderView(table.getTableHeader());
        pane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                syncWidths();
            }
        });

        add(pane, BorderLayout.CENTER);
        add(buildEntryRow(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildEntryRow() {
        JPanel outer = new JPanel(new BorderLayout(0, 3));
        outer.setOpaque(false);

        JLabel cue = new JLabel("New row");
        cue.setFont(Theme.TABLE_HEAD);
        cue.setForeground(Theme.HEADING);

        // Laid out by hand so every box sits exactly above its own column.
        boxesRow = new JPanel(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public void doLayout() {
                // skip the row-number column, which has no box of its own
                int x = numberWidth;
                for (int i = 0; i < boxes.length && boxes[i] != null; i++) {
                    int w = boxWidth[i];
                    boxes[i].setBounds(x, 0, w, 26);
                    x += w;
                }
            }
        };
        boxesRow.setOpaque(false);
        boxesRow.setPreferredSize(new java.awt.Dimension(10, 26));

        for (int i = 0; i < columns.length; i++) {
            boxWidth[i] = Math.max(columns[i].width, 56);
            JTextField box = Ui.field(6, boxWidth[i]);
            box.addActionListener(e -> addOrSave());
            if (!columnEnabled[i]) {
                box.setEnabled(false);
                box.setBackground(Theme.WINDOW);
                box.setForeground(Theme.DISABLED);
                box.setFocusable(false);
            }
            boxes[i] = box;
            boxesRow.add(box);
        }
        message.setFont(Theme.HINT);
        message.setForeground(Theme.RED);
        message.setBorder(new EmptyBorder(0, 2, 0, 0));
        message.setVisible(false);

        JPanel top = new JPanel(new BorderLayout(0, 2));
        top.setOpaque(false);
        top.add(cue, BorderLayout.NORTH);
        top.add(boxesRow, BorderLayout.CENTER);
        outer.add(top, BorderLayout.NORTH);
        outer.add(message, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildButtons() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        addButton.addActionListener(e -> addOrSave());
        cancelButton.addActionListener(e -> stopEditing());
        removeButton.addActionListener(e -> removeSelected());
        clearButton.addActionListener(e -> clearAll());

        count.setFont(Theme.HINT);
        count.setForeground(Theme.MUTED);

        row.add(addButton);
        row.add(cancelButton);
        row.add(removeButton);
        row.add(clearButton);
        row.add(Box.createHorizontalStrut(4));
        row.add(count);
        cancelButton.setVisible(false);
        removeButton.setEnabled(false);
        clearButton.setEnabled(false);
        return row;
    }

    // Reads one value for one column. Returns an error message, or null if the
    // value is usable.
    private String check(int index, String raw) {
        Column c = columns[index];
        String v = raw == null ? "" : raw.trim();
        if (v.isEmpty()) {
            return c.optional ? null : c.title + " cannot be empty.";
        }
        if (c.kind == Column.TEXT) {
            if (v.length() > 12) {
                return c.title + " is too long, keep it to 12 characters.";
            }
            return null;
        }
        if (c.kind == Column.VECTOR) {
            String[] parts = v.split("\\s+");
            for (String part : parts) {
                try {
                    if (Integer.parseInt(part) < 0) {
                        return c.title + ": " + part + " cannot be negative.";
                    }
                } catch (NumberFormatException e) {
                    return c.title + ": \"" + part + "\" is not a whole number.";
                }
            }
            return null;
        }
        try {
            int value = Integer.parseInt(v);
            if (value < c.min || value > c.max) {
                return c.title + " must be between " + c.min + " and " + c.max + ".";
            }
        } catch (NumberFormatException e) {
            return c.title + ": \"" + v + "\" is not a whole number.";
        }
        return null;
    }

    // Checks the whole entry row, then adds it or saves over the picked row.
    private void addOrSave() {
        String[] row = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            row[i] = boxes[i].getText().trim();
            String problem = check(i, row[i]);
            if (problem != null) {
                message.setText(problem);
                message.setVisible(true);
                boxes[i].requestFocusInWindow();
                return;
            }
        }

        // two rows with the same name would make the report ambiguous
        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].unique || row[i].isEmpty()) {
                continue;
            }
            for (int r = 0; r < data.size(); r++) {
                if (r != editingRow && data.get(r)[i].equalsIgnoreCase(row[i])) {
                    message.setText("There is already a row with " + row[i] + " in "
                            + columns[i].title + ".");
                    boxes[i].requestFocusInWindow();
                    return;
                }
            }
        }

        int savedRow = editingRow;
        if (editingRow >= 0) {
            data.set(editingRow, row);
        } else {
            data.add(row);
        }
        editingRow = -1;
        table.clearSelection();
        for (JTextField box : boxes) {
            box.setText("");
        }
        boxes[0].requestFocusInWindow();
        message.setText(" ");
        message.setVisible(false);
        refresh();
        if (savedRow >= 0) {
            table.setRowSelectionInterval(savedRow, savedRow);
            table.clearSelection();
        }
        fire();
    }

    // Puts the picked row back in the boxes and turns Add into Save changes.
    private void rowPicked() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= data.size()) {
            stopEditing();
            return;
        }
        editingRow = row;
        for (int i = 0; i < columns.length; i++) {
            boxes[i].setText(data.get(row)[i]);
        }
        boxes[0].requestFocusInWindow();
        addButton.setText("Save changes");
        cancelButton.setVisible(true);
        removeButton.setEnabled(true);
        message.setText("Editing row " + label(row) + ". Press Save changes when done.");
        message.setForeground(Theme.MUTED);
        message.setVisible(true);
    }

    private void stopEditing() {
        editingRow = -1;
        for (JTextField box : boxes) {
            box.setText("");
        }
        table.clearSelection();
        addButton.setText("Add row");
        cancelButton.setVisible(false);
        removeButton.setEnabled(false);
        message.setText(" ");
        message.setForeground(Theme.RED);
        message.setVisible(false);
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= data.size()) {
            return;
        }
        data.remove(row);
        stopEditing();
        refresh();
        fire();
    }

    private void clearAll() {
        data.clear();
        stopEditing();
        refresh();
        fire();
    }

    private String label(int row) {
        if (numberPrefix == null) {
            return String.valueOf(row + 1);
        }
        return numberPrefix + (numberStart + row);
    }

    // Both the columns and the entry boxes take their widths from here, so the
    // row of boxes always sits directly over the columns it belongs to.
    private void syncWidths() {
        if (syncing || boxesRow == null || table.getColumnCount() == 0) {
            return;
        }
        // the viewport holds the table, so the scroll pane is what to measure
        int width = pane == null ? 0 : pane.getViewport().getWidth()
                - (pane.getVerticalScrollBar().isVisible()
                        ? pane.getVerticalScrollBar().getWidth() : 0);
        if (width <= 40) {
            return;
        }
        syncing = true;

        int first = numberPrefix == null ? 0 : 1;
        int available = width - numberWidth;

        // Share the room in proportion to what each column asked for, so a
        // narrow editor squeezes the columns instead of growing a sideways
        // scrollbar, and a wide one still gives the boxes room to type in.
        int totalWanted = 0;
        int[] wanted = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            wanted[i] = Math.max(columns[i].width, 56);
            totalWanted += wanted[i];
        }
        double scale = totalWanted == 0 ? 1.0 : (double) available / totalWanted;
        if (columns.length == 1) {
            // one column: let it take the room, but not into silly territory
            scale = Math.min(scale, 1.6);
        }

        int used = 0;
        for (int i = 0; i < columns.length; i++) {
            int w = Math.max(44, (int) Math.floor(wanted[i] * scale));
            boxWidth[i] = w;
            table.getColumnModel().getColumn(i + first).setPreferredWidth(w);
            table.getColumnModel().getColumn(i + first).setWidth(w);
            used += w;
        }
        if (numberPrefix != null) {
            table.getColumnModel().getColumn(0).setPreferredWidth(numberWidth);
            table.getColumnModel().getColumn(0).setWidth(numberWidth);
        }

        // Anything left over (a single narrow column, for instance) widens the
        // last table column only, so the table reaches the frame while the
        // entry box stays the size of a sensible value.
        int slack = available - used;
        if (slack > 0 && columns.length > 0) {
            int last = table.getColumnCount() - 1;
            int extra = table.getColumnModel().getColumn(last).getWidth() + slack;
            table.getColumnModel().getColumn(last).setPreferredWidth(extra);
            table.getColumnModel().getColumn(last).setWidth(extra);
        }
        boxesRow.setPreferredSize(new java.awt.Dimension(used + numberWidth, 26));
        boxesRow.revalidate();
        boxesRow.repaint();
        syncing = false;
    }

    // Re-run the width calculation whenever the pane changes size.
    @Override
    public void doLayout() {
        super.doLayout();
        syncWidths();
    }

    private void refresh() {
        model.fireTableDataChanged();
        String noun = data.size() == 1 ? "row" : "rows";
        count.setText(data.size() + " " + noun);
        clearButton.setEnabled(!data.isEmpty());
        removeButton.setEnabled(table.getSelectedRow() >= 0);
    }

    private void fire() {
        if (changed != null) {
            changed.run();
        }
    }

    // ---- what the module reads -------------------------------------------

    List<String[]> rows() {
        List<String[]> copy = new ArrayList<>();
        for (String[] row : data) {
            copy.add(row.clone());
        }
        return copy;
    }

    int rowCount() {
        return data.size();
    }

    void setRows(String[][] rows) {
        data.clear();
        for (String[] row : rows) {
            data.add(row.clone());
        }
        stopEditing();
        refresh();
        fire();
    }

    void setRows(List<String[]> rows) {
        data.clear();
        for (String[] row : rows) {
            data.add(row.clone());
        }
        stopEditing();
        refresh();
        fire();
    }

    void clearRows() {
        data.clear();
        stopEditing();
        refresh();
    }

    // Greys out a column's entry box when the chosen option ignores it. The
    // header stays readable so it is clear what the column would hold.
    void setColumnEnabled(int index, boolean on) {
        if (index < 0 || index >= columns.length) {
            return;
        }
        columnEnabled[index] = on;
        JTextField box = boxes[index];
        if (box == null) {
            // called while the module is still being built
            return;
        }
        box.setEnabled(on);
        box.setBackground(on ? java.awt.Color.WHITE : Theme.WINDOW);
        box.setForeground(on ? Theme.INK : Theme.DISABLED);
        box.setText("");
        box.setFocusable(on);
    }

    // Called after every add, save, remove and clear.
    void setChanged(Runnable changed) {
        this.changed = changed;
    }

    // Disables or enables the whole editor, used when an option is not in play.
    static void setEnabledDeep(JComponent root, boolean on) {
        root.setEnabled(on);
        for (java.awt.Component c : root.getComponents()) {
            c.setEnabled(on);
            if (c instanceof JComponent) {
                setEnabledDeep((JComponent) c, on);
            }
        }
    }

    private final class RowModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length + (numberPrefix == null ? 0 : 1);
        }

        @Override
        public String getColumnName(int column) {
            if (numberPrefix == null) {
                return columns[column].title;
            }
            return column == 0 ? "#" : columns[column - 1].title;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (numberPrefix == null) {
                return data.get(row)[column];
            }
            return column == 0 ? label(row) : data.get(row)[column - 1];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

}
