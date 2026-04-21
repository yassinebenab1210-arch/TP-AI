import java.util.*;

/**
 * Command-line experiment harness for comparing search algorithms
 * on a set of predefined Tunisia routing scenarios.
 *
 * <p>Usage:
 * <pre>
 *   java ExperimentRunner                      # runs default demo
 *   java ExperimentRunner --csv                # CSV output for all scenarios
 *   java ExperimentRunner --compare-all [S G]  # compare all algorithms for S→G
 * </pre>
 */
public class ExperimentRunner {

    // ------------------------------------------------------------------ scenario list

    private record Scenario(String start, String goal) {}

    private static final List<Scenario> SCENARIOS = List.of(
        new Scenario("Tunis",              "Tozeur"),
        new Scenario("Tunis",              "Sfax"),
        new Scenario("Nabeul",             "Gafsa"),
        new Scenario("Bizerte",            "Borj_El_Khadhra"),
        new Scenario("Djerba_Houmt_Souk",  "Tozeur")
    );

    // ------------------------------------------------------------------ entry point

    public static void main(String[] args) throws Exception {
        Map<String, CityData> cityMap = new LinkedHashMap<>();
        Graph graph = new Graph();
        DataLoader.loadAll(cityMap, graph);

        if (args.length > 0) {
            switch (args[0]) {
                case "--csv"         -> printCsvReport(graph, cityMap);
                case "--compare-all" -> printCompareAll(graph, cityMap, args);
                default              -> runDefaultDemo(graph, cityMap);
            }
        } else {
            runDefaultDemo(graph, cityMap);
        }
    }

    // ------------------------------------------------------------------ demo / cases

    private static void runDefaultDemo(Graph graph, Map<String, CityData> cityMap) {
        final String start = "Tunis", goal = "Tozeur";
        System.out.println("=== Best-First Search — Carte de Tunisie ===");

        printBestFirst(graph, cityMap, start, goal, null,
                       "CAS 1 — Tunis -> Tozeur (base)");

        printBestFirst(graph, cityMap, start, goal, badHeuristics(),
                       "CAS 2 — Mauvaise heuristique");

        System.out.println("\nCAS 3 — Comparaison BFS / DFS / UCS / Best-First");
        printAlgorithmComparison(graph, cityMap, start, goal);

        printBestFirst(graph, cityMap, start, goal, misleadingHeuristics(),
                       "CAS 4 — Heuristique trompeuse");

        System.out.println("\nRésumé multi-scénarios");
        SCENARIOS.forEach(s -> printScenarioSummary(graph, cityMap, s));
    }

    // ------------------------------------------------------------------ heuristic fixtures

    private static Map<String, Integer> badHeuristics() {
        Map<String, Integer> h = new LinkedHashMap<>();
        h.put("Le_Kef",   5000);
        h.put("Kasserine", 5000);
        h.put("Sfax",         1);
        h.put("Gabes",        1);
        return h;
    }

    private static Map<String, Integer> misleadingHeuristics() {
        Map<String, Integer> h = new LinkedHashMap<>();
        h.put("Le_Kef",    0);
        h.put("Kasserine", 0);
        h.put("Gafsa",  1200);
        return h;
    }

    // ------------------------------------------------------------------ output helpers

    private static SearchResult printBestFirst(Graph graph, Map<String, CityData> cityMap,
                                               String start, String goal,
                                               Map<String, Integer> overrides,
                                               String title) {
        System.out.println("\n" + title);
        SearchResult r = BestFirst.run(graph, cityMap, start, goal, overrides);
        if (r == null) { System.out.println("NO_PATH"); return null; }
        System.out.println("Chemin  : " + String.join(" -> ", r.path));
        System.out.println("Coût    : " + r.totalCost + " km");
        System.out.println("Nœuds   : " + r.nodesExplored);
        System.out.println("Temps   : " + r.timeMs + " ms");
        return r;
    }

    private static void printAlgorithmComparison(Graph graph, Map<String, CityData> cityMap,
                                                 String start, String goal) {
        SearchResult bfs = BFS.run(graph, start, goal);
        SearchResult dfs = DFS.run(graph, start, goal);
        SearchResult ucs = UCS.run(graph, start, goal);
        SearchResult bf  = BestFirst.run(graph, cityMap, start, goal);

        if (anyNull(bfs, dfs, ucs, bf)) { System.out.println("Résultat manquant."); return; }
        System.out.printf("BFS       | %d km, %d nœuds%n", bfs.totalCost, bfs.nodesExplored);
        System.out.printf("DFS       | %d km, %d nœuds%n", dfs.totalCost, dfs.nodesExplored);
        System.out.printf("UCS       | %d km, %d nœuds%n", ucs.totalCost, ucs.nodesExplored);
        System.out.printf("BestFirst | %d km, %d nœuds%n", bf.totalCost,  bf.nodesExplored);
    }

    private static void printScenarioSummary(Graph graph, Map<String, CityData> cityMap,
                                             Scenario s) {
        SearchResult bfs = BFS.run(graph, s.start(), s.goal());
        SearchResult dfs = DFS.run(graph, s.start(), s.goal());
        SearchResult ucs = UCS.run(graph, s.start(), s.goal());
        SearchResult bf  = BestFirst.run(graph, cityMap, s.start(), s.goal());

        if (anyNull(bfs, dfs, ucs, bf)) {
            System.out.println("- " + s.start() + " -> " + s.goal() + " : NO_PATH");
            return;
        }
        System.out.printf("- %s -> %s | BFS=%d/%d | DFS=%d/%d | UCS=%d/%d | BF=%d/%d | écart=%d km%n",
            s.start(), s.goal(),
            bfs.totalCost, bfs.nodesExplored,
            dfs.totalCost, dfs.nodesExplored,
            ucs.totalCost, ucs.nodesExplored,
            bf.totalCost,  bf.nodesExplored,
            bf.totalCost - ucs.totalCost);
    }

    private static void printCsvReport(Graph graph, Map<String, CityData> cityMap) {
        for (Scenario s : SCENARIOS) {
            SearchResult ucs = UCS.run(graph, s.start(), s.goal());
            SearchResult bf  = BestFirst.run(graph, cityMap, s.start(), s.goal());
            if (ucs == null || bf == null) {
                System.out.println(s.start() + "," + s.goal() + ",NO_PATH");
                continue;
            }
            System.out.printf("%s,%s,%d,%d,%d,%d,%d,%d%n",
                s.start(), s.goal(),
                ucs.totalCost, ucs.nodesExplored, ucs.timeMs,
                bf.totalCost,  bf.nodesExplored,  bf.timeMs);
        }
    }

    private static void printCompareAll(Graph graph, Map<String, CityData> cityMap,
                                        String[] args) {
        String s = args.length > 1 ? args[1] : "Tunis";
        String g = args.length > 2 ? args[2] : "Tozeur";
        emitCsvRow("BFS",        s, g, BFS.run(graph, s, g));
        emitCsvRow("DFS",        s, g, DFS.run(graph, s, g));
        emitCsvRow("UCS",        s, g, UCS.run(graph, s, g));
        emitCsvRow("Best-First", s, g, BestFirst.run(graph, cityMap, s, g));
    }

    private static void emitCsvRow(String algo, String start, String goal, SearchResult r) {
        if (r == null) { System.out.println(algo + "," + start + "," + goal + ",NO_PATH"); return; }
        System.out.printf("%s,%s,%s,%d,%d,%d,%d%n",
            algo, start, goal, r.totalCost, r.nodesExplored, r.timeMs, r.path.size());
    }

    private static boolean anyNull(Object... values) {
        for (Object v : values) if (v == null) return true;
        return false;
    }
}