package epms.data;

/** Thrown when an operation references an employee id that is not on file. */
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(int id) {
        super("No employee found with id " + id);
    }
}
