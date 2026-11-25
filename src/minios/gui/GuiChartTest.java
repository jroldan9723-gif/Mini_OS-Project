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

// Checks the Gantt chart without opening a window. The chart reads the text
// the scheduler already prints, so these checks feed it report text and look
// at what it made of it.
public final class GuiChartTest {

    private static int passed;
    private static int failed;

    private GuiChartTest() {
    }

    public static void main(String[] args) {
        System.out.println("GUI CHART CHECKS");

        GanttChart chart = new GanttChart();

        check("report with three slices is read", chart.load(
                "Algorithm: SRTF\n\nGantt chart:\n  0-5 P1 | 5-7 P3 | 7-10 P2\n\nProcess"));
        check("chart reports itself as loaded", chart.hasChart());

        check("idle time is accepted", new GanttChart().load(
                "Gantt chart:\n  0-2 idle | 2-9 Web | 9-11 DB\n"));

        check("preempted label is accepted", new GanttChart().load(
                "Gantt chart:\n  0-2 P1 | 2-3 P2 (preempted) | 3-9 P1\n"));

        GanttChart empty = new GanttChart();
        check("text without a chart is refused", !empty.load("no chart here"));
        check("refused text leaves no chart", !empty.hasChart());

        check("null report is refused", !new GanttChart().load(null));

        GanttChart broken = new GanttChart();
        check("damaged slice is skipped but the rest loads", broken.load(
                "Gantt chart:\n  0-x P1 | 3-6 P2\n"));
        check("the good slice survived", broken.hasChart());

        GanttChart backwards = new GanttChart();
        check("a slice that ends before it starts is dropped", !backwards.load(
                "Gantt chart:\n  5-2 P1\n"));

        // Every state must survive a paint without throwing.
        paint(new GanttChart());
        paint(chart);
        GanttChart idleOnly = new GanttChart();
        idleOnly.load("Gantt chart:\n  0-4 idle\n");
        paint(idleOnly);
        System.out.println("painting the empty, busy and idle charts: ok");

        // A very long schedule must still fit the width.
        StringBuilder longRun = new StringBuilder("Gantt chart:\n  ");
        for (int i = 0; i < 40; i++) {
            if (i > 0) {
                longRun.append(" | ");
            }
            longRun.append(i).append('-').append(i + 1).append(" P").append(i % 3 + 1);
        }
        GanttChart wide = new GanttChart();
        wide.setSize(420, 118);
        wide.load(longRun.toString());
        paint(wide);
        System.out.println("forty slices on one line: ok");

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

    private static void paint(GanttChart chart) {
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
