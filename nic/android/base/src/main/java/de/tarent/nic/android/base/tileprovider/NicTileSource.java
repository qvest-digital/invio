package de.tarent.nic.android.base.tileprovider;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.io.File;

/**
 * This class represents a TileSource for a Nic-Specific Map.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class NicTileSource extends OnlineTileSourceBase {
    private static final String SCHEMA_PARAM_MAP_NAME = "{mapname}";
    private static final String SCHEMA_PARAM_ZOOM = "{zoom}";
    private static final String SCHEMA_PARAM_X = "{x}";
    private static final String SCHEMA_PARAM_Y = "{y}";
    private static final String SCHEMA_PARAM_ENDING = "{ending}";
    private final String mUrlSchema;

    /**
     * Constructor.
     *
     * @param name                TODO
     * @param resourceId          TODO
     * @param zoomMinLevel        the minimum zoom level of the map
     * @param zoomMaxLevel        the maximum zoom level of the map
     * @param tileSizeInPixel     the tile size in pixels
     * @param imageFilenameEnding the image suffix
     */
    public NicTileSource(final String name, final string resourceId, final int zoomMinLevel,
                         final int zoomMaxLevel, final int tileSizeInPixel, final String imageFilenameEnding) {
        this(name, resourceId, zoomMinLevel, zoomMaxLevel, null, tileSizeInPixel, imageFilenameEnding);
    }

    /**
     * Constructor.
     *
     * @param name                TODO
     * @param resourceId          TODO
     * @param zoomMinLevel        the minimum zoom level of the map
     * @param zoomMaxLevel        the maximum zoom level of the map
     * @param urlSchema           TODO
     * @param tileSizeInPixel     the tile size in pixels
     * @param imageFilenameEnding the image suffix
     */
    public NicTileSource(final String name, final string resourceId, final int zoomMinLevel, final int zoomMaxLevel,
                         final String urlSchema, final int tileSizeInPixel, final String imageFilenameEnding) {
        super(name, resourceId, zoomMinLevel, zoomMaxLevel, tileSizeInPixel, imageFilenameEnding);
        this.mUrlSchema = urlSchema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTileRelativeFilenameString(final MapTile tile) {
        // <tileSourceName>/<zoom>/<x>/<y>.<ending>
        final String filePath = name() + File.separator + tile.getZoomLevel() + File.separator + tile.getX() +
                File.separator + getYinTMSFormat(tile.getZoomLevel(), tile.getY()) + imageFilenameEnding();
        return filePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTileURLString(final MapTile tile) {
        if (getUrlSchema() == null) {
            return null;
        }
        final String resultUrl = getUrl(tile);
        return resultUrl;
    }

    /**
     * Gets the url from the url schema.
     *
     * @param tile the map tile
     * @return the url
     */
    private String getUrl(final MapTile tile) {
        // example schema: http://localhost:8080/maps/{mapname}/tiles/{zoom}/{x}/{y}.{ending} NOSONAR
        final String mapName = name();
        final String zoom = String.valueOf(tile.getZoomLevel());
        final String x = String.valueOf(tile.getX());
        final String y = String.valueOf(getYinTMSFormat(tile.getZoomLevel(), tile.getY()));
        final String ending = imageFilenameEnding();

        final String url = constructUrl(mapName, zoom, x, y, ending);
        return url;
    }

    //TODO: simplify method signature (maybe a map?)
    private String constructUrl(final String mapName, final String zoom, final String x, final String y,
                                final String ending) {
        final String url = mUrlSchema
                .replace(SCHEMA_PARAM_MAP_NAME, mapName)
                .replace(SCHEMA_PARAM_ZOOM, zoom)
                .replace(SCHEMA_PARAM_X, x)
                .replace(SCHEMA_PARAM_Y, y)
                .replace(SCHEMA_PARAM_ENDING, ending);
        return url;
    }

    /*
     * Die Generierten Kacheln haben eine andere Y-Axen-Ausrichtung (von
     * Unten nach Oben). OSMDroid ließt die Kacheln aber von Oben nach Unten
     * (das wird oft als "TMS" beschrieben).
     */

    /**
     * TODO:  write javadoc.
     *
     * @param zoom TODO
     * @param y    TODO
     * @return TODO
     */
    protected int getYinTMSFormat(final int zoom, final int y) {
        //(2^zoom) - y - 1
        final int reverseY = (1 << zoom) - y - 1;
        return reverseY;
    }

    protected String getUrlSchema() {
        return mUrlSchema;
    }
}
