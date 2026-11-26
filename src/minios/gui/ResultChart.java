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
        new Color(0x3D, 0x6E, 0xA8), new Color(0x4C, 0x8B, 0x6A),
        new Color(0x8C, 0x5A, 0xA0), new Color(0xB5, 0x6B, 0x38),
        new Color(0x2F, 0x7D, 0x8C), new Color(0x9A, 0x4A, 0x5C),
        new Color(0x6B, 0x7A, 0x3A), new Color(0x5A, 0x62, 0xA8)
    };
    private static final Color IDLE = new Color(0xDD, 0xE1, 0xE6);
    private static final Color HOLE = new Color(0xE8, 0xEC, 0xF1);
    private static final Color FAULT = new Color(0x9C, 0x22, 0x1A);

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
        wanted = 118;
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
        wanted = 112;
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
        wanted = 118;
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
        wanted = 124;
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
        wanted = 124;
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
        wanted = 30 + pieces.size() * 18 + 8;
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

    // The CPU Gantt chart. Coloured blocks on a time axis, the algorithm
    // name above, the process names inside the blocks, a tick under each
    // start and a legend at the bottom. A preempted slice ends in a star.
    private void paintGantt(Graphics2D g2, int left, int right, int usable) {
        int padLeft = 8;
        int padRight = 8;
        int width = usable - padLeft - padRight;
        int totalTime = pieces.get(pieces.size() - 1).end;
        if (width <= 20 || totalTime <= 0) {
            return;
        }


        // the bar sits low enough to leave room for a caption above it
        int barY = 26;
        int barH = Math.max(30, getHeight() - barY - 56);

        int lastTickX = -100;
        for (int i = 0; i < pieces.size(); i++) {
            Piece seg = pieces.get(i);
            int x1 = left + padLeft + (int) Math.round((double) seg.start / totalTime * width);
            int x2 = left + padLeft + (int) Math.round((double) seg.end / totalTime * width);
            int w = Math.max(2, x2 - x1);

            g2.setColor(seg.idle ? IDLE : colourOf(seg.label));
            g2.fillRect(x1, barY, w, barH);
            g2.setColor(Theme.SURFACE);
            g2.drawRect(x1, barY, w, barH);

            // the name goes inside the block when it fits
            g2.setFont(Theme.LABEL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            String name = seg.label + seg.suffix;
            if (fm.stringWidth(name) + 6 <= w) {
                g2.setColor(seg.idle ? Theme.MUTED : Color.WHITE);
                int tx = x1 + (w - fm.stringWidth(name)) / 2;
                int ty = barY + (barH + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(name, tx, ty);
            }

            // a tick under every start, skipped when two would overlap
            g2.setFont(Theme.HINT);
            int baseY = barY + barH;
            if (x1 - lastTickX >= 22) {
                g2.setColor(Theme.BORDER_DARK);
                g2.drawLine(x1, baseY, x1, baseY + 5);
                g2.setColor(Theme.MUTED);
                String startText = String.valueOf(seg.start);
                int sx = x1 - g2.getFontMetrics().stringWidth(startText) / 2;
                g2.drawString(startText, Math.max(0, sx), baseY + 17);
                lastTickX = x1;
            }
            if (i == pieces.size() - 1) {
                g2.setColor(Theme.BORDER_DARK);
                g2.drawLine(x2, baseY, x2, baseY + 5);
                g2.setColor(Theme.MUTED);
                String endText = String.valueOf(seg.end);
                int ex = x2 - g2.getFontMetrics().stringWidth(endText) / 2;
                // keep the last label inside the picture
                int limit = getWidth() - 4 - g2.getFontMetrics().stringWidth(endText);
                g2.drawString(endText, Math.min(ex, limit), baseY + 17);
            }
        }

        // legend at the bottom, one colour per process
        int lx = left + padLeft;
        int ly = barY + barH + 34;
        g2.setFont(Theme.HINT);
        FontMetrics lf = g2.getFontMetrics();
        for (Map.Entry<String, Color> e : colours.entrySet()) {
            int need = 14 + lf.stringWidth(e.getKey()) + 10;
            if (lx + need > right) {
                break;
            }
            g2.setColor(e.getValue());
            g2.fillRect(lx, ly - 9, 10, 10);
            g2.setColor(Theme.HEADING);
            g2.drawString(e.getKey(), lx + 14, ly);
            lx += need + 4;
        }
        g2.setColor(IDLE);
        g2.fillRect(lx, ly - 9, 10, 10);
        g2.setColor(Theme.MUTED);
        g2.drawString("idle", lx + 14, ly);
    }

    private void paintTimeline(Graphics2D g2, int left, int right, int usable) {
        int top = 24;
        int barHeight = 34;
        int first = pieces.get(0).start;
        int last = pieces.get(pieces.size() - 1).end;
        int span = Math.max(1, last - first);

        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();

        for (Piece s : pieces) {
            int x1 = left + Math.round((s.start - first) * (float) usable / span);
            int x2 = left + Math.round((s.end - first) * (float) usable / span);
            int w = Math.max(2, x2 - x1);
            Color fill = s.fault ? FAULT : s.idle ? IDLE : colourOf(s.label);
            g2.setColor(fill);
            g2.fillRect(x1, top, w, barHeight);
            g2.setColor(Theme.SURFACE);
            g2.drawRect(x1, top, w, barHeight);

            String name = s.label + s.suffix;
            g2.setFont(Theme.LABEL);
            FontMetrics tf = g2.getFontMetrics();
            if (tf.stringWidth(name) + 6 < w) {
                g2.setColor(s.idle ? Theme.MUTED : Color.WHITE);
                int tx = x1 + (w - tf.stringWidth(name)) / 2;
                int ty = top + (barHeight + tf.getAscent() - tf.getDescent()) / 2;
                g2.drawString(name, tx, ty);
            }
            g2.setFont(Theme.HINT);
            fm = g2.getFontMetrics();
        }

        // a tick at every start, plus the final end
        g2.setFont(Theme.HINT);
        fm = g2.getFontMetrics();
        for (int i = 0; i < pieces.size(); i++) {
            tick(g2, fm, left, usable, span, first, pieces.get(i).start, top + barHeight);
        }
        tick(g2, fm, left, usable, span, first, last, top + barHeight);

        legend(g2, left, top + barHeight + 27, true);
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

    private void paintSteps(Graphics2D g2, int left, int usable) {
        // one column per move, scaled to the highest cylinder reached
        int top = 26;
        // the column is wider than the box so the movement number fits in
        // the gap between one cylinder and the next
        // the gap must hold a three digit movement number
        int gap = Math.max(16, tabPaneMetrics(g2).stringWidth("888"));
        int size = Math.min(56, Math.max(gap + 20, usable / Math.max(1, pieces.size())));
        int box = size - gap;
        int highest = 1;
        for (Piece p : pieces) {
            highest = Math.max(highest, p.value);
        }
        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();
        int x = left;
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            Color fill = i == 0 ? Theme.BORDER_DARK : colourOf("head");
            g2.setColor(fill);
            g2.fillRect(x, top, box, box);
            g2.setFont(Theme.LABEL);
            FontMetrics tf = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            String text = String.valueOf(p.value);
            if (tf.stringWidth(text) + 4 < box) {
                g2.drawString(text, x + (box - tf.stringWidth(text)) / 2,
                        top + (box + tf.getAscent() - tf.getDescent()) / 2);
            }
            g2.setFont(Theme.HINT);
            fm = g2.getFontMetrics();
            if (i < pieces.size() - 1) {
                // the distance travelled goes in the gap, or under the box
                // when the gap is too narrow for a number
                int next = pieces.get(i + 1).value;
                String move = String.valueOf(Math.abs(next - p.value));
                g2.setColor(Theme.MUTED);
                int mx = x + box + (size - box - fm.stringWidth(move)) / 2;
                g2.drawString(move, mx, top + box / 2 + 4);
            }
            x += size;
        }
        g2.setColor(Theme.MUTED);
        g2.drawString("cylinder per box, movement between boxes, scale 0.." + highest,
                left, top + box + 20);
    }

    // Metrics of the small font, used to size the gaps in the step chart.
    private java.awt.FontMetrics tabPaneMetrics(Graphics2D g2) {
        g2.setFont(Theme.HINT);
        return g2.getFontMetrics();
    }

    private void paintBars(Graphics2D g2, int left, int right, int usable) {
        int top = 24;
        int rowHeight = 18;
        int labelWidth = 90;
        int total = Math.max(1, pieces.get(0).end);

        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            int y = top + i * rowHeight;
            g2.setColor(Theme.HEADING);
            g2.drawString(p.label, left, y + rowHeight - 5);

            String value = String.valueOf(p.value);
            int numberWidth = fm.stringWidth(value) + 12;
            int barX = left + labelWidth;
            int barW = Math.max(20, right - barX - numberWidth);
            int fill = Math.round(p.value * (float) barW / total);
            g2.setColor(Theme.HAIRLINE);
            g2.fillRect(barX, y + 2, barW, rowHeight - 6);
            // one colour for all bars, unless a colour marks something
            // special such as a page that is on disk
            g2.setColor(p.idle ? IDLE : Theme.ACCENT);
            g2.fillRect(barX, y + 2, Math.max(1, fill), rowHeight - 6);

            g2.setColor(Theme.HEADING);
            g2.drawString(value, right - fm.stringWidth(value), y + rowHeight - 5);
        }
    }

    private void paintMemory(Graphics2D g2, int left, int usable) {
        int top = 24;
        int barHeight = 34;
        int first = pieces.get(0).start;
        int last = pieces.get(pieces.size() - 1).end;
        int span = Math.max(1, last - first);

        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();
        for (Piece p : pieces) {
            int x1 = left + Math.round((p.start - first) * (float) usable / span);
            int x2 = left + Math.round((p.end - first) * (float) usable / span);
            int w = Math.max(2, x2 - x1);
            g2.setColor(p.idle ? HOLE : colourOf(p.label));
            g2.fillRect(x1, top, w, barHeight);
            g2.setColor(Theme.SURFACE);
            g2.drawRect(x1, top, w, barHeight);

            String name = p.label + " (" + (p.end - p.start) + ")";
            g2.setFont(Theme.LABEL);
            FontMetrics tf = g2.getFontMetrics();
            if (tf.stringWidth(name) + 6 < w) {
                g2.setColor(p.idle ? Theme.MUTED : Color.WHITE);
                g2.drawString(name, x1 + (w - tf.stringWidth(name)) / 2,
                        top + (barHeight + tf.getAscent() - tf.getDescent()) / 2);
            }
            g2.setFont(Theme.HINT);
            fm = g2.getFontMetrics();
        }
        tick(g2, fm, left, usable, span, first, first, top + barHeight);
        tick(g2, fm, left, usable, span, first, last, top + barHeight);
        legend(g2, left, top + barHeight + 27, false);
    }

    // Small colour key, so every colour in the picture has a name.
    private void legend(Graphics2D g2, int startX, int y, boolean withIdle) {
        int x = startX;
        g2.setFont(Theme.HINT);
        FontMetrics fm = g2.getFontMetrics();
        List<String> names = new ArrayList<>(colours.keySet());
        for (String name : names) {
            if (x + fm.stringWidth(name) > getWidth() - 10) {
                break;
            }
            g2.setColor(colours.get(name));
            g2.fillRect(x, y - 7, 9, 9);
            g2.setColor(Theme.HEADING);
            g2.drawString(name, x + 13, y);
            x += 13 + fm.stringWidth(name) + 12;
        }
        if (withIdle && x + 13 + fm.stringWidth("idle") < getWidth() - 10) {
            g2.setColor(IDLE);
            g2.fillRect(x, y - 7, 9, 9);
            g2.setColor(Theme.MUTED);
            g2.drawString("idle", x + 13, y);
        }
    }
}
