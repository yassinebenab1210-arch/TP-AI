package astar;

import java.util.List;

/**
 * TP7 — Algorithme A* | GLSI2 Semestre 4 — 2025/2026
 * Enseignant : Mohamed Lassoued
 *
 * Ce fichier principal exécute :
 *   - Tâches 1-4 : A* de base avec trace complète
 *   - Question 3  : comparaison A* / UCS / Best-First
 *   - Question 4  : graphe étendu avec Gabès et El Kef
 *   - Bonus Q2b   : A* avec h(Gafsa) = 500 (heuristique inadmissible)
 */
public class Main {

    public static void main(String[] args) {

        // ══════════════════════════════════════════════════
        //  PARTIE 1 : A* — Graphe de base (Tâches 1 à 4)
        // ══════════════════════════════════════════════════
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   TP7 — Algorithme A* | GPS Tunisie                 ║");
        System.out.println("║   GLSI2 — Semestre 4 — 2025/2026                   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        Graph graph = GraphBuilder.buildBaseGraph();
        HeuristicTable heuristic = GraphBuilder.buildBaseHeuristic();
        heuristic.injectInto(graph);

        graph.printGraph();

        System.out.println("\n▶ Recherche A* : Tunis → Tozeur");
        AStar astar = new AStar(graph);
        List<Node> pathAStar = astar.search("Tunis", "Tozeur");
        astar.printResult(pathAStar, "Tunis", "Tozeur");

        // ══════════════════════════════════════════════════
        //  PARTIE 2 : Question 3 — Comparaison des algo
        // ══════════════════════════════════════════════════
        System.out.println("\n\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║   QUESTION 3 — Comparaison UCS / Best-First / A*    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // UCS
        Graph g2 = GraphBuilder.buildBaseGraph();
        GraphBuilder.buildBaseHeuristic().injectInto(g2);
        UCS ucs = new UCS(g2);
        List<Node> pathUCS = ucs.search("Tunis", "Tozeur");
        System.out.print("\n[UCS] Chemin : ");
        printPath(pathUCS);
        System.out.printf("      Coût   : %.0f km | Nœuds développés : %d%n",
            pathUCS != null ? pathUCS.get(pathUCS.size()-1).getG() : 0, ucs.getNodesExpanded());

        // Best-First
        Graph g3 = GraphBuilder.buildBaseGraph();
        GraphBuilder.buildBaseHeuristic().injectInto(g3);
        BestFirst bf = new BestFirst(g3);
        List<Node> pathBF = bf.search("Tunis", "Tozeur");
        System.out.print("\n[Best-First] Chemin : ");
        printPath(pathBF);
        System.out.printf("             Coût   : %.0f km | Nœuds développés : %d%n",
            pathBF != null ? pathBF.get(pathBF.size()-1).getG() : 0, bf.getNodesExpanded());

        // A* (déjà calculé)
        System.out.print("\n[A*] Chemin : ");
        printPath(pathAStar);
        System.out.printf("     Coût   : %.0f km | Nœuds développés : %d%n",
            pathAStar != null ? pathAStar.get(pathAStar.size()-1).getG() : 0, astar.getNodesExpanded());

        // ══════════════════════════════════════════════════
        //  PARTIE 3 : Question 2b — h(Gafsa) = 500 (inadmissible)
        // ══════════════════════════════════════════════════
        System.out.println("\n\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Q2b — Heuristique inadmissible : h(Gafsa) = 500   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        Graph g4 = GraphBuilder.buildBaseGraph();
        HeuristicTable hBad = GraphBuilder.buildBaseHeuristic();
        hBad.set("Gafsa", 500); // inadmissible
        hBad.injectInto(g4);

        AStar astarBad = new AStar(g4);
        List<Node> pathBad = astarBad.search("Tunis", "Tozeur");
        System.out.println();
        astarBad.printResult(pathBad, "Tunis", "Tozeur");

        // ══════════════════════════════════════════════════
        //  PARTIE 4 : Question 4 — Graphe étendu
        // ══════════════════════════════════════════════════
        System.out.println("\n\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║   QUESTION 4 — Graphe étendu (+ Gabès, El Kef)      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        Graph extGraph = GraphBuilder.buildExtendedGraph();
        HeuristicTable extH = GraphBuilder.buildExtendedHeuristic();
        extH.injectInto(extGraph);

        extGraph.printGraph();
        System.out.println("\n▶ Recherche A* étendue : Tunis → Tozeur");
        AStar astarExt = new AStar(extGraph);
        List<Node> pathExt = astarExt.search("Tunis", "Tozeur");
        astarExt.printResult(pathExt, "Tunis", "Tozeur");

        // ══════════════════════════════════════════════════
        //  Tableau de synthèse final
        // ══════════════════════════════════════════════════
        System.out.println("\n\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║   TABLEAU COMPARATIF FINAL                           ║");
        System.out.println("╠══════════════════════╦══════════╦═══════════════════╣");
        System.out.println("║ Algorithme           ║  Coût    ║ Nœuds développés  ║");
        System.out.println("╠══════════════════════╬══════════╬═══════════════════╣");
        printRow("UCS",         pathUCS,  ucs.getNodesExpanded());
        printRow("Best-First",  pathBF,   bf.getNodesExpanded());
        printRow("A*",          pathAStar,astar.getNodesExpanded());
        printRow("A* étendu",   pathExt,  astarExt.getNodesExpanded());
        System.out.println("╚══════════════════════╩══════════╩═══════════════════╝");
    }

    private static void printPath(List<Node> path) {
        if (path == null) { System.out.println("Aucun chemin"); return; }
        for (int i = 0; i < path.size(); i++) {
            System.out.print(path.get(i).getName());
            if (i < path.size() - 1) System.out.print(" → ");
        }
        System.out.println();
    }

    private static void printRow(String algo, List<Node> path, int expanded) {
        double cost = (path != null) ? path.get(path.size()-1).getG() : 0;
        System.out.printf("║ %-20s ║ %6.0f km ║ %-17d ║%n", algo, cost, expanded);
    }
}
