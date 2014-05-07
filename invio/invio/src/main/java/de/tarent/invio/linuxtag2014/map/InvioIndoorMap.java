package de.tarent.invio.linuxtag2014.map;

import de.tarent.invio.linuxtag2014.products.ProductItem;
import de.tarent.invio.linuxtag2014.task.InvioOsmParserKeys;
import de.tarent.invio.linuxtag2014.task.ZipFingerprintsTask;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.invio.linuxtag2014.task.ZipMapDataTask;
import de.tarent.nic.entities.Edge;
import org.osmdroid.util.BoundingBoxE6;

import java.io.File;
import java.util.*;

/**
 * An IndoorMap consists of an overlay, to display the fingerprint icons, a boundingbox, and a max and min zoom level.
 * These fields are stored here until they are needed for the MapView.
 */
public class InvioIndoorMap implements DownloadListener {

    private String fingerprintsJson;

    private int minZoomLevel;

    private int maxZoomLevel;

    private BoundingBoxE6 boundingBox;

    private List<Edge> edges;

    private Float scale;

    private Integer baseAngle;

    private File mapDirectory;

    private String name;

    private String shortName;

    private Set<ProductItem> productItems;

    public InvioIndoorMap(File mapDirectory) {
        this.mapDirectory = mapDirectory;
    }

    /**
     * Set the limits for the zoomlevels.
     *
     * @param minZoomLevel minimum level
     * @param maxZoomLevel maximum level
     */
    public void setZoomLevels(int minZoomLevel, int maxZoomLevel) {
        this.minZoomLevel = minZoomLevel;
        this.maxZoomLevel = maxZoomLevel;
    }

    /**
     * Set the boundingbox of this map.
     * @param boundingBox the BoundingBoxE6
     */
    public void setBoundingBox(BoundingBoxE6 boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getMinZoomLevel() {
        return minZoomLevel;
    }

    public int getMaxZoomLevel() {
        return maxZoomLevel;
    }

    public BoundingBoxE6 getBoundingBox() {
        return boundingBox;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public File getMapDirectory() {
        return mapDirectory;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Set<ProductItem> getProductItems() {
        return productItems;
    }

    @Override
    public void onDownloadFinished(DownloadTask task, boolean success, Object data) {
        if (success) {
            if(task instanceof ZipMapDataTask) {
                edges = (List<Edge>) ((Map<String, Collection>)data).get(InvioOsmParserKeys.EDGES);
                scale = ((List<Float>)((Map<String, Collection>)data).get(InvioOsmParserKeys.INDOOR_SCALE)).get(0);
                baseAngle = ((List<Integer>)((Map<String, Collection>)data).get(InvioOsmParserKeys.NORTH_ANGLE)).get(0);
                name = ((List<Map<String, String>>)((Map<String, Collection>)data).get(InvioOsmParserKeys.NAMESPACE))
                        .get(0).get("namespace_map_name");
                productItems = ((Set<ProductItem>)((Map<String, Collection>)data).get(InvioOsmParserKeys.PRODUCTITEMS));
                shortName = ((List<Map<String, String>>)((Map<String, Collection>)data).get(InvioOsmParserKeys.NAMESPACE))
                        .get(0).get("namespace_short_name");
                return;
            } else if (task instanceof ZipFingerprintsTask) {
                final String jsonString = (String) data;
                fingerprintsJson = jsonString;
            }
        }
    }

    public String getFingerprintsJson() {
        return fingerprintsJson;
    }

    public Float getScale() {
        return scale;
    }

    public Integer getBaseAngle() {
        return baseAngle;
    }
}
