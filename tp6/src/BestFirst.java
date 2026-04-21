import java.util.*;

/**
 * Greedy Best-First Search – always expands the node with the lowest heuristic
 * estimate to the goal (straight-line / Haversine distance).
 *
 * <p>Fast in practice but <em>not</em> guaranteed to find the optimal path.
 * Heuristic values can be overridden per city for experimentation.
 */
public class BestFirst {
    private BestFirst() {}  // static utility – no instances

    // ------------------------------------------------------------------ public API

    /** Runs best-first search using the geographic heuristic. */
    public static SearchResult run(Graph graph, Map<String, CityData> cityMap,
                                   String start, String goal) {
        return run(graph, cityMap, start, goal, null);
    }

    /**
     * Runs best-first search with optional per-city heuristic overrides.
     *
     * @param heuristicOverrides city → custom h-value (may be {@code null})
     */
    public static SearchResult run(Graph graph, Map<String, CityData> cityMap,
                                   String start, String goal,
                                   Map<String, Integer> heuristicOverrides) {
        PriorityQueue<SearchNode> openSet    = buildPriorityQueue(goal, cityMap, heuristicOverrides);
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
                if (!settled.contains(edge.dest)) {
                    openSet.add(new SearchNode(edge.dest, current, current.gCost + edge.cost));
                }
            }
        }

        return null; // goal unreachable
    }

    /**
     * Pre-computes the heuristic value from every known city to {@code goal}.
     *
     * @return insertion-ordered map of city → h-value
     */
    public static Map<String, Integer> buildHeuristicTable(Map<String, CityData> cityMap,
                                                           String goal) {
        Map<String, Integer> table = new LinkedHashMap<>();
        for (String city : cityMap.keySet()) {
            table.put(city, computeHeuristic(city, goal, cityMap, null));
        }
        return table;
    }

    // ------------------------------------------------------------------ helpers

    /** Priority queue ordered by h(n) ascending, with gCost as tiebreaker. */
    private static PriorityQueue<SearchNode> buildPriorityQueue(
            String goal, Map<String, CityData> cityMap,
            Map<String, Integer> overrides) {
        return new PriorityQueue<>(
            Comparator
                .comparingInt((SearchNode n) -> computeHeuristic(n.city, goal, cityMap, overrides))
                .thenComparingInt(n -> n.gCost)
        );
    }

    private static int computeHeuristic(String city, String goal,
                                        Map<String, CityData> cityMap,
                                        Map<String, Integer> overrides) {
        if (city.equals(goal)) return 0;
        if (overrides != null && overrides.containsKey(city)) {
            return Math.max(0, overrides.get(city));
        }
        CityData from = cityMap.get(city);
        CityData to   = cityMap.get(goal);
        if (from == null || to == null) return 0;
        return (int) Math.round(haversineKm(from.lat, from.lon, to.lat, to.lon));
    }

    /** Great-circle distance between two geographic coordinates in kilometres. */
    private static double haversineKm(double lat1, double lon1,
                                      double lat2, double lon2) {
        final double R    = 6371.0;
        double       dLat = Math.toRadians(lat2 - lat1);
        double       dLon = Math.toRadians(lon2 - lon1);
        double       a    = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                          + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                          * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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