package epms.ui.text;

import epms.algorithms.SortAlgorithm;
import epms.algorithms.SortResult;
import epms.data.DuplicateEmployeeException;
import epms.data.EmployeeNotFoundException;
import epms.data.EmployeeRepository;
import epms.data.FileStorage;
import epms.model.Employee;
import epms.model.EmployeeField;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * The text-based front-end (TBI). A simple numbered-menu REPL that exposes every
 * operation the GUI does, driven entirely through {@link EmployeeRepository}.
 */
public class TextInterface {

    private final EmployeeRepository repo;
    private final Scanner in;
    private final PrintStream out;
    private boolean running = true;

    public TextInterface(EmployeeRepository repo, Scanner in, PrintStream out) {
        this.repo = repo;
        this.in = in;
        this.out = out;
    }

    public void run() {
        out.println();
        out.println("=== Employee Performance Management System - Text Interface ===");
        repo.getBackingFile().ifPresent(p -> out.println("Data file: " + p.toAbsolutePath()));
        while (running) {
            printMenu();
            String choice = prompt("Select an option");
            out.println();
            try {
                dispatch(choice);
            } catch (DuplicateEmployeeException | EmployeeNotFoundException
                     | IllegalArgumentException | IllegalStateException ex) {
                out.println("  ! " + ex.getMessage());
            } catch (IOException ex) {
                out.println("  ! File error: " + ex.getMessage());
            }
            out.println();
        }
        out.println("Goodbye.");
    }

    private void printMenu() {
        out.println("-----------------------------------------------");
        out.println(" 1) List / sort employees");
        out.println(" 2) Add employee");
        out.println(" 3) Edit employee");
        out.println(" 4) Delete employee");
        out.println(" 5) Search / query");
        out.println(" 6) Save to file");
        out.println(" 7) Load from file");
        out.println(" 0) Exit");
        out.println("-----------------------------------------------");
    }

    private void dispatch(String choice) throws IOException {
        switch (choice.trim()) {
            case "1": listAndSort(); break;
            case "2": addEmployee(); break;
            case "3": editEmployee(); break;
            case "4": deleteEmployee(); break;
            case "5": searchMenu(); break;
            case "6": saveToFile(); break;
            case "7": loadFromFile(); break;
            case "0": running = false; break;
            case "": break;
            default: out.println("  Unknown option: " + choice);
        }
    }

    // ------------------------------------------------------------------
    // 1 - list & sort
    // ------------------------------------------------------------------
    private void listAndSort() {
        if (repo.isEmpty()) {
            out.println("  (no employees on file)");
            return;
        }
        out.println("Sort by which field?");
        EmployeeField field = chooseEnum(EmployeeField.values(), EmployeeField.ID);
        boolean ascending = !prompt("Descending? (y/N)").equalsIgnoreCase("y");
        out.println("Which algorithm?");
        SortAlgorithm algo = chooseEnum(SortAlgorithm.values(), SortAlgorithm.QUICK);

        SortResult<Employee> result = repo.sortBy(field, ascending, algo);
        printTable(repo.findAll());
        out.println();
        out.println("  " + result.getMetrics());
        out.println("  " + algo.getDisplayName() + " - average " + algo.getAverageComplexity()
                + ", space " + algo.getSpaceComplexity()
                + (algo.isStable() ? ", stable" : ", not stable"));
    }

    // ------------------------------------------------------------------
    // 2 - add
    // ------------------------------------------------------------------
    private void addEmployee() {
        int id = readInt("Employee id", repo.nextId());
        String name = prompt("Name");
        String dept = prompt("Department");
        double score = readDouble("Performance score (0-100)", 0);
        int years = readInt("Years of service", 0);
        double salary = readDouble("Salary", 0);

        repo.add(new Employee(id, name, dept, score, years, salary));
        out.println("  + Added employee " + id + " (" + name + ")");
    }

    // ------------------------------------------------------------------
    // 3 - edit
    // ------------------------------------------------------------------
    private void editEmployee() {
        int id = readInt("Employee id to edit", 0);
        Employee current = repo.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        out.println("  Current: " + current);
        out.println("  Press Enter to keep the current value.");

        String name = promptDefault("Name", current.getName());
        String dept = promptDefault("Department", current.getDepartment());
        double score = readDouble("Performance score (0-100)", current.getPerformanceScore());
        int years = readInt("Years of service", current.getYearsOfService());
        double salary = readDouble("Salary", current.getSalary());

        repo.update(new Employee(id, name, dept, score, years, salary));
        out.println("  ~ Updated employee " + id);
    }

    // ------------------------------------------------------------------
    // 4 - delete
    // ------------------------------------------------------------------
    private void deleteEmployee() {
        int id = readInt("Employee id to delete", 0);
        Employee removed = repo.deleteById(id);
        out.println("  - Removed " + removed.getName());
    }

    // ------------------------------------------------------------------
    // 5 - search / query
    // ------------------------------------------------------------------
    private void searchMenu() {
        out.println(" a) Find by id (linear search)");
        out.println(" b) Find by id (binary search)");
        out.println(" c) Query by name (contains)");
        out.println(" d) Query by department");
        out.println(" e) Query by score range");
        switch (prompt("Choose").trim().toLowerCase()) {
            case "a": {
                int id = readInt("Employee id", 0);
                EmployeeRepository.SearchOutcome o = repo.searchById(id);
                reportOutcome(o);
                break;
            }
            case "b": {
                int id = readInt("Employee id", 0);
                EmployeeRepository.SearchOutcome o = repo.binarySearchById(id);
                reportOutcome(o);
                break;
            }
            case "c": {
                List<Employee> hits = repo.queryByName(prompt("Name contains"));
                printResults(hits);
                break;
            }
            case "d": {
                List<Employee> hits = repo.queryByDepartment(prompt("Department"));
                printResults(hits);
                break;
            }
            case "e": {
                double min = readDouble("Minimum score", 0);
                double max = readDouble("Maximum score", 100);
                printResults(repo.queryByScoreRange(min, max));
                break;
            }
            default:
                out.println("  Nothing selected.");
        }
    }

    private void reportOutcome(EmployeeRepository.SearchOutcome o) {
        if (o.isFound()) {
            out.println("  Found (" + o.getMethod() + ", " + o.getComparisons()
                    + " comparisons): " + o.getMatch().get());
        } else {
            out.println("  Not found (" + o.getMethod() + ", " + o.getComparisons()
                    + " comparisons).");
        }
    }

    private void printResults(List<Employee> hits) {
        if (hits.isEmpty()) {
            out.println("  No matches.");
        } else {
            printTable(hits);
            out.println("  " + hits.size() + " match(es).");
        }
    }

    // ------------------------------------------------------------------
    // 6 / 7 - file operations
    // ------------------------------------------------------------------
    private void saveToFile() throws IOException {
        String def = repo.getBackingFile().map(Path::toString).orElse("data/employees.csv");
        Path path = Path.of(promptDefault("Save to path", def));
        repo.saveTo(path);
        out.println("  Saved " + repo.size() + " employees to " + path.toAbsolutePath());
    }

    private void loadFromFile() throws IOException {
        String def = repo.getBackingFile().map(Path::toString).orElse("data/employees.csv");
        Path path = Path.of(promptDefault("Load from path", def));
        FileStorage.LoadReport report = repo.loadFrom(path);
        out.println("  Loaded " + report.getEmployees().size() + " employees from "
                + path.toAbsolutePath());
        if (report.hasWarnings()) {
            out.println("  " + report.getWarnings().size() + " row(s) skipped:");
            for (String w : report.getWarnings()) {
                out.println("    - " + w);
            }
        }
    }

    // ------------------------------------------------------------------
    // Rendering helpers
    // ------------------------------------------------------------------
    private void printTable(List<Employee> list) {
        out.printf("  %-4s  %-22s  %-16s  %7s  %-17s  %5s  %12s%n",
                "ID", "Name", "Department", "Score", "Rating", "Years", "Salary");
        out.println("  " + "-".repeat(94));
        for (Employee e : list) {
            out.printf("  %-4d  %-22s  %-16s  %7.1f  %-17s  %5d  %,12.2f%n",
                    e.getId(), truncate(e.getName(), 22), truncate(e.getDepartment(), 16),
                    e.getPerformanceScore(), e.getRatingBand(), e.getYearsOfService(),
                    e.getSalary());
        }
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ------------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------------
    private String prompt(String label) {
        out.print(label + ": ");
        out.flush();
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    private String promptDefault(String label, String def) {
        out.print(label + " [" + def + "]: ");
        out.flush();
        String line = in.hasNextLine() ? in.nextLine().trim() : "";
        return line.isEmpty() ? def : line;
    }

    private int readInt(String label, int def) {
        while (true) {
            String s = promptDefault(label, String.valueOf(def));
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ex) {
                out.println("  Please enter a whole number.");
            }
        }
    }

    private double readDouble(String label, double def) {
        while (true) {
            String s = promptDefault(label, String.valueOf(def));
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException ex) {
                out.println("  Please enter a number.");
            }
        }
    }

    private <E extends Enum<E>> E chooseEnum(E[] values, E def) {
        for (int i = 0; i < values.length; i++) {
            out.printf("   %d) %s%n", i + 1, values[i]);
        }
        while (true) {
            String s = promptDefault("Choice", String.valueOf(indexOf(values, def) + 1));
            try {
                int idx = Integer.parseInt(s.trim()) - 1;
                if (idx >= 0 && idx < values.length) {
                    return values[idx];
                }
            } catch (NumberFormatException ignored) {
                // fall through
            }
            out.println("  Enter a number between 1 and " + values.length + ".");
        }
    }

    private static <E> int indexOf(E[] values, E target) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == target) {
                return i;
            }
        }
        return 0;
    }
}
