package epms;

import epms.algorithms.SearchResult;
import epms.algorithms.Searcher;
import epms.algorithms.SortAlgorithm;
import epms.algorithms.SortMetrics;
import epms.algorithms.Sorter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Empirical checks that the algorithms scale the way their Big-O says they
 * should. These double as the data source for the growth-rate table in
 * {@code DISCUSSION.md}; run {@code ./bench.sh} to print the full numbers.
 */
class AlgorithmPerformanceTest {

    private static final Comparator<Integer> NATURAL = Comparator.naturalOrder();

    @Test
    void quadraticSortsGrowRoughlyFourfoldWhenInputDoubles() {
        long small = comparisons(SortAlgorithm.INSERTION, randomList(1_000, 1));
        long large = comparisons(SortAlgorithm.INSERTION, randomList(2_000, 1));
        double ratio = (double) large / small;
        assertTrue(ratio > 3.0 && ratio < 5.5,
                "insertion sort comparisons should ~4x, ratio was " + ratio);
    }

    @Test
    void nLogNSortGrowsFarSlowerThanQuadratic() {
        long merge2k = comparisons(SortAlgorithm.MERGE, randomList(2_000, 7));
        long bubble2k = comparisons(SortAlgorithm.BUBBLE, randomList(2_000, 7));
        assertTrue(merge2k * 10 < bubble2k,
                "merge sort should be an order of magnitude cheaper than bubble at n=2000 "
                        + "(merge=" + merge2k + ", bubble=" + bubble2k + ")");
    }

    @Test
    void binarySearchComparisonsGrowByOnePerInputDoubling() {
        long at1k = Searcher.binarySearch(range(1_000), -1, NATURAL).getComparisons();
        long at2k = Searcher.binarySearch(range(2_000), -1, NATURAL).getComparisons();
        long at4k = Searcher.binarySearch(range(4_000), -1, NATURAL).getComparisons();
        assertTrue(at2k - at1k <= 1 && at4k - at2k <= 1,
                "binary search depth should rise by ~1 per doubling: "
                        + at1k + ", " + at2k + ", " + at4k);
    }

    @Test
    void printGrowthTable() {
        System.out.println();
        System.out.printf("%-14s %10s %10s %10s %10s%n",
                "algorithm", "n=1000", "n=2000", "n=4000", "n=8000");
        for (SortAlgorithm algo : SortAlgorithm.values()) {
            System.out.printf("%-14s %10d %10d %10d %10d%n", algo.name(),
                    comparisons(algo, randomList(1_000, 99)),
                    comparisons(algo, randomList(2_000, 99)),
                    comparisons(algo, randomList(4_000, 99)),
                    comparisons(algo, randomList(8_000, 99)));
        }
        SearchResult lin = Searcher.linearSearch(range(8_000), -1, NATURAL);
        SearchResult bin = Searcher.binarySearch(range(8_000), -1, NATURAL);
        System.out.printf("%-14s %31s linear=%d binary=%d%n",
                "search@8000", "", lin.getComparisons(), bin.getComparisons());
    }

    private static long comparisons(SortAlgorithm algo, List<Integer> data) {
        SortMetrics m = Sorter.sort(data, NATURAL, algo).getMetrics();
        return m.getComparisons();
    }

    private static List<Integer> randomList(int size, long seed) {
        Random rng = new Random(seed);
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(rng.nextInt());
        return list;
    }

    private static List<Integer> range(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i * 2);
        return list;
    }
}
