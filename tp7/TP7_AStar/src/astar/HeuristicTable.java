package astar;

import java.util.HashMap;
import java.util.Map;

/**
 * Table d'heuristiques : injecte h(n) dans les nœuds depuis une source externe.
 * Ce design respecte la contrainte de la Tâche 1 : h(n) n'est pas stockée en dur dans Node.
 * On peut facilement remplacer cette table par un calcul Haversine (Extension A).
 */
public class HeuristicTable {
    private final Map<String, Double> table = new HashMap<>();

    public HeuristicTable() {}

    public void set(String city, double h) {
        table.put(city, h);
    }

    /**
     * Injecte les valeurs h dans tous les nœuds du graphe.
     */
    public void injectInto(Graph graph) {
        for (Map.Entry<String, Double> entry : table.entrySet()) {
            Node node = graph.getNode(entry.getKey());
            if (node != null) {
                node.setH(entry.getValue());
            }
        }
    }

    public double get(String city) {
        return table.getOrDefault(city, 0.0);
    }
}
