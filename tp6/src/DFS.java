import java.util.*;

/**
 * Depth-First Search – dives deep before backtracking.
 *
 * <p>Does <em>not</em> guarantee shortest path or fewest hops.
 * Memory-efficient for deep graphs; may be slow on wide graphs.
 */
public class DFS {
    private DFS() {}  // static utility – no instances

    /**
     * Runs DFS from {@code start} to {@code goal}.
     *
     * @return a {@link SearchResult} if a path exists, {@code null} otherwise
     */
    public static SearchResult run(Graph graph, String start, String goal) {
        if (!isValidRequest(graph, start, goal)) return null;

        Deque<SearchNode> stack      = new ArrayDeque<>();
        Set<String>       discovered = new HashSet<>();
        List<String>      visitOrder = new ArrayList<>();
        int               nodeCount  = 0;

        long startTime = System.currentTimeMillis();

        stack.push(new SearchNode(start, null, 0));
        discovered.add(start);

        while (!stack.isEmpty()) {
            SearchNode current = stack.pop();
            nodeCount++;
            visitOrder.add(current.city);

            if (current.city.equals(goal)) {
                return buildResult(current, nodeCount, startTime, visitOrder);
            }

            expandNode(graph, stack, discovered, current);
        }

        return null; // goal unreachable
    }

    // ------------------------------------------------------------------ helpers

    private static boolean isValidRequest(Graph graph, String start, String goal) {
        return start != null && goal != null
            && graph.hasCity(start) && graph.hasCity(goal);
    }

    /**
     * Pushes undiscovered neighbours onto the stack in reverse order so that
     * the first neighbour in adjacency order is explored first.
     */
    private static void expandNode(Graph graph, Deque<SearchNode> stack,
                                   Set<String> discovered, SearchNode current) {
        List<Edge> edges = graph.neighbors(current.city);
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge edge = edges.get(i);
            if (!discovered.contains(edge.dest)) {
                discovered.add(edge.dest);
                stack.push(new SearchNode(edge.dest, current, current.gCost + edge.cost));
            }
        }
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