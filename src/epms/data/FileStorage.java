package epms.data;

import epms.model.Employee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the employee roster as a simple CSV file.
 *
 * <p>Format: one header line followed by
 * {@code id,name,department,performanceScore,yearsOfService,salary} rows. Commas
 * inside the name/department are escaped by doubling any embedded quote and
 * wrapping the field in quotes, matching the common spreadsheet convention.
 */
public final class FileStorage {

    public static final String HEADER =
            "id,name,department,performanceScore,yearsOfService,salary";

    private FileStorage() {
    }

    /** The result of a load: the rows that parsed, plus any that did not. */
    public static final class LoadReport {
        private final List<Employee> employees;
        private final List<String> warnings;

        LoadReport(List<Employee> employees, List<String> warnings) {
            this.employees = employees;
            this.warnings = warnings;
        }

        public List<Employee> getEmployees() {
            return employees;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    /**
     * Loads every well-formed row from {@code path}. Malformed rows are skipped
     * and described in {@link LoadReport#getWarnings()} rather than aborting the
     * whole load.
     *
     * @throws IOException if the file cannot be read
     */
    public static LoadReport load(Path path) throws IOException {
        List<Employee> employees = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (lineNo == 1 && line.toLowerCase().startsWith("id,")) {
                    continue; // header
                }
                try {
                    employees.add(parseRow(line));
                } catch (RuntimeException ex) {
                    warnings.add("Line " + lineNo + ": " + ex.getMessage() + "  [" + line + "]");
                }
            }
        }
        return new LoadReport(employees, warnings);
    }

    /**
     * Writes {@code employees} to {@code path}, overwriting any existing file.
     * The parent directory is created if needed.
     */
    public static void save(Path path, List<Employee> employees) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Employee e : employees) {
                writer.write(formatRow(e));
                writer.newLine();
            }
        }
    }

    static Employee parseRow(String line) {
        List<String> fields = splitCsv(line);
        if (fields.size() != 6) {
            throw new IllegalArgumentException("expected 6 fields, got " + fields.size());
        }
        try {
            int id = Integer.parseInt(fields.get(0).trim());
            String name = fields.get(1);
            String department = fields.get(2);
            double score = Double.parseDouble(fields.get(3).trim());
            int years = Integer.parseInt(fields.get(4).trim());
            double salary = Double.parseDouble(fields.get(5).trim());
            return new Employee(id, name, department, score, years, salary);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("non-numeric value: " + ex.getMessage());
        }
    }

    static String formatRow(Employee e) {
        return String.join(",",
                String.valueOf(e.getId()),
                escape(e.getName()),
                escape(e.getDepartment()),
                trimNumber(e.getPerformanceScore()),
                String.valueOf(e.getYearsOfService()),
                trimNumber(e.getSalary()));
    }

    private static String trimNumber(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private static String escape(String field) {
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /** Minimal RFC-4180-ish splitter: handles quoted fields and doubled quotes. */
    public static List<String> splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        out.add(sb.toString());
        return out;
    }
}
