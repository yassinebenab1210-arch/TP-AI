/**
 * Represents a weighted directed connection between two cities in the road network.
 */
public class Edge {
    public final String dest;
    public final int cost;

    public Edge(String destination, int distanceKm) {
        this.dest = destination;
        this.cost = distanceKm;
    }

    @Override
    public String toString() {
        return "-> " + dest + " (" + cost + " km)";
    }
}