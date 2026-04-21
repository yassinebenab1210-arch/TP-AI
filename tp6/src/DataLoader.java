import java.io.*;
import java.util.*;

/**
 * Utility class that reads cities and road distances from CSV files
 * and populates a {@link Graph} and city metadata map.
 *
 * <p>Expected CSV formats:
 * <ul>
 *   <li>{@code cities.csv}: {@code id,lat,lon} (header row skipped)</li>
 *   <li>{@code distances.csv}: {@code cityA,cityB,km} (header row skipped)</li>
 * </ul>
 */
public class DataLoader {
    private DataLoader() {}   // utility class – no instances

    // ------------------------------------------------------------------ public API

    /**
     * Loads all data files found under the auto-detected data directory.
     *
     * @param cityMap destination map for city metadata (will be populated)
     * @param graph   destination graph (will be populated)
     */
    public static void loadAll(Map<String, CityData> cityMap, Graph graph) throws IOException {
        String dataDir = findDataDirectory();
        parseCities(dataDir + File.separator + "cities.csv", cityMap);
        parseDistances(dataDir + File.separator + "distances.csv", graph);
    }

    // ------------------------------------------------------------------ private helpers

    /** Tries several candidate paths and returns the first that contains {@code cities.csv}. */
    private static String findDataDirectory() {
        String[] candidates = {"data", "../data", "TunisiaUCS/data"};
        for (String dir : candidates) {
            if (new File(dir + "/cities.csv").exists()) return dir;
        }
        return "data"; // fallback – let the file I/O fail with a clear message
    }

    private static void parseCities(String filePath, Map<String, CityData> cityMap) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // skip CSV header
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String id  = cols[0].trim();
                    double lat = Double.parseDouble(cols[1].trim());
                    double lon = Double.parseDouble(cols[2].trim());
                    cityMap.put(id, new CityData(id, lat, lon));
                }
            }
        }
    }

    private static void parseDistances(String filePath, Graph graph) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // skip CSV header
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String cityA = cols[0].trim();
                    String cityB = cols[1].trim();
                    int    dist  = Integer.parseInt(cols[2].trim());
                    graph.addEdge(cityA, cityB, dist);
                }
            }
        }
    }
}