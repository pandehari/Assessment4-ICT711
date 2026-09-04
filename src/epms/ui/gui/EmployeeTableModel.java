package epms.ui.gui;

import epms.model.Employee;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts a list of {@link Employee} objects to a Swing {@link javax.swing.JTable}.
 * Read-only: edits go through the form dialog and the repository, then the model
 * is refreshed via {@link #setEmployees(List)}.
 */
public class EmployeeTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Name", "Department", "Score", "Rating", "Years", "Salary"
    };

    private final List<Employee> rows = new ArrayList<>();

    public void setEmployees(List<Employee> employees) {
        rows.clear();
        rows.addAll(employees);
        fireTableDataChanged();
    }

    public Employee getEmployeeAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 5:
                return Integer.class;
            case 3:
            case 6:
                return Double.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Employee e = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return e.getId();
            case 1: return e.getName();
            case 2: return e.getDepartment();
            case 3: return e.getPerformanceScore();
            case 4: return e.getRatingBand();
            case 5: return e.getYearsOfService();
            case 6: return e.getSalary();
            default: return null;
        }
    }
}
