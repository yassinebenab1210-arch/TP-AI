package astar;

/**
 * Construit le graphe routier tunisien du TP7.
 * Centralisé ici pour éviter la duplication entre Main et les tests.
 */
public class GraphBuilder {

    /**
     * Graphe de base (7 villes du sujet).
     */
    public static Graph buildBaseGraph() {
        Graph graph = new Graph();

        // Nœuds
        String[] cities = {"Tunis", "Sousse", "Kairouan", "Sfax", "Gafsa", "Tozeur"};
        for (String city : cities) {
            graph.addNode(new Node(city));
        }

        // Arêtes pondérées (bidirectionnelles)
        graph.addEdge("Tunis",    "Sousse",   140, "A1");
        graph.addEdge("Tunis",    "Kairouan", 160, "GP3");
        graph.addEdge("Sousse",   "Kairouan",  90, "MC82");
        graph.addEdge("Sousse",   "Sfax",     130, "A1");
        graph.addEdge("Kairouan", "Gafsa",    200, "GP3");
        graph.addEdge("Sfax",     "Gafsa",    150, "GP14");
        graph.addEdge("Gafsa",    "Tozeur",    90, "GP3");

        return graph;
    }

    /**
     * Heuristiques de base vers Tozeur.
     */
    public static HeuristicTable buildBaseHeuristic() {
        HeuristicTable h = new HeuristicTable();
        h.set("Tunis",    400);
        h.set("Sousse",   300);
        h.set("Kairouan", 250);
        h.set("Sfax",     230);
        h.set("Gafsa",    100);
        h.set("Tozeur",     0);
        return h;
    }

    /**
     * Graphe étendu — Question 4 : ajout de Gabès et El Kef.
     */
    public static Graph buildExtendedGraph() {
        Graph graph = buildBaseGraph();

        graph.addNode(new Node("Gabes"));
        graph.addNode(new Node("ElKef"));

        graph.addEdge("Sfax",     "Gabes",  75, "GP1");
        graph.addEdge("Gabes",    "Gafsa", 130, "GP15");
        graph.addEdge("Tunis",    "ElKef", 170, "GP5");
        graph.addEdge("ElKef",    "Kairouan", 110, "GP4");

        return graph;
    }

    /**
     * Heuristiques étendues.
     */
    public static HeuristicTable buildExtendedHeuristic() {
        HeuristicTable h = buildBaseHeuristic();
        h.set("Gabes", 180);
        h.set("ElKef", 320);
        return h;
    }
}
