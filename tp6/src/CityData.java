/**
 * Holds geographic metadata for a Tunisian city node.
 * {@code px}/{@code py} are pixel coordinates set at render time by the map canvas.
 */
public class CityData {
    public final String id;
    public final String display;
    public final double lat;
    public final double lon;

    /** Pixel position – assigned by the UI layer before painting. */
    public int px;
    public int py;

    public CityData(String id, double latitude, double longitude) {
        this.id      = id;
        this.display = id.replace('_', ' ');
        this.lat     = latitude;
        this.lon     = longitude;
    }

    @Override
    public String toString() {
        return display + " (" + lat + ", " + lon + ")";
    }
}