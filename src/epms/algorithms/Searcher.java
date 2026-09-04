package epms.algorithms;

import java.util.Comparator;
import java.util.List;

/**
 * Hand-written linear and binary search over a {@link List}.
 *
 * <p>Both routines report their comparison count through {@link SearchResult} so
 * the UI can show, and the tests can assert, the O(n) versus O(log n) gap.
 */
public final class Searcher {

    private Searcher() {
    }

    /**
     * Scans from the front until an element equals {@code key} under
     * {@code comparator}. Works on unsorted data. O(n) comparisons.
     */
    public static <T> SearchResult linearSearch(List<T> list, T key, Comparator<T> comparator) {
        validate(list, comparator);
        long comparisons = 0;
        for (int i = 0; i < list.size(); i++) {
            comparisons++;
            if (comparator.compare(list.get(i), key) == 0) {
                return new SearchResult(i, comparisons);
            }
        }
        return new SearchResult(SearchResult.NOT_FOUND, comparisons);
    }

    /**
     * Classic halving search. The list <em>must</em> already be sorted in
     * ascending {@code comparator} order; callers are responsible for that
     * precondition. O(log n) comparisons.
     *
     * <p>When duplicates are present, the index returned is an arbitrary
     * matching position (whichever the halving lands on).
     */
    public static <T> SearchResult binarySearch(List<T> sortedList, T key, Comparator<T> comparator) {
        validate(sortedList, comparator);
        long comparisons = 0;
        int lo = 0;
        int hi = sortedList.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            comparisons++;
            int c = comparator.compare(sortedList.get(mid), key);
            if (c == 0) {
                return new SearchResult(mid, comparisons);
            } else if (c < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return new SearchResult(SearchResult.NOT_FOUND, comparisons);
    }

    private static <T> void validate(List<T> list, Comparator<T> comparator) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("comparator must not be null");
        }
    }
}
