package astar;

import java.util.*;

/**
 * Uniform Cost Search (UCS) — pour la Question 3 du rapport.
 * UCS explore les nœuds par ordre de coût réel g(n) croissant.
 * Il est optimal mais aveugle à la destination (h(n) = 0 toujours).
 */
public class UCS {

    private final Graph graph;
    private int nodesExpanded;

    public UCS(Graph graph) {
        this.graph = graph;
    }

    public List<Node> search(String sourceName, String destName) {
        graph.reset();

        Node source = graph.getNode(sourceName);
        Node dest   = graph.getNode(destName);

        // File de priorité ordonnée par g(n)
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getG));
        Set<Node> closedList = new LinkedHashSet<>();
        Set<Node> openSet    = new HashSet<>();

        source.setG(0);
        openList.add(source);
        openSet.add(source);
        nodesExpanded = 0;

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            openSet.remove(current);

            if (current.equals(dest)) {
                nodesExpanded = closedList.size();
                return reconstructPath(current);
            }

            closedList.add(current);

            for (Edge edge : graph.getNeighbors(current.getName())) {
                Node neighbor = edge.getDestination();
                if (closedList.contains(neighbor)) continue;

                double gNew = current.getG() + edge.getWeight();
                if (!openSet.contains(neighbor) || gNew < neighbor.getG()) {
                    neighbor.setG(gNew);
                    neighbor.setParent(current);
                    if (openSet.contains(neighbor)) openList.remove(neighbor);
                    openList.add(neighbor);
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }

    private List<Node> reconstructPath(Node dest) {
        List<Node> path = new ArrayList<>();
        Node current = dest;
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        return path;
    }

    public int getNodesExpanded() { return nodesExpanded; }
}
