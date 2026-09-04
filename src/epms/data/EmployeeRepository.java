package epms.data;

import epms.algorithms.SearchResult;
import epms.algorithms.Searcher;
import epms.algorithms.SortAlgorithm;
import epms.algorithms.SortResult;
import epms.algorithms.Sorter;
import epms.model.Employee;
import epms.model.EmployeeField;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * In-memory store of {@link Employee} records and the single place where the
 * add / delete / search / sort operations live. Both the text and GUI
 * front-ends drive the application exclusively through this class, which keeps
 * their behaviour identical.
 */
public class EmployeeRepository {

    private final List<Employee> employees = new ArrayList<>();
    private Path backingFile;

    // ------------------------------------------------------------------
    // Basic accessors
    // ------------------------------------------------------------------

    /** Live-ordered snapshot of every employee (a defensive copy). */
    public List<Employee> findAll() {
        return new ArrayList<>(employees);
    }

    public int size() {
        return employees.size();
    }

    public boolean isEmpty() {
        return employees.isEmpty();
    }

    public Optional<Path> getBackingFile() {
        return Optional.ofNullable(backingFile);
    }

    // ------------------------------------------------------------------
    // Add / update / delete
    // ------------------------------------------------------------------

    /**
     * Adds {@code employee}.
     *
     * @throws DuplicateEmployeeException if the id is already used
     */
    public void add(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("employee must not be null");
        }
        if (existsById(employee.getId())) {
            throw new DuplicateEmployeeException(employee.getId());
        }
        employees.add(employee);
    }

    /** Next unused id, handy for the "add" forms. */
    public int nextId() {
        int max = 0;
        for (Employee e : employees) {
            max = Math.max(max, e.getId());
        }
        return max + 1;
    }

    /**
     * Replaces the record whose id matches {@code updated}.
     *
     * @throws EmployeeNotFoundException if no such id exists
     */
    public void update(Employee updated) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId() == updated.getId()) {
                employees.set(i, updated);
                return;
            }
        }
        throw new EmployeeNotFoundException(updated.getId());
    }

    /**
     * Removes the employee with {@code id}.
     *
     * @return the removed record
     * @throws EmployeeNotFoundException if no such id exists
     */
    public Employee deleteById(int id) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId() == id) {
                return employees.remove(i);
            }
        }
        throw new EmployeeNotFoundException(id);
    }

    public void clear() {
        employees.clear();
    }

    public boolean existsById(int id) {
        return findById(id).isPresent();
    }

    public Optional<Employee> findById(int id) {
        for (Employee e : employees) {
            if (e.getId() == id) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------
    // Sorting - delegates to the hand-written Sorter
    // ------------------------------------------------------------------

    /**
     * Sorts the in-memory list by {@code field} using {@code algorithm} and
     * keeps the new order. Returns the run's metrics for display.
     */
    public SortResult<Employee> sortBy(EmployeeField field, boolean ascending,
                                       SortAlgorithm algorithm) {
        Comparator<Employee> cmp = field.comparator(ascending);
        SortResult<Employee> result = Sorter.sort(employees, cmp, algorithm);
        employees.clear();
        employees.addAll(result.getSorted());
        return result;
    }

    // ------------------------------------------------------------------
    // Searching
    // ------------------------------------------------------------------

    /**
     * Linear search for an exact id over the current (any) ordering.
     * Returns the matching employee plus the comparison count.
     */
    public SearchOutcome searchById(int id) {
        Employee probe = new Employee(id, "?", "?", 0, 0, 0);
        Comparator<Employee> byId = Comparator.comparingInt(Employee::getId);
        SearchResult r = Searcher.linearSearch(employees, probe, byId);
        return toOutcome(r, "linear");
    }

    /**
     * Binary search for an exact id. Sorts a copy by id first (that cost is
     * <em>not</em> folded into the reported comparison count, matching how the
     * discussion frames "search on already-sorted data").
     */
    public SearchOutcome binarySearchById(int id) {
        List<Employee> byId = Sorter.sort(employees,
                Comparator.comparingInt(Employee::getId), SortAlgorithm.MERGE).getSorted();
        Employee probe = new Employee(id, "?", "?", 0, 0, 0);
        SearchResult r = Searcher.binarySearch(byId, probe,
                Comparator.comparingInt(Employee::getId));
        // Translate the index-in-sorted-copy into the actual employee.
        Employee match = r.isFound() ? byId.get(r.getIndex()) : null;
        return new SearchOutcome(Optional.ofNullable(match), r.getComparisons(), "binary");
    }

    /** Case-insensitive substring match on the name. */
    public List<Employee> queryByName(String fragment) {
        String needle = fragment == null ? "" : fragment.trim().toLowerCase();
        List<Employee> hits = new ArrayList<>();
        for (Employee e : employees) {
            if (e.getName().toLowerCase().contains(needle)) {
                hits.add(e);
            }
        }
        return hits;
    }

    /** Exact (case-insensitive) department match. */
    public List<Employee> queryByDepartment(String department) {
        String target = department == null ? "" : department.trim().toLowerCase();
        List<Employee> hits = new ArrayList<>();
        for (Employee e : employees) {
            if (e.getDepartment().toLowerCase().equals(target)) {
                hits.add(e);
            }
        }
        return hits;
    }

    /** Employees whose performance score falls within [min, max] inclusive. */
    public List<Employee> queryByScoreRange(double min, double max) {
        List<Employee> hits = new ArrayList<>();
        for (Employee e : employees) {
            if (e.getPerformanceScore() >= min && e.getPerformanceScore() <= max) {
                hits.add(e);
            }
        }
        return hits;
    }

    private SearchOutcome toOutcome(SearchResult r, String method) {
        Employee match = r.isFound() ? employees.get(r.getIndex()) : null;
        return new SearchOutcome(Optional.ofNullable(match), r.getComparisons(), method);
    }

    // ------------------------------------------------------------------
    // File operations
    // ------------------------------------------------------------------

    /**
     * Loads the roster from {@code path}, replacing the current contents, and
     * remembers the path for later {@link #save()} calls.
     */
    public FileStorage.LoadReport loadFrom(Path path) throws IOException {
        FileStorage.LoadReport report = FileStorage.load(path);
        employees.clear();
        for (Employee e : report.getEmployees()) {
            if (!existsById(e.getId())) {
                employees.add(e);
            }
        }
        this.backingFile = path;
        return report;
    }

    /** Saves to an explicit path and remembers it. */
    public void saveTo(Path path) throws IOException {
        FileStorage.save(path, employees);
        this.backingFile = path;
    }

    /**
     * Saves to the remembered backing file.
     *
     * @throws IllegalStateException if no file has been chosen yet
     */
    public void save() throws IOException {
        if (backingFile == null) {
            throw new IllegalStateException("No file chosen yet - use \"Save As\" first");
        }
        FileStorage.save(backingFile, employees);
    }

    /** A search hit (or miss) together with how much work it took. */
    public static final class SearchOutcome {
        private final Optional<Employee> match;
        private final long comparisons;
        private final String method;

        SearchOutcome(Optional<Employee> match, long comparisons, String method) {
            this.match = match;
            this.comparisons = comparisons;
            this.method = method;
        }

        public Optional<Employee> getMatch() {
            return match;
        }

        public boolean isFound() {
            return match.isPresent();
        }

        public long getComparisons() {
            return comparisons;
        }

        public String getMethod() {
            return method;
        }
    }
}
