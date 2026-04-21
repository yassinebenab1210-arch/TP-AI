import java.util.*;

/**
 * Uniform-Cost Search – always expands the lowest cumulative-cost node.
 *
 * <p>Optimal when all edge costs are non-negative. Equivalent to Dijkstra's
 * single-target shortest path.
 */
public class UCS {
    private UCS() {}  // static utility – no instances

    // ------------------------------------------------------------------ public API

    /** Runs UCS with no blocked edges. */
    public static SearchResult run(Graph graph, String start, String goal) {
        return runWithBlockedEdges(graph, start, goal, null);
    }

    /** Runs UCS while treating edges in {@code blockedEdges} as absent. */
    public static SearchResult run(Graph graph, String start, String goal,
                                   Set<String> blockedEdges) {
        return runWithBlockedEdges(graph, start, goal, blockedEdges);
    }

    /**
     * Finds the second-best path by temporarily removing each edge on the
     * primary path and keeping the cheapest valid alternative.
     *
     * @param primaryPath the already-found optimal path
     * @return the best alternative path, or {@code null} if none exists
     */
    public static SearchResult findSecondBest(Graph graph, String start,
                                              String goal, List<String> primaryPath) {
        if (primaryPath == null || primaryPath.size() < 2) return null;

        SearchResult bestAlternative = null;
        Set<String>  blocked        = new HashSet<>();

        for (int i = 0; i < primaryPath.size() - 1; i++) {
            blocked.clear();
            blocked.add(makeEdgeKey(primaryPath.get(i), primaryPath.get(i + 1)));

            SearchResult candidate = runWithBlockedEdges(graph, start, goal, blocked);
            if (candidate == null || candidate.path.equals(primaryPath)) continue;

            if (bestAlternative == null || candidate.totalCost < bestAlternative.totalCost) {
                bestAlternative = candidate;
            }
        }
        return bestAlternative;
    }

    // ------------------------------------------------------------------ core search

    private static SearchResult runWithBlockedEdges(Graph graph, String start,
                                                    String goal, Set<String> blocked) {
        PriorityQueue<SearchNode> openSet    = new PriorityQueue<>();
        Set<String>               settled    = new HashSet<>();
        List<String>              visitOrder = new ArrayList<>();
        int                       nodeCount  = 0;

        long startTime = System.currentTimeMillis();
        openSet.add(new SearchNode(start, null, 0));

        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();
            if (settled.contains(current.city)) continue;

            settled.add(current.city);
            nodeCount++;
            visitOrder.add(current.city);

            if (current.city.equals(goal)) {
                return buildResult(current, nodeCount, startTime, visitOrder);
            }

            for (Edge edge : graph.neighbors(current.city)) {
                if (isBlocked(current.city, edge.dest, blocked)) continue;
                if (!settled.contains(edge.dest)) {
                    openSet.add(new SearchNode(edge.dest, current, current.gCost + edge.cost));
                }
            }
        }

        return null; // goal unreachable
    }

    // ------------------------------------------------------------------ helpers

    private static boolean isBlocked(String from, String to, Set<String> blocked) {
        return blocked != null && blocked.contains(makeEdgeKey(from, to));
    }

    /**
     * Produces a canonical (order-independent) key for an undirected edge
     * using a null-byte separator that cannot appear in city names.
     */
    private static String makeEdgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "\0" + b : b + "\0" + a;
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