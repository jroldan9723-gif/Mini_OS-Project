package minios.gui;

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

// Draws the result of a module as a picture. Four shapes are enough for the
// whole simulator:
//   GANTT     the CPU schedule as a bar chart on a time axis
//   TIMELINE  one block per reference       (page replacement)
//   STEPS     a flat sequence of moves      (disk head movement)
//   BARS      one bar per row with a total  (paging, segmentation, Banker)
//   MEMORY    how memory is laid out        (fragmentation, multiprogramming)
// The numbers always come from the text the module prints, so a picture can
// never show something the report does not say.
final class ResultChart extends JComponent {

    private static final long serialVersionUID = 1L;

    enum Kind { GANTT, TIMELINE, STEPS, BARS, MEMORY }

    // One piece of the picture. Which fields matter depends on the kind.
    private static final class Piece {
        String label;      // name drawn on the piece
        String suffix;     // short note kept beside the name in the report
        int start;
        int end;
        int value;         // a length or a count, depending on the kind
        boolean idle;      // nothing running, drawn grey
        boolean fault;     // a miss or a rejected request, drawn red
    }

    private final List<Piece> pieces = new ArrayList<>();
    private final Map<String, Color> colours = new LinkedHashMap<>();
    private Kind kind = Kind.TIMELINE;
    private String caption = "";
    private String summary = "";
    private String emptyHint = "Run the module to see the picture.";
    // how tall the picture wants to be, so a long list of bars is not cut off
    private int wanted = 104;

    // Colours reused in order, so the same name keeps one colour all run.
    private static final Color[] PALETTE = {
        new Color(79, 129, 189), new Color(192, 80, 77),
        new Color(155, 187, 89), new Color(128, 100, 162),
        new Color(75, 172, 198), new Color(247, 150, 70),
        new Color(119, 147, 60), new Color(149, 55, 53)
    };
    private static final Color IDLE = new Color(0xDC, 0xDC, 0xDC);
    private static final Color HOLE = new Color(0xDC, 0xDC, 0xDC);
    private static final Color GRID = new Color(0xE6, 0xE6, 0xE6);
    private static final Color AXIS = new Color(0x5A, 0x5A, 0x5A);
    private static final Color INK = new Color(0x32, 0x32, 0x32);
    private static final Color FAULT = new Color(192, 80, 77);

    ResultChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 104));
        setMinimumSize(new Dimension(200, 104));
    }

    void setEmptyHint(String hint) {
        emptyHint = hint;
    }

    private void reset(Kind newKind) {
        pieces.clear();
        colours.clear();
        kind = newKind;
        caption = "";
        summary = "";
    }

    private Piece add(String label, int start, int end) {
        Piece p = new Piece();
        p.label = label;
        p.start = start;
        p.end = end;
        pieces.add(p);
        return p;
    }

    private Color colourOf(String name) {
        Color c = colours.get(name);
        if (c == null) {
            c = PALETTE[colours.size() % PALETTE.length];
            colours.put(name, c);
        }
        return c;
    }

    // ---------------------------------------------------------------- CPU
    // Reads a line such as "  0-5 P1 | 5-7 P3 | 7-10 P2 (preempted)".
    boolean loadTimeline(String report) {
        reset(Kind.GANTT);
        String chart = findChartLine(report);
        if (chart != null) {
            for (String part : chart.split("\\|")) {
                String piece = part.trim();
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
                    if (end <= start) {
                        continue;
                    }
                    String label = piece.substring(space + 1).trim();
                    Piece p = add(label, start, end);
                    p.idle = label.startsWith("idle");
                    // a preempted slice keeps the process colour and is
                    // marked with a star, as the report words it
                    p.suffix = label.contains("(preempted)") ? "*" : "";
                    p.label = label.replace(" (preempted)", "");
                    if (!p.idle) {
                        colourOf(p.label);
                    }
                } catch (NumberFormatException e) {
                    // a line that does not parse is simply not drawn
                }
            }
        }
        summary = averages(report);
        // the report starts with "Algorithm: SRTF (Shortest Remaining ...)"
        String title = "";
        for (String line : (report == null ? "" : report).split("\n")) {
            if (line.startsWith("Algorithm:")) {
                title = line.substring("Algorithm:".length()).trim();
                break;
            }
        }
        if (title.contains(" (")) {
            title = title.substring(0, title.indexOf(" ("));
        }
        caption = title;
        wanted = 128;
        setPreferredSize(new Dimension(400, wanted));
        repaint();
        return !pieces.isEmpty();
    }

    private static String averages(String report) {
        String wait = readAfter(report, "Average waiting time");
        String turn = readAfter(report, "Average turnaround time:");
        String resp = readAfter(report, "Average response time");
        String text = "";
        if (!wait.isEmpty()) {
            text = "average wait " + wait;
        }
        if (!turn.isEmpty()) {
            text += (text.isEmpty() ? "" : "   |   ") + "turnaround " + turn;
        }
        if (!resp.isEmpty()) {
            text += (text.isEmpty() ? "" : "   |   ") + "response " + resp;
        }
        return text;
    }

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

    // ------------------------------------------------------- page replacement
    // Reads the FIFO table: one row per reference, frames in columns, and a
    // HIT or FAULT word at the end of the row.
    boolean loadPageFrames(String report) {
        reset(Kind.TIMELINE);
        String frames = "";
        for (String line : report.split("\n")) {
            int at = line.indexOf("(frames =");
            if (at >= 0 && line.trim().startsWith("FIFO")) {
                frames = line.substring(at).replace("(frames =", "").replace(")", "").trim();
                break;
            }
        }
        caption = frames.isEmpty() ? "FIFO" : "FIFO, " + frames + " frames";
        int slot = 0;
        boolean inTable = false;
        for (String line : report.split("\n")) {
            String t = line.trim();
            if (t.startsWith("FIFO")) {
                inTable = true;
                continue;
            }
            if (!inTable || t.isEmpty()) {
                continue;
            }
            if (t.startsWith("Total page faults")) {
                break;
            }
            String[] cols = t.split("\\|");
            if (cols.length < 3 || !t.contains("|")) {
                continue;
            }
            String[] left = cols[0].trim().split("\\s+");
            String result = cols[cols.length - 1].trim();
            if (left.length == 0) {
                continue;
            }
            try {
                int page = Integer.parseInt(left[0]);
                Piece p = add(String.valueOf(page), slot, slot + 1);
                p.fault = result.startsWith("FAULT");
                p.suffix = p.fault ? "" : " hit";
                p.idle = !p.fault;
                if (!p.fault) {
                    colourOf(p.label);
                }
                slot++;
            } catch (NumberFormatException e) {
                // header rows start with letters and are skipped
            }
        }
        String faults = readAfter(report, "Total page faults:");
        String hits = readAfter(report, "Total page hits");
        String rate = readAfter(report, "Page fault rate");
        if (!faults.isEmpty()) {
            summary = "faults " + faults + "   |   hits " + hits + "   |   fault rate " + rate;
        }
        summary = summary.isEmpty() ? "" : summary;
        wanted = 128;
        setPreferredSize(new Dimension(400, wanted));
        repaint();
        return !pieces.isEmpty();
    }

    // ------------------------------------------------------ memory diagrams
    // Reads one memory snap map line, e.g.
    //   [A 0-400] [B 400-500] [C 500-800] [HOLE 800-1000]
    // or the multiprogramming form
    //   [J1 0-40, 40 KB] [free 65-100]
    boolean loadMemoryMap(String mapLine) {
        reset(Kind.MEMORY);
        if (mapLine == null) {
            return false;
        }
        int bracket = mapLine.indexOf('[');
        if (bracket >= 0) {
            mapLine = mapLine.substring(bracket);
        }
        int note = mapLine.indexOf('|');
        if (note >= 0) {
            mapLine = mapLine.substring(0, note);
        }
        for (String part : mapLine.split("\\[")) {
            if (part.trim().isEmpty()) {
                continue;
            }
            String body = part.replace("]", "").trim();
            int space = body.indexOf(' ');
            if (space < 0) {
                continue;
            }
            String name = body.substring(0, space);
            String range = body.substring(space + 1);
            int comma = range.indexOf(',');
            if (comma >= 0) {
                range = range.substring(0, comma);
            }
            int dash = range.indexOf('-');
            if (dash < 0) {
                continue;
            }
            try {
                int start = Integer.parseInt(range.substring(0, dash).trim());
                int end = Integer.parseInt(range.substring(dash + 1).trim());
                if (end <= start) {
                    continue;
                }
                Piece p = add(name, start, end);
                p.idle = name.equalsIgnoreCase("free")
                        || name.equalsIgnoreCase("HOLE");
                if (p.idle) {
                    colours.remove(name);
                } else {
                    colourOf(name);
                }
            } catch (NumberFormatException e) {
                // an entry that does not parse is left out
            }
        }
        wanted = 128;
        setPreferredSize(new Dimension(400, wanted));
        repaint();
        return !pieces.isEmpty();
    }

    // ------------------------------------------------------------------ disk
    // Reads "Order of service : 53 -> 65 -> 67 -> ..." into a move sequence.
    boolean loadSequence(String line) {
        reset(Kind.STEPS);
        if (line == null) {
            return false;
        }
        int colon = line.indexOf(':');
        if (colon < 0) {
            return false;
        }
        String list = line.substring(colon + 1);
        for (String part : list.split("->")) {
            String token = part.trim();
            if (token.isEmpty() || token.startsWith("(")) {
                continue;
            }
            String[] words = token.split("\\s+");
            try {
                int cylinder = Integer.parseInt(words[0]);
                Piece p = add(String.valueOf(cylinder), 0, 0);
                p.value = cylinder;
            } catch (NumberFormatException e) {
                // "(end of disk)" and "(wrap)" are notes, not cylinders
            }
        }
        wanted = 168;
        setPreferredSize(new Dimension(400, wanted));
        repaint();
        return pieces.size() > 1;
    }

    // ------------------------------------------------------------------ bars
    // A simple bar per row: label, value, and a total for the scale.
    void addBar(String label, int value, int total) {
        Piece p = add(label, 0, total);
        p.value = value;
    }

    // ------------------------------------------------------------- Banker
    // One bar per process: how much it still needs against the most it ever
    // needed, plus how much of the available pool is currently allocated.
    // Read from the "Need matrix" table and the "Work (available) starts at"
    // line, so the bars agree with the safety check underneath them.
    boolean loadBanker(String report) {
        reset(Kind.BARS);
        if (report == null) {
            return false;
        }
        List<String> names = new ArrayList<>();
        List<Integer> needs = new ArrayList<>();
        int rowsLeft = 0;
        boolean inNeed = false;
        int top = 0;

        for (String line : report.split("\n")) {
            String t = line.trim();
            if (t.startsWith("Need matrix")) {
                inNeed = true;
                continue;
            }
            if (!inNeed) {
                continue;
            }
            if (t.isEmpty()) {
                if (rowsLeft > 0) {
                    continue;
                }
                inNeed = false;
                continue;
            }
            String[] parts = t.split("\\s+");
            if (parts.length < 2) {
                continue;
            }
            int sum = 0;
            int count = 0;
            for (int i = 1; i < parts.length; i++) {
                try {
                    sum += Integer.parseInt(parts[i]);
                    count++;
                } catch (NumberFormatException e) {
                    count = 0;
                    break;
                }
            }
            if (count == 0) {
                continue;
            }
            names.add(parts[0]);
            needs.add(sum);
            top = Math.max(top, sum);
            rowsLeft++;
        }

        if (names.isEmpty()) {
            return false;
        }
        // the work line gives the size of the resource pool
        String work = readAfter(report, "Work (available) starts at");
        int pool = 0;
        for (String number : work.replaceAll("[^0-9,]", ",").split(",")) {
            if (number.isEmpty()) {
                continue;
            }
            try {
                pool += Integer.parseInt(number);
            } catch (NumberFormatException e) {
                // ignore a piece that is not a number
            }
        }
        // keep the chart readable: at most ten bars, the widest needs first
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            order.add(i);
        }
        if (names.size() > 10) {
            order.sort((a, b) -> needs.get(b) - needs.get(a));
            order = new ArrayList<>(order.subList(0, 10));
        }

        startBars("Total resources still needed per process");
        int total = Math.max(1, Math.max(top, pool));
        for (int i : order) {
            addBar(names.get(i), needs.get(i), total);
        }
        String safe = report.contains("SAFE sequence") ? "safe state" : "";
        finishBars(names.size() > 10
                ? "10 of " + names.size() + " processes shown, largest need first"
                : safe.isEmpty() ? "" : safe);
        return true;
    }

    // ------------------------------------------------------------- Paging
    // One bar per page: the memory it occupies, with pages on disk at zero.
    boolean loadPaging(String report) {
        reset(Kind.BARS);
        if (report == null) {
            return false;
        }
        // "Logical address space = 8 pages x 1024 bytes = 8192 bytes"
        int size = 0;
        for (String line : report.split("\n")) {
            int at = line.indexOf("pages x");
            if (at < 0) {
                continue;
            }
            String rest = line.substring(at + "pages x".length()).trim();
            String[] words = rest.split("\\s+");
            try {
                size = Integer.parseInt(words[0]);
            } catch (RuntimeException e) {
                size = 0;
            }
            break;
        }
        if (size <= 0) {
            size = 1;
        }
        List<String> pages = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (String line : report.split("\n")) {
            String t = line.trim();
            int bar = t.indexOf('|');
            if (bar < 0) {
                continue;
            }
            try {
                Integer.parseInt(t.substring(0, bar).trim());
            } catch (NumberFormatException e) {
                continue;
            }
            pages.add("P" + t.substring(0, bar).trim());
            values.add(t.substring(bar + 1).trim().equals("on disk") ? 0 : size);
        }
        if (pages.isEmpty()) {
            return false;
        }
        startBars("Memory used per page");
        for (int i = 0; i < pages.size(); i++) {
            addBar(pages.get(i), values.get(i), size);
            pieces.get(pieces.size() - 1).idle = values.get(i) == 0;
        }
        int onDisk = 0;
        for (int v : values) {
            if (v == 0) {
                onDisk++;
            }
        }
        finishBars(onDisk + " page(s) on disk, page size " + size + " bytes");
        return true;
    }

    // ------------------------------------------------------- Segmentation
    // One bar per segment: the bytes it occupies in memory.
    boolean loadSegments(String report) {
        reset(Kind.BARS);
        if (report == null) {
            return false;
        }
        List<String> segs = new ArrayList<>();
        List<Integer> limits = new ArrayList<>();
        int top = 0;
        boolean inTable = false;
        for (String line : report.split("\n")) {
            String t = line.trim();
            if (t.startsWith("Segment table in use")) {
                inTable = true;
                continue;
            }
            if (!inTable) {
                continue;
            }
            String[] parts = t.split("\\s*\\|\\s*");
            if (parts.length < 3) {
                if (!t.isEmpty() && !t.contains("-")) {
                    inTable = false;
                }
                continue;
            }
            try {
                int id = Integer.parseInt(parts[0].trim());
                int limit = Integer.parseInt(parts[2].trim());
                segs.add("Seg " + id);
                limits.add(limit);
                top = Math.max(top, limit);
            } catch (NumberFormatException e) {
                // the header row is skipped
            }
        }
        if (segs.isEmpty()) {
            return false;
        }
        startBars("Size of each segment");
        for (int i = 0; i < segs.size(); i++) {
            addBar(segs.get(i), limits.get(i), Math.max(1, top));
        }
        finishBars(segs.size() + " segment(s), largest is " + top + " bytes");
        return true;
    }

    // --------------------------------------------------------------- Disk
    // A run of boxes: the service order, with the movement between them.
    boolean loadDisk(String report) {
        reset(Kind.STEPS);
        if (report == null) {
            return false;
        }
        String order = null;
        for (String line : report.split("\n")) {
            if (line.contains("Order of service")) {
                order = line;
                break;
            }
        }
        if (order == null) {
            return false;
        }
        loadSequence(order);
        for (String line : report.split("\n")) {
            if (line.contains("Total head movement")) {
                String total = line.substring(line.indexOf(':') + 1).trim();
                summary = "total movement " + total.replace("cylinders", "").trim();
                break;
            }
        }
        wanted = 168;
        setPreferredSize(new Dimension(400, wanted));
        repaint();
        return pieces.size() > 1;
    }

    // ------------------------------------------------- Multiprogramming
    // The last memory line of the report shows how memory ended up.
    boolean loadMulti(String report) {
        reset(Kind.MEMORY);
        if (report == null) {
            return false;
        }
        // the very last map is usually all free, which says nothing, so
        // prefer the last map that still has a job loaded
        String last = null;
        String any = null;
        for (String line : report.split("\n")) {
            String t = line.trim();
            if (!t.startsWith("memory: [")) {
                continue;
            }
            String map = t.substring("memory: ".length());
            any = map;
            if (!map.startsWith("[free ") || map.contains("] [J")) {
                last = map;
            }
        }
        if (last == null) {
            last = any;
        }
        if (last == null) {
            return false;
        }
        boolean ok = loadMemoryMap(last);
        if (ok) {
            caption = "Memory at the end of the run";
        }
        return ok;
    }

    void startBars(String title) {
        reset(Kind.BARS);
        caption = title;
    }

    void finishBars(String footer) {
        summary = footer;
        wanted = 150;
        setPreferredSize(new Dimension(400, wanted));
        setMinimumSize(new Dimension(200, wanted));
        repaint();
    }

    void setCaption(String text) {
        caption = text;
    }

    boolean hasChart() {
        return !pieces.isEmpty();
    }

    // Height the panel should give this picture.
    int neededHeight() {
        return wanted;
    }

    private static String readAfter(String report, String key) {
        if (report == null) {
            return "";
        }
        for (String line : report.split("\n")) {
            if (line.trim().startsWith(key)) {
                int colon = line.lastIndexOf(':');
                if (colon < 0) {
                    colon = line.lastIndexOf(' ');
                }
                return line.substring(colon + 1).trim();
            }
        }
        return "";
    }

    // ---------------------------------------------------------------- paint
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int left = 8;
        int right = getWidth() - 10;
        int usable = Math.max(60, right - left);

        if (pieces.isEmpty()) {
            g2.setFont(Theme.HINT);
            g2.setColor(Theme.MUTED);
            g2.drawString(emptyHint, left, 26);
            g2.dispose();
            return;
        }

        if (!caption.isEmpty()) {
            g2.setFont(Theme.HINT);
            g2.setColor(Theme.HEADING);
            g2.drawString(caption, left, 12);
        }
        if (!summary.isEmpty()) {
            g2.setFont(Theme.LABEL_BOLD);
            FontMetrics sm = g2.getFontMetrics();
            g2.setColor(Theme.HEADING);
            g2.drawString(summary, right - sm.stringWidth(summary), 12);
        }

        switch (kind) {
            case GANTT:
                paintGantt(g2, left, right, usable);
                break;
            case TIMELINE:
                paintTimeline(g2, left, right, usable);
                break;
            case STEPS:
                paintSteps(g2, left, usable);
                break;
            case BARS:
                paintBars(g2, left, right, usable);
                break;
            case MEMORY:
                paintMemory(g2, left, usable);
                break;
            default:
                break;
        }
        g2.dispose();
    }

    // The CPU Gantt chart, drawn the way the reference does it: a tall
    // coloured bar with the process name bold inside each block, the
    // algorithm name above it, a tick under every start and a colour key
    // along the bottom. A preempted slice ends in a star.
    private void paintGantt(Graphics2D g2, int left, int right, int usable) {
        int padLeft = 8;
        int width = usable - padLeft - 16;
        int totalTime = pieces.get(pieces.size() - 1).end;
        if (width <= 20 || totalTime <= 0) {
            return;
        }

        int barY = 30;
        int barH = Math.max(28, getHeight() - barY - 58);

        int lastTickX = -100;
        for (int i = 0; i < pieces.size(); i++) {
            Piece seg = pieces.get(i);
            int x1 = left + padLeft + (int) Math.round((double) seg.start / totalTime * width);
            int x2 = left + padLeft + (int) Math.round((double) seg.end / totalTime * width);
            int w = Math.max(2, x2 - x1);

            g2.setColor(seg.idle ? IDLE : colourOf(seg.label));
            g2.fillRect(x1, barY, w, barH);
            g2.setColor(Color.WHITE);
            g2.drawRect(x1, barY, w, barH);

            g2.setFont(Theme.LABEL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            String name = seg.label + seg.suffix;
            if (fm.stringWidth(name) + 6 <= w) {
                g2.setColor(seg.idle ? new Color(90, 90, 90) : Color.WHITE);
                g2.drawString(name, x1 + (w - fm.stringWidth(name)) / 2,
                        barY + (barH + fm.getAscent() - fm.getDescent()) / 2);
            }

            g2.setFont(Theme.HINT);
            g2.setColor(INK);
            int baseY = barY + barH;
            if (x1 - lastTickX >= 22) {
                g2.drawLine(x1, baseY, x1, baseY + 5);
                g2.drawString(String.valueOf(seg.start), x1 - 3, baseY + 17);
                lastTickX = x1;
            }
            g2.setFont(Theme.HINT);
            if (i == pieces.size() - 1) {
                g2.drawLine(x2, baseY, x2, baseY + 5);
                String endText = String.valueOf(seg.end);
                int ex = x2 - g2.getFontMetrics().stringWidth(endText) / 2;
                int limit = getWidth() - 4 - g2.getFontMetrics().stringWidth(endText);
                g2.drawString(endText, Math.min(ex, limit), baseY + 17);
            }
        }

        legend(g2, left + padLeft, barY + barH + 36, true);
    }

    // Page replacement keeps the block style but carries a result strip:
    // one block per reference, red for a miss, grey for a hit.
    private void paintTimeline(Graphics2D g2, int left, int right, int usable) {
        int top = 26;
        int barHeight = Math.max(30, getHeight() - top - 58);
        int first = pieces.get(0).start;
        int last = pieces.get(pieces.size() - 1).end;
        int span = Math.max(1, last - first);

        FontMetrics fm = g2.getFontMetrics();
        for (Piece s : pieces) {
            int x1 = left + Math.round((s.start - first) * (float) usable / span);
            int x2 = left + Math.round((s.end - first) * (float) usable / span);
            int w = Math.max(2, x2 - x1);
            g2.setColor(s.fault ? FAULT : IDLE);
            g2.fillRect(x1, top, w, barHeight);
            g2.setColor(Color.WHITE);
            g2.drawRect(x1, top, w, barHeight);

            String name = s.label + s.suffix;
            g2.setFont(Theme.LABEL_BOLD);
            FontMetrics tf = g2.getFontMetrics();
            if (tf.stringWidth(name) + 6 < w) {
                g2.setColor(s.fault ? Color.WHITE : new Color(90, 90, 90));
                g2.drawString(name, x1 + (w - tf.stringWidth(name)) / 2,
                        top + (barHeight + tf.getAscent() - tf.getDescent()) / 2);
            }
            g2.setFont(Theme.HINT);
            fm = g2.getFontMetrics();
        }

        g2.setFont(Theme.HINT);
        fm = g2.getFontMetrics();
        for (int i = 0; i < pieces.size(); i++) {
            tick(g2, fm, left, usable, span, first, pieces.get(i).start, top + barHeight);
        }
        tick(g2, fm, left, usable, span, first, last, top + barHeight);

        // key: what a red block means and what a grey block means
        int lx = left;
        int ly = top + barHeight + 36;
        g2.setFont(Theme.HINT);
        fm = g2.getFontMetrics();
        g2.setColor(FAULT);
        g2.fillRect(lx, ly - 9, 10, 10);
        g2.setColor(INK);
        g2.drawString("miss", lx + 14, ly);
        lx += 24 + fm.stringWidth("miss");
        g2.setColor(IDLE);
        g2.fillRect(lx, ly - 9, 10, 10);
        g2.setColor(INK);
        g2.drawString("hit", lx + 14, ly);
    }

    private void tick(Graphics2D g2, FontMetrics fm, int left, int usable,
            int span, int first, int value, int baseY) {
        int x = left + Math.round((value - first) * (float) usable / span);
        g2.setColor(Theme.BORDER_DARK);
        g2.drawLine(x, baseY, x, baseY + 4);
        g2.setColor(Theme.MUTED);
        String label = String.valueOf(value);
        int tx = x - fm.stringWidth(label) / 2;
        // keep the last label inside the picture instead of half off the edge
        if (tx + fm.stringWidth(label) > getWidth() - 4) {
            tx = getWidth() - 4 - fm.stringWidth(label);
        }
        if (tx < 0) {
            tx = 0;
        }
        g2.drawString(label, tx, baseY + 15);
    }

    // The disk chart, drawn the way the reference draws its comparison:
    // a vertical bar per stop in the service order, a value axis on the
    // left, gridlines across and the cylinder under each bar.
    private void paintSteps(Graphics2D g2, int left, int usable) {
        int top = 30;
        int bottom = getHeight() - 34;
        int right = left + usable - 16;
        if (bottom <= top || right <= left) {
            return;
        }

        int highest = 1;
        for (Piece p : pieces) {
            highest = Math.max(highest, p.value);
        }
        // round the axis up so the labels are readable numbers
        int axisMax = roundUp(highest);
        if (axisMax <= 0) {
            axisMax = 1;
        }

        // gridlines and the value axis
        g2.setFont(Theme.HINT);
        for (int i = 0; i <= 4; i++) {
            int y = bottom - (bottom - top) * i / 4;
            g2.setColor(GRID);
            g2.drawLine(left + 34, y, right, y);
            g2.setColor(AXIS);
            g2.drawString(String.valueOf(axisMax * i / 4), left, y + 4);
        }
        g2.setColor(AXIS);
        g2.drawLine(left + 34, top, left + 34, bottom);
        g2.drawLine(left + 34, bottom, right, bottom);

        int groups = pieces.size();
        int groupW = (right - left - 34) / Math.max(1, groups);
        int barW = Math.min(42, Math.max(8, groupW - 10));

        g2.setFont(Theme.HINT);
        FontMetrics nameFont = g2.getFontMetrics();
        for (int i = 0; i < groups; i++) {
            Piece p = pieces.get(i);
            int cx = left + 34 + i * groupW + groupW / 2;
            int h = (int) Math.round((double) p.value / axisMax * (bottom - top));
            // the first bar is the head position, drawn in the muted shade
            g2.setColor(i == 0 ? new Color(0xAF, 0xB9, 0xC6) : PALETTE[0]);
            g2.fillRect(cx - barW / 2, bottom - h, barW, Math.max(1, h));

            g2.setColor(INK);
            String name = String.valueOf(p.value);
            if (nameFont.stringWidth(name) <= groupW) {
                g2.drawString(name, cx - nameFont.stringWidth(name) / 2, bottom + 15);
            }
        }
    }

    // Rounds a maximum up so the axis reads 0, 25, 50, 75, 100. The four
    // gridline steps want a number that divides evenly, so start from the
    // step size and multiply up.
    private static int roundUp(int value) {
        if (value <= 4) {
            return 4;
        }
        int[] steps = {1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 300, 400, 500,
            1000, 2000, 2500, 5000};
        for (int step : steps) {
            int top = step * 4;
            if (top >= value) {
                return top;
            }
        }
        return (value / 4000 + 1) * 4000;
    }

    // A vertical bar per row, like the reference comparison chart: value
    // axis on the left, gridlines, the row name under each bar and the
    // number above it.
    private void paintBars(Graphics2D g2, int left, int right, int usable) {
        int top = 30;
        int bottom = getHeight() - 34;
        if (bottom <= top || right <= left) {
            return;
        }

        int highest = 1;
        for (Piece p : pieces) {
            highest = Math.max(highest, Math.max(p.value, p.end));
        }
        int axisMax = roundUp(highest);
        if (axisMax <= 0) {
            axisMax = 1;
        }

        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            int y = bottom - (bottom - top) * i / 4;
            g2.setColor(GRID);
            g2.drawLine(left + 34, y, right, y);
            g2.setColor(AXIS);
            g2.drawString(String.valueOf(axisMax * i / 4), left, y + 4);
        }
        g2.setColor(AXIS);
        g2.drawLine(left + 34, top, left + 34, bottom);
        g2.drawLine(left + 34, bottom, right, bottom);

        int groups = pieces.size();
        int groupW = (right - left - 34) / Math.max(1, groups);
        int barW = Math.min(40, Math.max(8, groupW - 10));

        for (int i = 0; i < groups; i++) {
            Piece p = pieces.get(i);
            int cx = left + 34 + i * groupW + groupW / 2;
            int h = (int) Math.round((double) p.value / axisMax * (bottom - top));
            g2.setColor(p.idle ? IDLE : PALETTE[0]);
            g2.fillRect(cx - barW / 2, bottom - h, barW, Math.max(1, h));

            // the number sits above the bar when there is room
            g2.setColor(INK);
            String value = String.valueOf(p.value);
            g2.drawString(value, cx - fm.stringWidth(value) / 2,
                    Math.max(top + 10, bottom - h - 5));

            // the row name goes under the bar, trimmed when the gap is tight
            String name = p.label;
            while (fm.stringWidth(name) > groupW && name.length() > 2) {
                name = name.substring(0, name.length() - 1);
            }
            g2.drawString(name, cx - fm.stringWidth(name) / 2, bottom + 15);
        }
    }

    // The memory map: one tall bar split into the jobs and the holes, each
    // block named and measured, with a colour key underneath.
    private void paintMemory(Graphics2D g2, int left, int usable) {
        int top = 26;
        int barHeight = Math.max(30, getHeight() - top - 58);
        int first = pieces.get(0).start;
        int last = pieces.get(pieces.size() - 1).end;
        int span = Math.max(1, last - first);

        FontMetrics fm = g2.getFontMetrics();
        for (Piece p : pieces) {
            int x1 = left + Math.round((p.start - first) * (float) usable / span);
            int x2 = left + Math.round((p.end - first) * (float) usable / span);
            int w = Math.max(2, x2 - x1);
            g2.setColor(p.idle ? HOLE : colourOf(p.label));
            g2.fillRect(x1, top, w, barHeight);
            g2.setColor(Color.WHITE);
            g2.drawRect(x1, top, w, barHeight);

            String name = p.label + " (" + (p.end - p.start) + ")";
            g2.setFont(Theme.LABEL_BOLD);
            FontMetrics tf = g2.getFontMetrics();
            if (tf.stringWidth(name) + 6 < w) {
                g2.setColor(p.idle ? new Color(90, 90, 90) : Color.WHITE);
                g2.drawString(name, x1 + (w - tf.stringWidth(name)) / 2,
                        top + (barHeight + tf.getAscent() - tf.getDescent()) / 2);
            }
            g2.setFont(Theme.HINT);
            fm = g2.getFontMetrics();
        }
        tick(g2, fm, left, usable, span, first, first, top + barHeight);
        tick(g2, fm, left, usable, span, first, last, top + barHeight);
        legend(g2, left, top + barHeight + 36, false);
    }

    // Small colour key, so every colour in the picture has a name.
    private void legend(Graphics2D g2, int startX, int y, boolean withIdle) {
        int x = startX;
        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();
        List<String> names = new ArrayList<>(colours.keySet());
        for (String name : names) {
            int need = 24 + fm.stringWidth(name);
            if (x + need > getWidth() - 10) {
                break;
            }
            g2.setColor(colours.get(name));
            g2.fillRect(x, y - 10, 10, 10);
            g2.setColor(INK);
            g2.drawString(name, x + 14, y);
            x += need;
        }
        if (withIdle && x + 24 + fm.stringWidth("idle") < getWidth() - 10) {
            g2.setColor(IDLE);
            g2.fillRect(x, y - 10, 10, 10);
            g2.setColor(INK);
            g2.drawString("idle", x + 14, y);
        }
    }
}
