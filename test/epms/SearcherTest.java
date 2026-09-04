package epms;

import epms.algorithms.SearchResult;
import epms.algorithms.Searcher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearcherTest {

    private static final Comparator<Integer> NATURAL = Comparator.naturalOrder();

    private static List<Integer> range(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i * 2); // even numbers 0..2n-2
        return list;
    }

    @Test
    void linearSearchFindsFirstMatch() {
        List<Integer> data = new ArrayList<>(List.of(4, 8, 15, 16, 23, 42));
        SearchResult r = Searcher.linearSearch(data, 16, NATURAL);
        assertTrue(r.isFound());
        assertEquals(3, r.getIndex());
        assertEquals(4, r.getComparisons());
    }

    @Test
    void linearSearchReportsMissAfterScanningEverything() {
        List<Integer> data = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        SearchResult r = Searcher.linearSearch(data, 99, NATURAL);
        assertFalse(r.isFound());
        assertEquals(SearchResult.NOT_FOUND, r.getIndex());
        assertEquals(5, r.getComparisons());
    }

    @Test
    void binarySearchFindsEveryPresentElement() {
        List<Integer> data = range(1000);
        for (int i = 0; i < data.size(); i++) {
            SearchResult r = Searcher.binarySearch(data, data.get(i), NATURAL);
            assertTrue(r.isFound(), "should find " + data.get(i));
            assertEquals(data.get(i), data.get(r.getIndex()));
        }
    }

    @Test
    void binarySearchMissStaysLogarithmic() {
        List<Integer> data = range(1024); // 1024 elements -> at most 11 probes
        SearchResult r = Searcher.binarySearch(data, 777, NATURAL); // odd -> absent
        assertFalse(r.isFound());
        assertTrue(r.getComparisons() <= 11,
                "expected <= 11 comparisons, got " + r.getComparisons());
    }

    @Test
    void binarySearchBeatsLinearOnLargeInput() {
        List<Integer> data = range(100_000);
        Integer target = data.get(data.size() - 1);
        long linear = Searcher.linearSearch(data, target, NATURAL).getComparisons();
        long binary = Searcher.binarySearch(data, target, NATURAL).getComparisons();
        assertEquals(100_000, linear);
        assertTrue(binary < 20, "binary search should need < 20 comparisons, got " + binary);
    }

    @Test
    void emptyListIsHandled() {
        List<Integer> empty = new ArrayList<>();
        assertFalse(Searcher.linearSearch(empty, 1, NATURAL).isFound());
        assertFalse(Searcher.binarySearch(empty, 1, NATURAL).isFound());
    }
}
