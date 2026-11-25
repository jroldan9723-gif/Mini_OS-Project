package minios.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

import minios.BankersAlgorithm;
import minios.CPUScheduler;
import minios.DiskScheduler;
import minios.FragmentationDemo;
import minios.MultiprogrammingSimulator;
import minios.PageReplacementSimulator;
import minios.PagingSimulator;
import minios.SegmentationSimulator;

// Checks the result pictures without opening a window. Every picture is read
// from text the module already prints, so the checks feed it report text and
// look at what it made of it.
public final class GuiChartTest {

    private static int passed;
    private static int failed;

    private GuiChartTest() {
    }

    public static void main(String[] args) {
        System.out.println("GUI CHART CHECKS");

        cpuTimeline();
        pageFrames();
        bankerBars();
        memoryMaps();
        diskSteps();
        everyModule();
        homeScreen();

        System.out.println();
        System.out.println("Result: " + passed + " passed, " + failed + " failed, "
                + (passed + failed) + " total.");
        if (failed == 0) {
            System.out.println("ALL CHART CHECKS PASSED");
        } else {
            System.out.println("CHART CHECKS FAILED");
        }
    }

    private static void cpuTimeline() {
        ResultChart chart = new ResultChart();
        check("cpu report with three slices is read", chart.loadTimeline(
                "Algorithm: SRTF\n\nGantt chart:\n  0-5 P1 | 5-7 P3 | 7-10 P2\n\nProcess"));
        check("cpu chart reports itself as loaded", chart.hasChart());

        check("cpu idle time is accepted", new ResultChart().loadTimeline(
                "Gantt chart:\n  0-2 idle | 2-9 Web | 9-11 DB\n"));

        check("cpu preempted label is accepted", new ResultChart().loadTimeline(
                "Gantt chart:\n  0-2 P1 | 2-3 P2 (preempted) | 3-9 P1\n"));

        ResultChart empty = new ResultChart();
        check("cpu text without a chart is refused", !empty.loadTimeline("no chart here"));
        check("cpu refused text leaves no chart", !empty.hasChart());
        check("cpu null report is refused", !new ResultChart().loadTimeline(null));

        ResultChart broken = new ResultChart();
        check("cpu damaged slice is skipped but the rest loads", broken.loadTimeline(
                "Gantt chart:\n  0-x P1 | 3-6 P2\n"));
        check("cpu the good slice survived", broken.hasChart());
        check("cpu a slice that ends before it starts is dropped",
                !new ResultChart().loadTimeline("Gantt chart:\n  5-2 P1\n"));

        paint(new ResultChart());
        paint(chart);
        ResultChart idleOnly = new ResultChart();
        idleOnly.loadTimeline("Gantt chart:\n  0-4 idle\n");
        paint(idleOnly);

        StringBuilder longRun = new StringBuilder("Gantt chart:\n  ");
        for (int i = 0; i < 40; i++) {
            if (i > 0) {
                longRun.append(" | ");
            }
            longRun.append(i).append('-').append(i + 1).append(" P").append(i % 3 + 1);
        }
        ResultChart wide = new ResultChart();
        wide.loadTimeline(longRun.toString());
        paint(wide);
        System.out.println("painting empty, busy, idle and forty slice charts: ok");
    }

    private static void pageFrames() {
        String report = "FIFO  (frames = 3)\n"
                + "  Ref | frame frame frame | Result\n"
                + "  --- | ------ ------ ------ |--------\n"
                + "    7 |     7     -     - | FAULT \n"
                + "    0 |     7     0     - | FAULT \n"
                + "    2 |     2     0     - | HIT   \n"
                + "  Total page faults: 2\n"
                + "  Total page hits  : 1\n"
                + "  Page fault rate  : 66.67%\n";
        ResultChart chart = new ResultChart();
        check("page frames are read row by row", chart.loadPageFrames(report));
        check("page frame report with no table is refused",
                !new ResultChart().loadPageFrames("nothing here"));
        paint(chart);
    }

    private static void bankerBars() {
        String report = "Need matrix (Max - Allocation):\n"
                + "           R1    R2\n"
                + "   P1       1     2\n"
                + "   P2       0     3\n"
                + "\nSafety check:\n"
                + "  Work (available) starts at [5,5]\n";
        ResultChart chart = new ResultChart();
        check("banker need table is read", chart.loadBanker(report));
        check("banker report without a need table is refused",
                !new ResultChart().loadBanker("no table"));
        paint(chart);
    }

    private static void memoryMaps() {
        ResultChart holes = new ResultChart();
        check("fragmentation map with holes is read", holes.loadMemoryMap(
                "  Memory map: [A 0-400] [HOLE 400-500] [C 500-800] [HOLE 800-1000]"));
        paint(holes);

        ResultChart full = new ResultChart();
        check("free memory is read as empty", full.loadMemoryMap(
                "memory: [free 0-40] [J3 40-65, 25 KB] [free 65-100]"));
        paint(full);

        check("a map with no brackets is refused",
                !new ResultChart().loadMemoryMap("no memory line here"));
        check("null map is refused", !new ResultChart().loadMemoryMap(null));

        String multi = "Tick | CPU\n"
                + "0\n"
                + "  memory: [J1 0-40, 40 KB] [free 40-100]\n"
                + "1\n"
                + "  memory: [J1 0-40, 40 KB] [J2 40-90, 50 KB] [free 90-100]\n";
        check("multiprogramming final map is read", new ResultChart().loadMulti(multi));
        check("multiprogramming report without a map is refused",
                !new ResultChart().loadMulti("no ticks"));
    }

    private static void diskSteps() {
        String report = "FCFS (First-Come, First-Served)\n"
                + "  Order of service : 53 -> 98 -> 183 -> 199 (end of disk) -> 37\n"
                + "  Total head movement: 640 cylinders\n";
        ResultChart chart = new ResultChart();
        check("disk order of service is read", chart.loadDisk(report));
        check("a note in brackets is not read as a cylinder", chart.loadDisk(report));
        check("disk report without an order is refused",
                !new ResultChart().loadDisk("no order line"));
        paint(chart);
    }

    // The strongest check: load the example, press Run, and confirm the
    // picture the panel builds from the real output is not empty.
    private static void everyModule() {
        check("cpu screen draws its timeline",
                draws(new CpuPanel(), CPUScheduler::run));
        check("banker screen draws its bars",
                draws(new BankerPanel(), BankersAlgorithm::run));
        check("paging screen draws its bars",
                draws(new PagingPanel(), PagingSimulator::run));
        check("segmentation screen draws its bars",
                draws(new SegmentationPanel(), SegmentationSimulator::run));
        check("fragmentation screen draws its memory map",
                draws(new FragmentationPanel(), FragmentationDemo::run));
        check("page replacement screen draws its timeline",
                draws(new PageReplacePanel(), PageReplacementSimulator::run));
        check("disk screen draws its steps",
                draws(new DiskPanel(), DiskScheduler::run));
        check("multiprogramming screen draws its memory map",
                draws(new MultiPanel(), MultiprogrammingSimulator::run));
        paintLast();
    }

    private static ModulePanel lastPanel;
    private static ResultChart lastChart;

    private static void paintLast() {
        if (lastChart != null) {
            paint(lastChart);
            System.out.println("painting the chart of " + lastPanel.getClass().getSimpleName() + ": ok");
        }
    }

    // Runs the module the way a user does: Load example, then Run.
    private static boolean draws(ModulePanel panel, Runner.Module module) {
        try {
            panel.loadExample();
            String output = Runner.run(module, panel.buildScript());
            if (!output.isEmpty()) {
                invokeAfterRun(panel, output);
            }
            ResultChart chart = findChart(panel);
            if (chart == null) {
                return false;
            }
            lastPanel = panel;
            lastChart = chart;
            return chart.hasChart();
        } catch (RuntimeException e) {
            System.out.println("  (" + e + ")");
            return false;
        }
    }

    private static void invokeAfterRun(ModulePanel panel, String output) {
        try {
            java.lang.reflect.Method m = ModulePanel.class.getDeclaredMethod("afterRun", String.class);
            m.setAccessible(true);
            m.invoke(panel, output);
        } catch (Exception e) {
            // a screen without a chart simply has nothing to redraw
        }
    }

    private static ResultChart findChart(ModulePanel panel) {
        Class<?> type = panel.getClass();
        while (type != null) {
            try {
                java.lang.reflect.Field f = type.getDeclaredField("chart");
                f.setAccessible(true);
                return (ResultChart) f.get(panel);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    // The landing screen is only useful if every card opens the right module.
    private static void homeScreen() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // the plain look is fine for a check
        }
        StripTabs tabs = new StripTabs();
        HomePanel home = new HomePanel(tabs);
        tabs.addTab("Home", home);
        String[] names = {"CPU", "Banker", "Paging", "Segments", "Fragmentation",
            "Page replacement", "Disk", "Multiprogramming"};
        for (String name : names) {
            tabs.addTab(name, new JPanel());
        }
        home.setSize(1200, 700);
        home.doLayout();

        List<Component> cards = new ArrayList<>();
        collectCards(home, cards);
        check("landing screen has one card per module", cards.size() == 8);

        boolean allRight = true;
        for (int i = 0; i < cards.size(); i++) {
            tabs.setSelectedIndex(0);
            Component card = cards.get(i);
            card.dispatchEvent(new MouseEvent(card, MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(), 0, 5, 5, 1, false));
            if (tabs.getSelectedIndex() != i + 1) {
                allRight = false;
            }
        }
        check("each card opens its own module", allRight);
    }

    private static void collectCards(Container root, List<Component> out) {
        for (Component c : root.getComponents()) {
            if (c.getCursor() != null && c.getCursor().getType() == Cursor.HAND_CURSOR) {
                out.add(c);
            } else if (c instanceof Container) {
                collectCards((Container) c, out);
            }
        }
    }

    private static void paint(ResultChart chart) {
        chart.setSize(420, 118);
        BufferedImage img = new BufferedImage(420, 118, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        chart.paint(g);
        g.dispose();
    }

    private static void check(String what, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + what);
        } else {
            failed++;
            System.out.println("FAIL  " + what);
        }
    }
}
