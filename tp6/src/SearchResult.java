import java.util.Collections;
import java.util.List;

/**
 * Immutable result produced by any search algorithm.
 */
public class SearchResult {
    public final List<String> path;
    public final int          totalCost;
    public final int          nodesExplored;
    public final long         timeMs;
    public final List<String> explorationOrder;

    /** Convenience constructor without exploration order. */
    public SearchResult(List<String> path, int totalCost, int nodesExplored, long timeMs) {
        this(path, totalCost, nodesExplored, timeMs, Collections.emptyList());
    }

    public SearchResult(List<String> path, int totalCost,
                        int nodesExplored, long timeMs,
                        List<String> explorationOrder) {
        this.path             = Collections.unmodifiableList(path);
        this.totalCost        = totalCost;
        this.nodesExplored    = nodesExplored;
        this.timeMs           = timeMs;
        this.explorationOrder = Collections.unmodifiableList(explorationOrder);
    }

    @Override
    public String toString() {
        return String.join(" → ", path) + " | " + totalCost + " km | " + nodesExplored + " nodes";
    }
}