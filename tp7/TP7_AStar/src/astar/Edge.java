package astar;

/**
 * Représente une arête pondérée dans le graphe.
 */
public class Edge {
    private final Node source;
    private final Node destination;
    private final double weight;
    private final String label;

    public Edge(Node source, Node destination, double weight, String label) {
        this.source      = source;
        this.destination = destination;
        this.weight      = weight;
        this.label       = label;
    }

    public Node   getSource()      { return source; }
    public Node   getDestination() { return destination; }
    public double getWeight()      { return weight; }
    public String getLabel()       { return label; }

    @Override
    public String toString() {
        return String.format("%s --%s--> %s (%.0f km)",
            source.getName(), label, destination.getName(), weight);
    }
}
