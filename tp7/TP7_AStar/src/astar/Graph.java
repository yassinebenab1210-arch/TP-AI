package astar;

import java.util.*;

/**
 * Graphe non-orienté pondéré représenté par une liste d'adjacence.
 */
public class Graph {
    private final Map<String, Node>       nodes     = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.getName(), node);
        adjacency.putIfAbsent(node.getName(), new ArrayList<>());
    }

    /**
     * Ajoute une arête bidirectionnelle.
     */
    public void addEdge(String from, String to, double weight, String label) {
        Node src  = nodes.get(from);
        Node dest = nodes.get(to);
        if (src == null || dest == null) {
            throw new IllegalArgumentException("Nœud introuvable: " + from + " ou " + to);
        }
        adjacency.get(from).add(new Edge(src, dest, weight, label));
        adjacency.get(to).add(new Edge(dest, src, weight, label));
    }

    public List<Edge> getNeighbors(String nodeName) {
        return adjacency.getOrDefault(nodeName, Collections.emptyList());
    }

    public Node getNode(String name) {
        return nodes.get(name);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    /**
     * Réinitialise g, f, parent de tous les nœuds (pour relancer un algorithme).
     */
    public void reset() {
        for (Node n : nodes.values()) {
            n.setG(Double.MAX_VALUE);
            n.setParent(null);
        }
    }

    public void printGraph() {
        System.out.println("=== Graphe routier ===");
        Set<String> printed = new HashSet<>();
        for (Map.Entry<String, List<Edge>> entry : adjacency.entrySet()) {
            for (Edge e : entry.getValue()) {
                String key = e.getSource().getName() + "-" + e.getDestination().getName();
                String keyRev = e.getDestination().getName() + "-" + e.getSource().getName();
                if (!printed.contains(keyRev)) {
                    System.out.printf("  %-12s <--> %-12s : %5.0f km  [%s]%n",
                        e.getSource().getName(), e.getDestination().getName(),
                        e.getWeight(), e.getLabel());
                    printed.add(key);
                }
            }
        }
    }
}
