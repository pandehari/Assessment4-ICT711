package epms.data;

/** Thrown when an employee is added with an id that is already in use. */
public class DuplicateEmployeeException extends RuntimeException {
    public DuplicateEmployeeException(int id) {
        super("An employee with id " + id + " already exists");
    }
}
