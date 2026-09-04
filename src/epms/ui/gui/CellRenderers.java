package epms.ui.gui;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Small table cell renderers used by {@link MainWindow}: right-aligned number
 * formatting and a colour-coded performance-rating column. Keeping them here
 * keeps the main window focused on behaviour rather than presentation details.
 */
final class CellRenderers {

    private CellRenderers() {
    }

    /** Right-aligned renderer that formats numeric values with {@code format}. */
    static DefaultTableCellRenderer number(String format) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number
                        ? String.format(format, ((Number) value).doubleValue())
                        : "");
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    /** Tints the rating cell green→red across the five performance bands. */
    static DefaultTableCellRenderer rating() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column);
                if (!selected) {
                    c.setBackground(bandColour(String.valueOf(value)));
                    c.setForeground(Color.DARK_GRAY);
                }
                return c;
            }
        };
    }

    private static Color bandColour(String band) {
        switch (band) {
            case "Outstanding":       return new Color(0xC8E6C9);
            case "Exceeds":           return new Color(0xDCEDC8);
            case "Meets":             return new Color(0xFFF9C4);
            case "Needs Improvement": return new Color(0xFFE0B2);
            case "Unsatisfactory":    return new Color(0xFFCDD2);
            default:                  return Color.WHITE;
        }
    }
}
