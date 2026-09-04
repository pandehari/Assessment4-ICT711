package epms.algorithms;

/**
 * Instrumentation captured while a sort runs. Exposed so the UI and the
 * performance tests can talk about algorithm behaviour concretely
 * (comparison counts, moves, wall-clock time).
 */
public class SortMetrics {

    private final SortAlgorithm algorithm;
    private long comparisons;
    private long moves;
    private long elapsedNanos;

    public SortMetrics(SortAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    void countComparison() {
        comparisons++;
    }

    void countMove() {
        moves++;
    }

    void countMoves(long n) {
        moves += n;
    }

    void setElapsedNanos(long elapsedNanos) {
        this.elapsedNanos = elapsedNanos;
    }

    public SortAlgorithm getAlgorithm() {
        return algorithm;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getMoves() {
        return moves;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public double getElapsedMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    @Override
    public String toString() {
        return String.format("%s: %,d comparisons, %,d moves, %.3f ms",
                algorithm.getDisplayName(), comparisons, moves, getElapsedMillis());
    }
}
