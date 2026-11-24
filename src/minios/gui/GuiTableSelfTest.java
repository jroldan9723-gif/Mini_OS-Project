package minios.gui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;

// Checks the add / edit / remove buttons on a module table. It types into the
// entry boxes and clicks the buttons the same way a user would, then looks at
// what the table holds. No window is opened.
public final class GuiTableSelfTest {
    static int fails = 0;

    static void check(String what, Object got, Object want) {
        boolean ok = String.valueOf(got).equals(String.valueOf(want));
        if (!ok) fails++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + what + (ok ? "" : "  got=" + got + " want=" + want));
    }

    static RecordEditor editor(Container c) {
        if (c instanceof RecordEditor) return (RecordEditor) c;
        if (c instanceof Container)
            for (Component k : ((Container) c).getComponents()) {
                RecordEditor r = editor((Container) k);
                if (r != null) return r;
            }
        return null;
    }

    static JButton button(Container c, String text) {
        for (Component k : c.getComponents()) {
            if (k instanceof JButton && text.equals(((JButton) k).getText())) return (JButton) k;
            if (k instanceof Container) {
                JButton b = button((Container) k, text);
                if (b != null) return b;
            }
        }
        return null;
    }

    static List<JTextField> boxes(Container c) {
        List<JTextField> out = new ArrayList<>();
        collect(c, out);
        return out;
    }

    static void collect(Container c, List<JTextField> out) {
        for (Component k : c.getComponents()) {
            if (k instanceof JTextField) out.add((JTextField) k);
            if (k instanceof Container) collect((Container) k, out);
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        CpuPanel panel = new CpuPanel();
        RecordEditor ed = editor(panel);
        JButton add = button(ed, "Add row");
        JButton save = add;
        List<JTextField> b = boxes(ed);
        check("four entry boxes", b.size(), 4);

        // type Java, arrival 0, burst 5 -> Add row
        b.get(0).setText("Java");
        b.get(1).setText("0");
        b.get(2).setText("5");
        add.doClick();
        check("one row after first add", ed.rowCount(), 1);
        check("boxes cleared after add", b.get(0).getText() + "|" + b.get(2).getText(), "|");

        // second process
        b.get(0).setText("Python");
        b.get(1).setText("1");
        b.get(2).setText("3");
        add.doClick();
        check("two rows", ed.rowCount(), 2);
        check("row 2 name", ed.rows().get(1)[0], "Python");

        // click row 1 to edit it: the boxes should fill with its values
        JTable table = null;
        for (Component k : ed.getComponents())
            if (k instanceof JScrollPane) table = (JTable) ((JScrollPane) k).getViewport().getView();
        table.setRowSelectionInterval(0, 0);
        check("boxes filled for editing", b.get(0).getText(), "Java");
        check("add button renamed", save.getText(), "Save changes");

        // change the burst and save
        b.get(2).setText("9");
        add.doClick();
        check("edited in place, still two rows", ed.rowCount(), 2);
        check("burst updated", ed.rows().get(0)[2], "9");
        check("name unchanged", ed.rows().get(0)[0], "Java");
        check("button back to Add row", save.getText(), "Add row");

        // duplicate name is refused
        b.get(0).setText("java");
        b.get(1).setText("2");
        b.get(2).setText("1");
        add.doClick();
        check("duplicate name refused", ed.rowCount(), 2);

        // remove the selected row
        table.setRowSelectionInterval(1, 1);
        button(ed, "Remove").doClick();
        check("one row left", ed.rowCount(), 1);
        check("right row removed", ed.rows().get(0)[0], "Java");

        // clear all
        button(ed, "Clear all").doClick();
        check("cleared", ed.rowCount(), 0);

        System.out.println(fails == 0 ? "ALL EDIT CHECKS PASSED" : fails + " EDIT CHECK(S) FAILED");
        System.exit(fails == 0 ? 0 : 1);
    }
}
