/**
 * Extended city metadata that also stores population data.
 * {@code px}/{@code py} are pixel coordinates assigned by the rendering layer.
 */
public class PlaceData {
    public final String id;
    public final String display;
    public final double lat;
    public final double lon;
    public final int    population;

    /** Pixel position – assigned by the UI layer before painting. */
    public int px;
    public int py;

    public PlaceData(String id, double latitude, double longitude, int population) {
        this.id         = id;
        this.display    = id.replace('_', ' ');
        this.lat        = latitude;
        this.lon        = longitude;
        this.population = population;
    }

    @Override
    public String toString() {
        return display + " (pop=" + population + ")";
    }
}