package epms;

import epms.data.EmployeeRepository;
import epms.data.FileStorage;
import epms.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @Test
    void roundTripsEmployeesThroughCsv(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("out.csv");
        List<Employee> original = List.of(
                new Employee(1, "Alice Thapa", "Engineering", 88.5, 6, 92000),
                new Employee(2, "Bob, Jr.", "R&D \"Labs\"", 70, 3, 61000.50));

        FileStorage.save(file, original);
        FileStorage.LoadReport report = FileStorage.load(file);

        assertFalse(report.hasWarnings());
        assertEquals(2, report.getEmployees().size());
        Employee bob = report.getEmployees().get(1);
        assertEquals("Bob, Jr.", bob.getName());          // comma survived quoting
        assertEquals("R&D \"Labs\"", bob.getDepartment()); // embedded quotes survived
        assertEquals(61000.50, bob.getSalary());
    }

    @Test
    void writesHeaderRow(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("h.csv");
        FileStorage.save(file, List.of());
        assertEquals(FileStorage.HEADER, Files.readAllLines(file).get(0));
    }

    @Test
    void malformedRowsAreCollectedAsWarningsNotExceptions(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("messy.csv");
        Files.write(file, List.of(
                FileStorage.HEADER,
                "1,Valid Vera,Sales,80,4,70000",
                "2,Missing Fields,Sales",
                "abc,Bad Id,Sales,50,2,40000",
                "4,Out Of Range,Sales,150,2,40000",
                "",
                "5,Valid Victor,Support,66,1,45000"));

        FileStorage.LoadReport report = FileStorage.load(file);

        assertEquals(2, report.getEmployees().size());
        assertEquals(3, report.getWarnings().size());
        assertTrue(report.getWarnings().get(0).contains("Line 3"));
    }

    @Test
    void repositoryLoadIgnoresDuplicateIdsWithinFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("dupes.csv");
        Files.write(file, List.of(
                FileStorage.HEADER,
                "1,First,Sales,80,4,70000",
                "1,Second,Sales,80,4,70000",
                "2,Third,Sales,80,4,70000"));

        EmployeeRepository repo = new EmployeeRepository();
        repo.loadFrom(file);

        assertEquals(2, repo.size());
        assertEquals("First", repo.findById(1).orElseThrow().getName());
        assertEquals(file, repo.getBackingFile().orElseThrow());
    }

    @Test
    void csvSplitterHandlesQuotedCommasAndDoubledQuotes() {
        List<String> fields = FileStorage.splitCsv("1,\"Doe, Jane\",\"say \"\"hi\"\"\",3");
        assertEquals(List.of("1", "Doe, Jane", "say \"hi\"", "3"), fields);
    }
}
