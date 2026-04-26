package astar;

import java.util.*;

/**
 * Implémentation de l'algorithme A* — Tâches 2, 3 et 4.
 */
public class AStar {

    private final Graph graph;
    private int nodesExpanded;

    public AStar(Graph graph) {
        this.graph = graph;
    }

    /**
     * Exécute A* depuis sourceName vers destName.
     * @return liste ordonnée des nœuds du chemin optimal, ou null si aucun chemin.
     */
    public List<Node> search(String sourceName, String destName) {
        graph.reset();

        Node source = graph.getNode(sourceName);
        Node dest   = graph.getNode(destName);
        if (source == null || dest == null) {
            throw new IllegalArgumentException("Ville inconnue");
        }

        // Open list : file de priorité triée par f(n)
        PriorityQueue<Node> openList   = new PriorityQueue<>();
        // Closed list : nœuds définitivement explorés
        Set<Node>           closedList = new LinkedHashSet<>();
        // Pour vérifier rapidement si un nœud est dans l'open list
        Set<Node>           openSet    = new HashSet<>();

        source.setG(0);
        openList.add(source);
        openSet.add(source);

        nodesExpanded = 0;
        int iteration = 0;

        while (!openList.isEmpty()) {
            iteration++;

            // 1. Extraire le nœud de plus faible f(n)
            Node current = openList.poll();
            openSet.remove(current);

            // --- Tâche 3 : trace d'exécution ---
            System.out.printf("%n=== Itération %d ===%n", iteration);
            System.out.printf("Nœud courant : %-12s | g=%-6.0f h=%-6.0f f=%.0f%n",
                current.getName(), current.getG(), current.getH(), current.getF());
            System.out.print("Open list    : [");
            // Affichage trié de l'open list
            List<Node> openSorted = new ArrayList<>(openList);
            Collections.sort(openSorted);
            for (int i = 0; i < openSorted.size(); i++) {
                System.out.print(openSorted.get(i));
                if (i < openSorted.size() - 1) System.out.print(", ");
            }
            System.out.println("]");
            System.out.print("Closed list  : [");
            Iterator<Node> it = closedList.iterator();
            while (it.hasNext()) {
                System.out.print(it.next().getName());
                if (it.hasNext()) System.out.print(", ");
            }
            System.out.println("]");

            // 2. Si c'est la destination → reconstruire le chemin
            if (current.equals(dest)) {
                nodesExpanded = closedList.size();
                return reconstructPath(current);
            }

            // 3. Ajouter à la closed list
            closedList.add(current);

            // 4. Développer les voisins
            for (Edge edge : graph.getNeighbors(current.getName())) {
                Node neighbor = edge.getDestination();

                if (closedList.contains(neighbor)) continue;

                double gNew = current.getG() + edge.getWeight();

                if (!openSet.contains(neighbor) || gNew < neighbor.getG()) {
                    neighbor.setG(gNew);
                    neighbor.setParent(current);

                    // Retirer l'ancienne version si elle existe, puis réinsérer
                    if (openSet.contains(neighbor)) {
                        openList.remove(neighbor);
                    }
                    openList.add(neighbor);
                    openSet.add(neighbor);
                }
            }
        }

        // Aucun chemin trouvé
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

    public int getNodesExpanded() {
        return nodesExpanded;
    }

    /**
     * Tâche 4 — Affichage du résultat final.
     */
    public void printResult(List<Node> path, String sourceName, String destName) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║          RÉSULTAT A*                     ║");
        System.out.println("╚══════════════════════════════════════════╝");

        if (path == null) {
            System.out.println("Aucun chemin trouvé entre " + sourceName + " et " + destName);
            return;
        }

        System.out.print("Chemin optimal : ");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(path.get(i).getName());
            if (i < path.size() - 1) System.out.print(" → ");
        }
        System.out.println();

        double totalCost = path.get(path.size() - 1).getG();
        System.out.printf("Coût total     : %.0f km%n", totalCost);
        System.out.printf("Nœuds développés (closed list) : %d%n", nodesExpanded);
    }
}
