package minios.gui;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Shows a module report. Plain text in a monospaced font, because the reports
// are column aligned tables and the Gantt charts: proportional text would break
// the alignment. Long lines wrap instead of forcing a sideways scrollbar.
class ReportView extends JScrollPane {

    private static final long serialVersionUID = 1L;

    private final JTextArea area = new JTextArea();

    ReportView() {
        area.setEditable(false);
        area.setFont(Theme.MONO);
        area.setForeground(Theme.INK);
        area.setBackground(Theme.SURFACE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setTabSize(4);
        area.setBorder(new javax.swing.border.EmptyBorder(10, 14, 12, 14));

        setViewportView(area);
        setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
        setBackground(Theme.SURFACE);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(18);
        getVerticalScrollBar().setBlockIncrement(90);
    }

    void show(String reportText) {
        area.setText(reportText);
        area.setFont(Theme.MONO);
        area.setCaretPosition(0);
    }
}
