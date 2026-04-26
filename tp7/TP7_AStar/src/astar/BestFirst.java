package astar;

import java.util.*;

/**
 * Best-First Search (Greedy) — pour la Question 3 du rapport.
 * Explore les nœuds uniquement selon h(n), sans tenir compte du coût réel.
 * Guidé mais non optimal.
 */
public class BestFirst {

    private final Graph graph;
    private int nodesExpanded;

    public BestFirst(Graph graph) {
        this.graph = graph;
    }

    public List<Node> search(String sourceName, String destName) {
        graph.reset();

        Node source = graph.getNode(sourceName);
        Node dest   = graph.getNode(destName);

        // File de priorité ordonnée par h(n)
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getH));
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
                if (closedList.contains(neighbor) || openSet.contains(neighbor)) continue;

                double gNew = current.getG() + edge.getWeight();
                neighbor.setG(gNew);
                neighbor.setParent(current);
                openList.add(neighbor);
                openSet.add(neighbor);
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
