package epms;

import epms.data.DuplicateEmployeeException;
import epms.data.EmployeeNotFoundException;
import epms.data.EmployeeRepository;
import epms.algorithms.SortAlgorithm;
import epms.model.Employee;
import epms.model.EmployeeField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeRepositoryTest {

    private EmployeeRepository repo;

    @BeforeEach
    void setUp() {
        repo = new EmployeeRepository();
        repo.add(new Employee(3, "Carol", "Sales", 90, 5, 70000));
        repo.add(new Employee(1, "Alice", "Engineering", 60, 2, 65000));
        repo.add(new Employee(2, "Bob", "Sales", 75, 8, 80000));
    }

    @Test
    void addRejectsDuplicateId() {
        assertThrows(DuplicateEmployeeException.class,
                () -> repo.add(new Employee(1, "Clone", "IT", 50, 1, 40000)));
    }

    @Test
    void deleteRemovesAndReturnsRecord() {
        Employee removed = repo.deleteById(2);
        assertEquals("Bob", removed.getName());
        assertEquals(2, repo.size());
        assertFalse(repo.existsById(2));
    }

    @Test
    void deleteUnknownIdThrows() {
        assertThrows(EmployeeNotFoundException.class, () -> repo.deleteById(99));
    }

    @Test
    void updateReplacesExistingRecord() {
        repo.update(new Employee(1, "Alice Smith", "Engineering", 82, 3, 70000));
        Employee updated = repo.findById(1).orElseThrow();
        assertEquals("Alice Smith", updated.getName());
        assertEquals(82, updated.getPerformanceScore());
    }

    @Test
    void nextIdIsOneAboveMax() {
        assertEquals(4, repo.nextId());
    }

    @Test
    void sortByNameOrdersAlphabetically() {
        repo.sortBy(EmployeeField.NAME, true, SortAlgorithm.MERGE);
        assertEquals(List.of("Alice", "Bob", "Carol"),
                repo.findAll().stream().map(Employee::getName).toList());
    }

    @Test
    void sortByScoreDescendingPutsTopPerformerFirst() {
        repo.sortBy(EmployeeField.PERFORMANCE_SCORE, false, SortAlgorithm.QUICK);
        assertEquals("Carol", repo.findAll().get(0).getName());
    }

    @Test
    void everyAlgorithmProducesTheSameOrdering() {
        repo.sortBy(EmployeeField.ID, true, SortAlgorithm.BUBBLE);
        List<Integer> reference = repo.findAll().stream().map(Employee::getId).toList();
        for (SortAlgorithm algo : SortAlgorithm.values()) {
            repo.sortBy(EmployeeField.SALARY, true, algo); // shuffle the order
            repo.sortBy(EmployeeField.ID, true, algo);
            assertEquals(reference, repo.findAll().stream().map(Employee::getId).toList(),
                    algo + " should yield the canonical id order");
        }
    }

    @Test
    void linearSearchByIdFindsEmployee() {
        EmployeeRepository.SearchOutcome outcome = repo.searchById(2);
        assertTrue(outcome.isFound());
        assertEquals("Bob", outcome.getMatch().orElseThrow().getName());
        assertEquals("linear", outcome.getMethod());
    }

    @Test
    void binarySearchByIdFindsEmployeeRegardlessOfCurrentOrder() {
        repo.sortBy(EmployeeField.NAME, false, SortAlgorithm.INSERTION); // not id order
        EmployeeRepository.SearchOutcome outcome = repo.binarySearchById(3);
        assertTrue(outcome.isFound());
        assertEquals("Carol", outcome.getMatch().orElseThrow().getName());
        assertEquals("binary", outcome.getMethod());
    }

    @Test
    void searchByIdReportsMiss() {
        EmployeeRepository.SearchOutcome outcome = repo.searchById(999);
        assertFalse(outcome.isFound());
        assertEquals(3, outcome.getComparisons()); // scanned all three
    }

    @Test
    void queryByNameIsCaseInsensitiveSubstring() {
        List<Employee> hits = repo.queryByName("O"); // Carol, Bob - in stored order
        assertEquals(List.of("Carol", "Bob"),
                hits.stream().map(Employee::getName).toList());
    }

    @Test
    void queryByDepartmentMatchesExactlyButIgnoresCase() {
        assertEquals(2, repo.queryByDepartment("sales").size());
        assertEquals(0, repo.queryByDepartment("Sale").size());
    }

    @Test
    void queryByScoreRangeIsInclusive() {
        List<Employee> hits = repo.queryByScoreRange(60, 75);
        assertEquals(List.of("Alice", "Bob"),
                hits.stream().map(Employee::getName).sorted().toList());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        List<Employee> snapshot = repo.findAll();
        snapshot.clear();
        assertEquals(3, repo.size());
    }
}
