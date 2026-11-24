package minios.gui;

import java.awt.Font;

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
        area.setBackground(Theme.CARD);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setTabSize(4);
        area.setBorder(new javax.swing.border.EmptyBorder(4, 12, 8, 12));

        setViewportView(area);
        setBorder(null);
        setBackground(Theme.CARD);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(16);
    }

    void show(String reportText) {
        area.setText(reportText);
        area.setFont(Theme.MONO);
        area.setCaretPosition(0);
    }

    // Used by the self test and the screenshots to read back what was shown.
    String text() {
        return area.getText();
    }

    void setScale(int points) {
        area.setFont(new Font(Theme.MONO.getFamily(), Font.PLAIN, points));
    }
}
