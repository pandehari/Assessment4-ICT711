package epms.algorithms;

/**
 * Outcome of a {@link Searcher} call: the index of the match (or {@code -1})
 * and the number of comparisons the search needed to get there.
 */
public class SearchResult {

    public static final int NOT_FOUND = -1;

    private final int index;
    private final long comparisons;

    SearchResult(int index, long comparisons) {
        this.index = index;
        this.comparisons = comparisons;
    }

    public int getIndex() {
        return index;
    }

    public boolean isFound() {
        return index != NOT_FOUND;
    }

    public long getComparisons() {
        return comparisons;
    }

    @Override
    public String toString() {
        return isFound()
                ? String.format("found at index %d after %d comparison(s)", index, comparisons)
                : String.format("not found after %d comparison(s)", comparisons);
    }
}
