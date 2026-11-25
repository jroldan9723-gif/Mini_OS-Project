package minios.gui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;

// Throws bad input at every panel and checks that only Ui.InputError comes
// back. Any other exception would escape the Run button and the window would
// look like it had frozen.
public final class GuiInputEdgeTest {
    static int fails = 0;

    static void expectInputError(String what, ModulePanel p, Runnable setup) {
        setup.run();
        try {
            p.buildScript();
            System.out.println("FAIL  " + what + " -> no error at all");
            fails++;
        } catch (Ui.InputError e) {
            System.out.println("PASS  " + what + " -> " + trim(e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("FAIL  " + what + " -> LEAKED " + e.getClass().getSimpleName()
                    + ": " + trim(e.getMessage()));
            fails++;
        }
    }

    static void expectOk(String what, ModulePanel p, Runnable setup) {
        setup.run();
        try {
            p.buildScript();
            System.out.println("PASS  " + what + " -> accepted");
        } catch (RuntimeException e) {
            System.out.println("FAIL  " + what + " -> " + e.getClass().getSimpleName()
                    + ": " + trim(e.getMessage()));
            fails++;
        }
    }

    static String trim(String m) {
        if (m == null) return "null message";
        return m.length() > 62 ? m.substring(0, 62) + "..." : m;
    }

    static void clearExamples(ModulePanel[] panels) {
        for (ModulePanel p : panels) p.loadExample();
    }

    public static void main(String[] a) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        // --- empty tables on every tab ---
        ModulePanel[] all = { new CpuPanel(), new BankerPanel(), new PagingPanel(),
            new SegmentationPanel(), new FragmentationPanel(), new PageReplacePanel(),
            new DiskPanel(), new MultiPanel() };
        String[] names = {"CPU", "Banker", "Paging", "Segmentation", "Fragment",
            "Replace", "Disk", "Multi"};
        for (int i = 0; i < all.length; i++) {
            final ModulePanel p = all[i];
            expectInputError(names[i] + " with no rows", p, () -> clearTables(p));
        }

        // --- blank settings ---
        CpuPanel cpu = new CpuPanel();
        cpu.loadExample();
        expectInputError("CPU blank quantum (round robin)", cpu, () -> {
            ((JComboBox<?>) find(cpu, JComboBox.class)).setSelectedIndex(2);
            setText(cpu, 0, "");
        });

        // --- junk in a field that only a strip holds ---
        PagingPanel paging = new PagingPanel();
        paging.loadExample();
        expectInputError("Paging junk page size", paging,
                () -> setText(paging, 0, "abc"));
        expectInputError("Paging negative page size", paging,
                () -> setText(paging, 0, "-5"));
        expectInputError("Paging huge page size", paging,
                () -> setText(paging, 0, "99999999999"));

        // --- banker vectors that do not line up ---
        BankerPanel banker = new BankerPanel();
        banker.loadExample();
        expectInputError("Banker empty available", banker,
                () -> banker.available.setText(""));

        // --- disk request past the platter ---
        DiskPanel disk = new DiskPanel();
        disk.loadExample();
        expectInputError("Disk head outside range", disk,
                () -> setText(disk, 1, "9999"));

        // --- a job bigger than memory ---
        MultiPanel multi = new MultiPanel();
        multi.loadExample();
        expectInputError("Multi memory too small", multi,
                () -> setText(multi, 0, "10"));

        // --- good input still works ---
        clearExamples(all);
        for (int i = 0; i < all.length; i++) {
            expectOk(names[i] + " example", all[i], () -> { });
        }

        System.out.println(fails == 0 ? "ALL EDGE CHECKS PASSED" : fails + " EDGE CHECK(S) FAILED");
        System.exit(fails == 0 ? 0 : 1);
    }

    // Empties every table the panel owns, leaving the settings strip alone.
    static void clearTables(Container c) {
        for (Component k : c.getComponents()) {
            if (k instanceof RecordEditor) {
                ((RecordEditor) k).clearRows();
            } else if (k instanceof Container) {
                clearTables((Container) k);
            }
        }
    }

    static <T> T find(Container c, Class<T> type) {
        for (Component k : c.getComponents()) {
            if (type.isInstance(k)) return type.cast(k);
            if (k instanceof Container) {
                T got = find((Container) k, type);
                if (got != null) return got;
            }
        }
        return null;
    }

    static void setText(Container c, int nth, String value) {
        List<JTextField> fields = allText(c);
        if (nth < fields.size()) {
            fields.get(nth).setText(value);
        } else {
            System.out.println("  (no field " + nth + ", only " + fields.size() + ")");
        }
    }

    static List<JTextField> allText(Container c) {
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
}
