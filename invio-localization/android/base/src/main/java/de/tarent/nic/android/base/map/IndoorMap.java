package de.tarent.nic.android.base.map;

import de.tarent.nic.android.base.task.DownloadMapDataTask;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.android.base.wifi.FingerprintItem;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.Fingerprint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An IndoorMap consists of an overlay, to display the fingerprint icons, a boundingbox, and a max and min zoom level.
 * These fields are stored here until they are needed for the MapView.
 */
public class IndoorMap implements DownloadListener {

    private final ItemizedIconOverlay<FingerprintItem> fingerprintOverlay;

    private int minZoomLevel;

    private int maxZoomLevel;

    private BoundingBoxE6 boundingBox;

    private Set<Edge> edges;


    /**
     * Construct a new IndoorMap with a prefabricated overlay. For the kunden-app this overlay will usually not show
     * any real fingerprint icons.
     *
     * @param fingerprintOverlay the ItemizedIconOverlay for the fingerprints.
     */
    public IndoorMap(ItemizedIconOverlay<FingerprintItem> fingerprintOverlay) {
        this.fingerprintOverlay = fingerprintOverlay;
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

    /**
     * Set the list of fingerprints.
     *
     * @param fingerprints List<Fingerprint>
     */
    public void setFingerprints(List<Fingerprint> fingerprints) {
        for (Fingerprint fingerprint : fingerprints) {
            final FingerprintItem fingerprintItem = new FingerprintItem(fingerprint);
            fingerprintOverlay.addItem(fingerprintItem);
        }
    }

    public ItemizedIconOverlay<FingerprintItem> getFingerprintOverlay() {
        return fingerprintOverlay;
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

    public Set<Edge> getEdges() {
        return edges;
    }

    @Override
    public void onDownloadFinished(DownloadTask task, boolean success, Object data) {
        if (success && (task instanceof DownloadMapDataTask)) {
            edges = (Set<Edge>)((Map<String, Set>)data).get(OsmParserKeys.EDGES);
            return;
        }
    }

}
