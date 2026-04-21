/**
 * A node in the search tree, tracking the city, accumulated cost, and parent link
 * for path reconstruction.
 */
public class SearchNode implements Comparable<SearchNode> {
    public final String city;
    public final SearchNode parent;
    public final int gCost;

    public SearchNode(String city, SearchNode parent, int accumulatedCost) {
        this.city   = city;
        this.parent = parent;
        this.gCost  = accumulatedCost;
    }

    /** Default ordering by accumulated path cost (used by UCS priority queue). */
    @Override
    public int compareTo(SearchNode other) {
        return Integer.compare(this.gCost, other.gCost);
    }

    @Override
    public String toString() {
        return city + " (g=" + gCost + ")";
    }
}