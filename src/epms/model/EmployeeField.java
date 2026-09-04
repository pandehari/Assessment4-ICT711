package epms.model;

import java.util.Comparator;

/**
 * The employee attributes that can be sorted or queried on, each paired with an
 * ascending {@link Comparator}. Centralising them here keeps the text and GUI
 * front-ends in sync.
 */
public enum EmployeeField {

    ID("ID", Comparator.comparingInt(Employee::getId)),
    NAME("Name", Comparator.comparing(e -> e.getName().toLowerCase())),
    DEPARTMENT("Department", Comparator.comparing((Employee e) -> e.getDepartment().toLowerCase())
            .thenComparing(Employee::getName)),
    PERFORMANCE_SCORE("Performance Score", Comparator.comparingDouble(Employee::getPerformanceScore)),
    YEARS_OF_SERVICE("Years of Service", Comparator.comparingInt(Employee::getYearsOfService)),
    SALARY("Salary", Comparator.comparingDouble(Employee::getSalary));

    private final String displayName;
    private final Comparator<Employee> ascending;

    EmployeeField(String displayName, Comparator<Employee> ascending) {
        this.displayName = displayName;
        this.ascending = ascending;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Comparator<Employee> ascending() {
        return ascending;
    }

    public Comparator<Employee> descending() {
        return ascending.reversed();
    }

    public Comparator<Employee> comparator(boolean ascending) {
        return ascending ? ascending() : descending();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
