package epms.model;

import java.util.Objects;

/**
 * A single employee record tracked by the Employee Performance Management System.
 *
 * <p>Instances are mutable value objects. Equality and hash code are based on the
 * unique {@link #getId() id} only, so an {@code Employee} can be located in a
 * collection even after its other fields have been edited.
 */
public class Employee {

    private int id;
    private String name;
    private String department;
    /** Latest appraisal score on a 0-100 scale. */
    private double performanceScore;
    private int yearsOfService;
    private double salary;

    public Employee(int id, String name, String department,
                    double performanceScore, int yearsOfService, double salary) {
        this.id = id;
        setName(name);
        setDepartment(department);
        setPerformanceScore(performanceScore);
        setYearsOfService(yearsOfService);
        setSalary(salary);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        this.name = name.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department must not be blank");
        }
        this.department = department.trim();
    }

    public double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(double performanceScore) {
        if (performanceScore < 0 || performanceScore > 100) {
            throw new IllegalArgumentException("Performance score must be between 0 and 100");
        }
        this.performanceScore = performanceScore;
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    public void setYearsOfService(int yearsOfService) {
        if (yearsOfService < 0) {
            throw new IllegalArgumentException("Years of service must not be negative");
        }
        this.yearsOfService = yearsOfService;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Salary must not be negative");
        }
        this.salary = salary;
    }

    /**
     * Maps the raw performance score onto a five-band rating that is friendlier
     * to read in the UI.
     */
    public String getRatingBand() {
        if (performanceScore >= 90) return "Outstanding";
        if (performanceScore >= 75) return "Exceeds";
        if (performanceScore >= 60) return "Meets";
        if (performanceScore >= 40) return "Needs Improvement";
        return "Unsatisfactory";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        return id == ((Employee) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Employee{id=%d, name='%s', department='%s', score=%.1f, years=%d, salary=%.2f}",
                id, name, department, performanceScore, yearsOfService, salary);
    }
}
