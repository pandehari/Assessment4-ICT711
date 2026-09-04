package epms.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Hand-written implementations of five classic comparison sorts.
 *
 * <p>Every algorithm is generic ({@code <T>} + {@link Comparator}), never mutates
 * the caller's list, and routes all element comparisons and moves through a
 * {@link SortMetrics} instance so their cost can be measured and discussed.
 *
 * <p>See {@code DISCUSSION.md} for the complexity analysis and suggested
 * improvements.
 */
public final class Sorter {

    private Sorter() {
    }

    /**
     * Sorts a copy of {@code input} into ascending {@code comparator} order using
     * the requested {@code algorithm}.
     *
     * @return the sorted copy together with the metrics gathered during the run
     */
    public static <T> SortResult<T> sort(List<T> input, Comparator<T> comparator,
                                         SortAlgorithm algorithm) {
        if (input == null) {
            throw new IllegalArgumentException("input list must not be null");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("comparator must not be null");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm must not be null");
        }

        @SuppressWarnings("unchecked")
        T[] data = (T[]) input.toArray();
        SortMetrics metrics = new SortMetrics(algorithm);

        long start = System.nanoTime();
        switch (algorithm) {
            case BUBBLE:
                bubbleSort(data, comparator, metrics);
                break;
            case INSERTION:
                insertionSort(data, comparator, metrics);
                break;
            case SELECTION:
                selectionSort(data, comparator, metrics);
                break;
            case QUICK:
                quickSort(data, 0, data.length - 1, comparator, metrics);
                break;
            case MERGE:
                @SuppressWarnings("unchecked")
                T[] buffer = (T[]) new Object[data.length];
                mergeSort(data, buffer, 0, data.length - 1, comparator, metrics);
                break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        metrics.setElapsedNanos(System.nanoTime() - start);

        List<T> sorted = new ArrayList<>(data.length);
        for (T item : data) {
            sorted.add(item);
        }
        return new SortResult<>(sorted, metrics);
    }

    // ------------------------------------------------------------------
    // Bubble sort - repeatedly swaps adjacent out-of-order pairs.
    // Early-exit flag makes a sorted input cost O(n).
    // ------------------------------------------------------------------
    private static <T> void bubbleSort(T[] a, Comparator<T> cmp, SortMetrics m) {
        for (int end = a.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int i = 0; i < end; i++) {
                m.countComparison();
                if (cmp.compare(a[i], a[i + 1]) > 0) {
                    swap(a, i, i + 1, m);
                    swapped = true;
                }
            }
            if (!swapped) {
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Insertion sort - grows a sorted prefix, shifting larger elements right
    // to open a slot for the current key. Fast on nearly-sorted data.
    // ------------------------------------------------------------------
    private static <T> void insertionSort(T[] a, Comparator<T> cmp, SortMetrics m) {
        for (int i = 1; i < a.length; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= 0) {
                m.countComparison();
                if (cmp.compare(a[j], key) > 0) {
                    a[j + 1] = a[j];
                    m.countMove();
                    j--;
                } else {
                    break;
                }
            }
            a[j + 1] = key;
            m.countMove();
        }
    }

    // ------------------------------------------------------------------
    // Selection sort - each pass picks the minimum of the unsorted tail and
    // swaps it into place. Always O(n^2) comparisons, at most n-1 swaps.
    // ------------------------------------------------------------------
    private static <T> void selectionSort(T[] a, Comparator<T> cmp, SortMetrics m) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                m.countComparison();
                if (cmp.compare(a[j], a[min]) < 0) {
                    min = j;
                }
            }
            if (min != i) {
                swap(a, i, min, m);
            }
        }
    }

    // ------------------------------------------------------------------
    // Quick sort - Lomuto partition around the last element, recursing into
    // the smaller side first to bound stack depth at O(log n) on average.
    // ------------------------------------------------------------------
    private static <T> void quickSort(T[] a, int lo, int hi,
                                      Comparator<T> cmp, SortMetrics m) {
        while (lo < hi) {
            int p = partition(a, lo, hi, cmp, m);
            if (p - lo < hi - p) {
                quickSort(a, lo, p - 1, cmp, m);
                lo = p + 1;
            } else {
                quickSort(a, p + 1, hi, cmp, m);
                hi = p - 1;
            }
        }
    }

    private static <T> int partition(T[] a, int lo, int hi,
                                     Comparator<T> cmp, SortMetrics m) {
        // Median-of-three pivot selection guards against the sorted-input
        // worst case; the chosen pivot is parked at hi.
        int mid = lo + (hi - lo) / 2;
        m.countComparison();
        if (cmp.compare(a[mid], a[lo]) < 0) swap(a, lo, mid, m);
        m.countComparison();
        if (cmp.compare(a[hi], a[lo]) < 0) swap(a, lo, hi, m);
        m.countComparison();
        if (cmp.compare(a[hi], a[mid]) < 0) swap(a, mid, hi, m);
        swap(a, mid, hi, m);

        T pivot = a[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            m.countComparison();
            if (cmp.compare(a[j], pivot) <= 0) {
                i++;
                swap(a, i, j, m);
            }
        }
        swap(a, i + 1, hi, m);
        return i + 1;
    }

    // ------------------------------------------------------------------
    // Merge sort - top-down split, then a stable merge through a shared
    // buffer. Guaranteed O(n log n) at the cost of O(n) extra space.
    // ------------------------------------------------------------------
    private static <T> void mergeSort(T[] a, T[] buf, int lo, int hi,
                                      Comparator<T> cmp, SortMetrics m) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(a, buf, lo, mid, cmp, m);
        mergeSort(a, buf, mid + 1, hi, cmp, m);
        merge(a, buf, lo, mid, hi, cmp, m);
    }

    private static <T> void merge(T[] a, T[] buf, int lo, int mid, int hi,
                                  Comparator<T> cmp, SortMetrics m) {
        for (int k = lo; k <= hi; k++) {
            buf[k] = a[k];
        }
        m.countMoves(hi - lo + 1);

        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = buf[j++];
                m.countMove();
            } else if (j > hi) {
                a[k] = buf[i++];
                m.countMove();
            } else {
                m.countComparison();
                if (cmp.compare(buf[j], buf[i]) < 0) {
                    a[k] = buf[j++];
                    m.countMove();
                } else {
                    a[k] = buf[i++];
                    m.countMove();
                }
            }
        }
    }

    private static <T> void swap(T[] a, int i, int j, SortMetrics m) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        m.countMoves(3);
    }
}
