package minios.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

    private Runnable changed = null;
    private int editingRow = -1;

    RecordEditor(Column... columns) {
        this(null, 1, columns);
    }

    // numberPrefix labels each row on the left (P1, P2, ... or Page 0, ...).
    RecordEditor(String numberPrefix, int numberStart, Column... columns) {
        super(new BorderLayout(0, 4));
        this.columns = columns;
        this.numberPrefix = numberPrefix;
        this.numberStart = numberStart;
        this.boxes = new JTextField[columns.length];

        setOpaque(false);

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.setFont(Theme.LABEL);
        table.setForeground(Theme.INK);
        table.setBackground(Theme.SURFACE);
        table.setGridColor(Theme.HAIRLINE);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setTableHeader(null);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        for (int i = 0; i < columns.length; i++) {
            int width = columns[i].width;
            table.getColumnModel().getColumn(i + (numberPrefix == null ? 0 : 1))
                    .setPreferredWidth(width);
        }
        if (numberPrefix != null) {
            table.getColumnModel().getColumn(0).setPreferredWidth(52);
            table.getColumnModel().getColumn(0).setMaxWidth(64);
        }
        table.getSelectionModel().addListSelectionListener(e -> rowPicked());

        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        add(pane, BorderLayout.CENTER);
        add(buildEntryRow(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildEntryRow() {
        JPanel outer = new JPanel(new BorderLayout(0, 4));
        outer.setOpaque(false);

        JPanel boxesRow = new JPanel(new java.awt.GridBagLayout());
        boxesRow.setOpaque(false);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new java.awt.Insets(2, 0, 2, 6);
        gc.anchor = java.awt.GridBagConstraints.WEST;

        for (int i = 0; i < columns.length; i++) {
            JPanel cell = new JPanel(new BorderLayout(0, 2));
            cell.setOpaque(false);
            JLabel caption = new JLabel(columns[i].title);
            caption.setFont(Theme.LABEL_BOLD);
            caption.setForeground(Theme.HEADING);
            int wide = Math.max(columns[i].width, 64);
            JTextField box = Ui.field(6, wide);
            final int index = i;
            box.addActionListener(e -> addOrSave());
            boxes[i] = box;
            cell.add(caption, BorderLayout.NORTH);
            cell.add(box, BorderLayout.CENTER);
            gc.gridx = i;
            gc.weightx = columns[i].kind == Column.VECTOR ? 0.2 : 0.0;
            boxesRow.add(cell, gc);
        }

        JPanel addCell = new JPanel(new BorderLayout(0, 2));
        addCell.setOpaque(false);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(Theme.LABEL_BOLD);
        addCell.add(spacer, BorderLayout.NORTH);
        addCell.add(addButton, BorderLayout.CENTER);
        gc.gridx = columns.length;
        gc.weightx = 0.0;
        boxesRow.add(addCell, gc);

        // keeps the row of boxes on the left instead of spreading them out
        JPanel gapFiller = new JPanel();
        gapFiller.setOpaque(false);
        gc.gridx = columns.length + 1;
        gc.weightx = 1.0;
        boxesRow.add(gapFiller, gc);

        message.setFont(Theme.HINT);
        message.setForeground(Theme.RED);

        outer.add(boxesRow, BorderLayout.NORTH);
        message.setBorder(new EmptyBorder(0, 2, 0, 0));
        message.setVisible(false);
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
        boxes[index].setEnabled(on);
        boxes[index].setBackground(on ? java.awt.Color.WHITE : Theme.WINDOW);
        boxes[index].setForeground(on ? Theme.INK : Theme.DISABLED);
        boxes[index].setText("");
        boxes[index].setFocusable(on);
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
            return column == 0 ? numberPrefix : columns[column - 1].title;
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
