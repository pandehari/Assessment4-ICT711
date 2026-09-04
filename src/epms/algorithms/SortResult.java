package epms.algorithms;

import java.util.Collections;
import java.util.List;

/**
 * The outcome of a {@link Sorter} call: the freshly ordered list plus the
 * {@link SortMetrics} gathered while producing it.
 */
public class SortResult<T> {

    private final List<T> sorted;
    private final SortMetrics metrics;

    SortResult(List<T> sorted, SortMetrics metrics) {
        this.sorted = sorted;
        this.metrics = metrics;
    }

    /** Unmodifiable view of the sorted elements. */
    public List<T> getSorted() {
        return Collections.unmodifiableList(sorted);
    }

    public SortMetrics getMetrics() {
        return metrics;
    }
}
