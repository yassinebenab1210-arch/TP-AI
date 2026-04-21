import java.util.*;

/**
 * Breadth-First Search – explores the graph level by level.
 *
 * <p>Guarantees the <em>fewest-hops</em> path (not necessarily lowest cost).
 * Time and space complexity: O(V + E).
 */
public class BFS {
    private BFS() {}   // static utility – no instances

    /**
     * Runs BFS from {@code start} to {@code goal}.
     *
     * @return a {@link SearchResult} if a path exists, {@code null} otherwise
     */
    public static SearchResult run(Graph graph, String start, String goal) {
        if (!isValidRequest(graph, start, goal)) return null;

        Queue<SearchNode>  frontier    = new ArrayDeque<>();
        Set<String>        discovered  = new HashSet<>();
        List<String>       visitOrder  = new ArrayList<>();
        int                nodeCount   = 0;

        long startTime = System.currentTimeMillis();

        // Seed the frontier
        frontier.add(new SearchNode(start, null, 0));
        discovered.add(start);

        while (!frontier.isEmpty()) {
            SearchNode current = frontier.poll();
            nodeCount++;
            visitOrder.add(current.city);

            if (current.city.equals(goal)) {
                return buildResult(current, nodeCount, startTime, visitOrder);
            }

            for (Edge edge : graph.neighbors(current.city)) {
                if (!discovered.contains(edge.dest)) {
                    discovered.add(edge.dest);
                    frontier.add(new SearchNode(edge.dest, current, current.gCost + edge.cost));
                }
            }
        }

        return null; // goal unreachable
    }

    // ------------------------------------------------------------------ helpers

    private static boolean isValidRequest(Graph graph, String start, String goal) {
        return start != null && goal != null
            && graph.hasCity(start) && graph.hasCity(goal);
    }

    private static SearchResult buildResult(SearchNode goal, int nodeCount,
                                            long startTime, List<String> visitOrder) {
        LinkedList<String> path = new LinkedList<>();
        for (SearchNode n = goal; n != null; n = n.parent) {
            path.addFirst(n.city);
        }
        long elapsed = System.currentTimeMillis() - startTime;
        return new SearchResult(path, goal.gCost, nodeCount, elapsed, visitOrder);
    }
}