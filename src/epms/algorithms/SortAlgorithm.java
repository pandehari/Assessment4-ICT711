package epms.algorithms;

/**
 * The hand-implemented sorting strategies offered by {@link Sorter}.
 * The {@link #averageComplexity} strings are used by the UI and by
 * {@code DISCUSSION.md} when comparing algorithms.
 */
public enum SortAlgorithm {

    BUBBLE("Bubble Sort", "O(n²)", "O(1)", true),
    INSERTION("Insertion Sort", "O(n²)", "O(1)", true),
    SELECTION("Selection Sort", "O(n²)", "O(1)", false),
    QUICK("Quick Sort", "O(n log n)", "O(log n)", false),
    MERGE("Merge Sort", "O(n log n)", "O(n)", true);

    private final String displayName;
    private final String averageComplexity;
    private final String spaceComplexity;
    private final boolean stable;

    SortAlgorithm(String displayName, String averageComplexity,
                  String spaceComplexity, boolean stable) {
        this.displayName = displayName;
        this.averageComplexity = averageComplexity;
        this.spaceComplexity = spaceComplexity;
        this.stable = stable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAverageComplexity() {
        return averageComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public boolean isStable() {
        return stable;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
