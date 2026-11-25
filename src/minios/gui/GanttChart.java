package minios.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

// Draws the CPU schedule as a row of coloured bars, one per slice of time.
// The data comes from the plain text line the scheduler already prints, so
// the picture can never disagree with the report beside it.
final class GanttChart extends JComponent {

    private static final long serialVersionUID = 1L;

    // One slice: from start to end, used by one process (or nobody).
    private static final class Slice {
        final int start;
        final int end;
        final String label;
        final boolean idle;

        Slice(int start, int end, String label, boolean idle) {
            this.start = start;
            this.end = end;
            this.label = label;
            this.idle = idle;
        }
    }

    private final List<Slice> slices = new ArrayList<>();
    private String avgTurnaround = "";
    private String avgWaiting = "";
    private String avgResponse = "";
    private final Map<String, Color> colours = new LinkedHashMap<>();

    // Colours reused in order, so the same process keeps one colour all run.
    private static final Color[] PALETTE = {
        new Color(0x3D, 0x6E, 0xA8), new Color(0x4C, 0x8B, 0x6A),
        new Color(0x8C, 0x5A, 0xA0), new Color(0xB5, 0x6B, 0x38),
        new Color(0x2F, 0x7D, 0x8C), new Color(0x9A, 0x4A, 0x5C),
        new Color(0x6B, 0x7A, 0x3A), new Color(0x5A, 0x62, 0xA8)
    };
    private static final Color IDLE = new Color(0xDD, 0xE1, 0xE6);

    GanttChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 96));
        setMinimumSize(new Dimension(200, 96));
    }

    // Reads a line such as "  0-5 P1 | 5-7 P3 | 7-10 P2 (preempted)".
    // Returns false when the text holds no slices, so the panel can stay hidden.
    boolean load(String report) {
        slices.clear();
        colours.clear();
        avgTurnaround = readAfter(report, "Average turnaround time:");
        avgWaiting = readAfter(report, "Average waiting time");
        avgResponse = readAfter(report, "Average response time");
        String chart = findChartLine(report);
        if (chart == null) {
            repaint();
            return false;
        }
        for (String part : chart.split("\\|")) {
            String piece = part.trim();
            if (piece.isEmpty()) {
                continue;
            }
            int dash = piece.indexOf('-');
            if (dash <= 0) {
                continue;
            }
            try {
                int start = Integer.parseInt(piece.substring(0, dash).trim());
                int space = piece.indexOf(' ', dash);
                if (space < 0) {
                    continue;
                }
                int end = Integer.parseInt(piece.substring(dash + 1, space).trim());
                String label = piece.substring(space + 1).trim();
                if (end <= start) {
                    continue;
                }
                boolean idle = label.startsWith("idle");
                slices.add(new Slice(start, end, label, idle));
                if (!idle) {
                    String key = label.replace(" (preempted)", "");
                    if (!colours.containsKey(key)) {
                        colours.put(key, PALETTE[colours.size() % PALETTE.length]);
                    }
                }
            } catch (NumberFormatException e) {
                // a line that does not parse is simply not drawn
            }
        }
        repaint();
        return !slices.isEmpty();
    }

    private static String readAfter(String report, String key) {
        if (report == null) {
            return "";
        }
        for (String line : report.split("\n")) {
            if (line.startsWith(key)) {
                int colon = line.lastIndexOf(':');
                if (colon < 0) {
                    colon = line.lastIndexOf(' ');
                }
                return line.substring(colon + 1).trim();
            }
        }
        return "";
    }

    boolean hasChart() {
        return !slices.isEmpty();
    }

    // The chart sits two lines under the "Gantt chart:" heading.
    private static String findChartLine(String report) {
        if (report == null) {
            return null;
        }
        String[] lines = report.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals("Gantt chart:")) {
                for (int j = i + 1; j < Math.min(i + 4, lines.length); j++) {
                    if (lines[j].contains("-") && lines[j].contains("|")) {
                        return lines[j];
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int left = 8;
        int right = getWidth() - 10;
        int top = 22;
        int barHeight = 34;

        if (slices.isEmpty()) {
            g2.setFont(Theme.HINT);
            g2.setColor(Theme.MUTED);
            g2.drawString("Run a schedule to see the chart.", left, top + barHeight / 2);
            g2.dispose();
            return;
        }

        int first = slices.get(0).start;
        int last = slices.get(slices.size() - 1).end;
        int span = Math.max(1, last - first);
        int usable = Math.max(60, right - left);

        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();

        // bars
        for (Slice s : slices) {
            int x1 = left + Math.round((s.start - first) * (float) usable / span);
            int x2 = left + Math.round((s.end - first) * (float) usable / span);
            int w = Math.max(2, x2 - x1);
            Color fill = s.idle ? IDLE : colours.get(s.label.replace(" (preempted)", ""));
            g2.setColor(fill == null ? PALETTE[0] : fill);
            g2.fillRect(x1, top, w, barHeight);
            g2.setColor(Theme.SURFACE);
            g2.drawRect(x1, top, w, barHeight);

            // the name goes in the bar when it fits
            String shortLabel = s.idle ? "idle" : s.label.replace(" (preempted)", "");
            int textWidth = fm.stringWidth(shortLabel);
            if (textWidth + 6 < w) {
                g2.setFont(Theme.LABEL);
                FontMetrics tf = g2.getFontMetrics();
                g2.setColor(s.idle ? Theme.MUTED : Color.WHITE);
                int tx = x1 + (w - tf.stringWidth(shortLabel)) / 2;
                int ty = top + (barHeight + tf.getAscent() - tf.getDescent()) / 2;
                g2.drawString(shortLabel, tx, ty);
                g2.setFont(Theme.HINT);
                fm = g2.getFontMetrics();
            }
        }

        // tick marks at the start of every slice, plus the final end
        g2.setFont(Theme.HINT);
        g2.setColor(Theme.MUTED);
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < slices.size(); i++) {
            int value = slices.get(i).start;
            int x = left + Math.round((value - first) * (float) usable / span);
            g2.setColor(Theme.BORDER_DARK);
            g2.drawLine(x, top + barHeight, x, top + barHeight + 4);
            g2.setColor(Theme.MUTED);
            String label = String.valueOf(value);
            g2.drawString(label, x - fm.stringWidth(label) / 2, top + barHeight + 15);
        }
        int endX = left + usable;
        g2.setColor(Theme.BORDER_DARK);
        g2.drawLine(endX, top + barHeight, endX, top + barHeight + 4);
        String endLabel = String.valueOf(last);
        g2.setColor(Theme.MUTED);
        g2.drawString(endLabel, endX - fm.stringWidth(endLabel) / 2, top + barHeight + 15);

        // small legend underneath so every colour has a name
        int lx = left;
        int ly = top + barHeight + 30;
        g2.setFont(Theme.HINT);
        fm = g2.getFontMetrics();
        for (Map.Entry<String, Color> e : colours.entrySet()) {
            g2.setColor(e.getValue());
            g2.fillRect(lx, ly - 7, 9, 9);
            g2.setColor(Theme.HEADING);
            g2.drawString(e.getKey(), lx + 13, ly);
            lx += 13 + fm.stringWidth(e.getKey()) + 12;
        }

        // the three averages sit in the empty strip above the bars
        String summary = "";
        if (!avgWaiting.isEmpty()) {
            summary = "average wait " + avgWaiting;
        }
        if (!avgTurnaround.isEmpty()) {
            summary += (summary.isEmpty() ? "" : "   |   ") + "turnaround " + avgTurnaround;
        }
        if (!avgResponse.isEmpty()) {
            summary += (summary.isEmpty() ? "" : "   |   ") + "response " + avgResponse;
        }
        if (!summary.isEmpty()) {
            g2.setFont(Theme.LABEL_BOLD);
            java.awt.FontMetrics sm = g2.getFontMetrics();
            g2.setColor(Theme.HEADING);
            g2.drawString(summary, right - sm.stringWidth(summary), 12);
        }
        g2.dispose();
    }
}
