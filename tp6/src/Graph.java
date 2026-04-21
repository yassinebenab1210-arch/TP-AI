import java.util.*;

/**
 * Undirected weighted graph backed by an adjacency list.
 * Insertion order of cities is preserved via {@link LinkedHashMap}.
 */
public class Graph {
    public final Map<String, List<Edge>> adj = new LinkedHashMap<>();

    /**
     * Adds a bidirectional edge between {@code cityA} and {@code cityB}.
     * Self-loops and duplicate edges are silently ignored.
     */
    public void addEdge(String cityA, String cityB, int distanceKm) {
        if (cityA.equals(cityB)) return;

        adj.computeIfAbsent(cityA, k -> new ArrayList<>());
        adj.computeIfAbsent(cityB, k -> new ArrayList<>());

        if (!edgeExists(cityA, cityB)) {
            adj.get(cityA).add(new Edge(cityB, distanceKm));
            adj.get(cityB).add(new Edge(cityA, distanceKm));
        }
    }

    /** Returns all outgoing edges from {@code city}, or an empty list if unknown. */
    public List<Edge> neighbors(String city) {
        return adj.getOrDefault(city, Collections.emptyList());
    }

    /** Returns {@code true} if the graph contains the given city node. */
    public boolean hasCity(String cityId) {
        return adj.containsKey(cityId);
    }

    /** Returns {@code true} if a direct edge between {@code a} and {@code b} exists. */
    public boolean edgeExists(String a, String b) {
        for (Edge e : adj.getOrDefault(a, Collections.emptyList())) {
            if (e.dest.equals(b)) return true;
        }
        return false;
    }

    /** @deprecated Use {@link #edgeExists(String, String)} instead. */
    @Deprecated
    public boolean hasEdge(String a, String b) {
        return edgeExists(a, b);
    }

    /** Returns the total number of city nodes in the graph. */
    public int cityCount() {
        return adj.size();
    }

    /** Returns an unmodifiable view of all city identifiers. */
    public Set<String> cities() {
        return Collections.unmodifiableSet(adj.keySet());
    }
}