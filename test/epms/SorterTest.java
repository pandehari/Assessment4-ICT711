package epms;

import epms.algorithms.SortAlgorithm;
import epms.algorithms.SortResult;
import epms.algorithms.Sorter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SorterTest {

    private static final Comparator<Integer> NATURAL = Comparator.naturalOrder();

    @ParameterizedTest
    @EnumSource(SortAlgorithm.class)
    void sortsRandomListAscending(SortAlgorithm algo) {
        List<Integer> input = randomList(500, 12345);
        List<Integer> expected = new ArrayList<>(input);
        Collections.sort(expected);

        SortResult<Integer> result = Sorter.sort(input, NATURAL, algo);

        assertEquals(expected, result.getSorted(), algo + " should match Collections.sort");
    }

    @ParameterizedTest
    @EnumSource(SortAlgorithm.class)
    void handlesEmptyAndSingleton(SortAlgorithm algo) {
        assertEquals(List.of(), Sorter.sort(new ArrayList<Integer>(), NATURAL, algo).getSorted());
        assertEquals(List.of(42), Sorter.sort(new ArrayList<>(List.of(42)), NATURAL, algo).getSorted());
    }

    @ParameterizedTest
    @EnumSource(SortAlgorithm.class)
    void handlesAlreadySortedAndReversed(SortAlgorithm algo) {
        List<Integer> ascending = new ArrayList<>();
        for (int i = 0; i < 200; i++) ascending.add(i);
        List<Integer> descending = new ArrayList<>(ascending);
        Collections.reverse(descending);

        assertEquals(ascending, Sorter.sort(ascending, NATURAL, algo).getSorted());
        assertEquals(ascending, Sorter.sort(descending, NATURAL, algo).getSorted());
    }

    @ParameterizedTest
    @EnumSource(SortAlgorithm.class)
    void handlesDuplicates(SortAlgorithm algo) {
        List<Integer> input = new ArrayList<>(List.of(5, 1, 5, 3, 3, 1, 5, 2, 2, 4));
        List<Integer> expected = new ArrayList<>(input);
        Collections.sort(expected);
        assertEquals(expected, Sorter.sort(input, NATURAL, algo).getSorted());
    }

    @ParameterizedTest
    @EnumSource(SortAlgorithm.class)
    void doesNotMutateCallerList(SortAlgorithm algo) {
        List<Integer> input = new ArrayList<>(List.of(9, 7, 8, 1, 3));
        List<Integer> snapshot = new ArrayList<>(input);
        SortResult<Integer> result = Sorter.sort(input, NATURAL, algo);
        assertEquals(snapshot, input, "input list must be untouched");
        assertNotSame(input, result.getSorted());
    }

    @Test
    void stableAlgorithmsPreserveRelativeOrder() {
        // Sort (label, key) pairs by key only; stable sorts keep insertion order of equal keys.
        record Pair(String label, int key) {}
        List<Pair> input = new ArrayList<>(List.of(
                new Pair("a", 1), new Pair("b", 1), new Pair("c", 0),
                new Pair("d", 1), new Pair("e", 0)));
        Comparator<Pair> byKey = Comparator.comparingInt(Pair::key);

        for (SortAlgorithm algo : SortAlgorithm.values()) {
            if (!algo.isStable()) continue;
            List<Pair> sorted = Sorter.sort(input, byKey, algo).getSorted();
            assertEquals(List.of("c", "e", "a", "b", "d"),
                    sorted.stream().map(Pair::label).toList(),
                    algo + " is documented stable");
        }
    }

    @Test
    void bubbleSortShortCircuitsOnSortedInput() {
        List<Integer> sorted = new ArrayList<>();
        for (int i = 0; i < 1000; i++) sorted.add(i);
        SortResult<Integer> result = Sorter.sort(sorted, NATURAL, SortAlgorithm.BUBBLE);
        // One clean pass: n-1 comparisons, no swaps.
        assertEquals(999, result.getMetrics().getComparisons());
        assertEquals(0, result.getMetrics().getMoves());
    }

    @Test
    void selectionSortComparisonCountIsQuadraticAndInputIndependent() {
        int n = 300;
        long expected = (long) n * (n - 1) / 2;
        assertEquals(expected, Sorter.sort(randomList(n, 1), NATURAL, SortAlgorithm.SELECTION)
                .getMetrics().getComparisons());
        assertEquals(expected, Sorter.sort(randomList(n, 2), NATURAL, SortAlgorithm.SELECTION)
                .getMetrics().getComparisons());
    }

    @Test
    void nullArgumentsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Sorter.sort(null, NATURAL, SortAlgorithm.QUICK));
        assertThrows(IllegalArgumentException.class,
                () -> Sorter.sort(new ArrayList<Integer>(), null, SortAlgorithm.QUICK));
        assertThrows(IllegalArgumentException.class,
                () -> Sorter.sort(new ArrayList<Integer>(), NATURAL, null));
    }

    @Test
    void quickSortStaysCheapOnSortedInput() {
        // Median-of-three keeps the sorted case near n log n, well under n^2 / 2.
        int n = 2000;
        long comparisons = Sorter.sort(sequential(n), NATURAL, SortAlgorithm.QUICK)
                .getMetrics().getComparisons();
        assertTrue(comparisons < (long) n * 20,
                "expected ~n log n comparisons, got " + comparisons);
    }

    private static List<Integer> randomList(int size, long seed) {
        Random rng = new Random(seed);
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(rng.nextInt(1000));
        }
        return list;
    }

    private static List<Integer> sequential(int size) {
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(i);
        return list;
    }
}
