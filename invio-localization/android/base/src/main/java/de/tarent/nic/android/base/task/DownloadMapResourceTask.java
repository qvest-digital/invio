package de.tarent.nic.android.base.task;

import android.app.Activity;
import android.util.Log;
import de.tarent.nic.android.base.map.IndoorMap;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is the AsyncTask which downloads the tileMapResource.xml from the server, in the background.
 * That xml-file is created by gdal and it contains the zoom levels and the bounding box for our map.
 */
public class DownloadMapResourceTask extends DownloadTask<Void, Void, String> {

    public static final int MIN_ZOOM_LEVEL_PLUS = 3;

    private static final String TAG = "DownloadMapResourceTask";

    // This pattern matches numbers like 50.45621323455456 and takes the 50 and 6 digits after the decimal point.
    // We then parse this as an integer because some numbers might not be valid floats. But we want E6-integers anyway.
    private static final Pattern BOUNDING_BOX_PATTERN = Pattern.compile(
            "<BoundingBox minx=\"(-?\\d+).(\\d{6})\\d+\" miny=\"(-?\\d+).(\\d{6})\\d+\"" +
                        " maxx=\"(-?\\d+).(\\d{6})\\d+\" maxy=\"(-?\\d+).(\\d{6})\\d+\"/>");

    private static final Pattern ZOOM_LEVEL_PATTERN = Pattern.compile(
            "<TileSet .*? order=\"([\\d]+)\"/>");


    private Activity activity;

    private MapView mapView;

    private String mapName;

    private int minZoomLevel = 0;

    // This default seems to be the maximum that osmdroid supports:
    private int maxZoomLevel = 22;

    private BoundingBoxE6 boundingBox;

    private IndoorMap map;

    private MapServerClient mapServerClient;

    /**
     * Construct a new DownloadMapResourceTask for an IndoorMap (i.e. a part of a multi-level-map).
     *
     * @param activity the activity, to get the R and for UI-access
     * @param mapServerClient the client which should be used for talking to the map-server
     * @param mapName the name of the map for which we shall download the resource-xml
     * @param map the IndoorMap for which the resources need to be downloaded.
     *            If this is not null then this IndoorMap will receive the data from the resources.
     * @param listener the DownloadListener that wants to be notified when we are done here
     */
    public DownloadMapResourceTask(Activity activity,
                                   MapServerClient mapServerClient,
                                   String mapName,
                                   IndoorMap map,
                                   DownloadListener<String> listener) {
        this.activity = activity;
        this.mapServerClient = mapServerClient;
        this.mapName = mapName;
        this.map = map;
        if (listener != null) {
            downloadListeners.add(listener);
        }
    }

    /**
     * Construct a new DownloadMapResourceTask for the map of a MapView (i.e. a single-level-map).
     *
     * @param activity the activity, to get the R and for UI-access
     * @param mapServerClient the client which should be used for talking to the map-server
     * @param mapView the MapView which is to be configured according to the resources that we will have downloaded.
     *                Ii will be zoomed/bounded according to the resources.
     * @param mapName the name of the map for which we shall download the resource-xml
     */
    public DownloadMapResourceTask(Activity activity,
                                   MapServerClient mapServerClient,
                                   MapView mapView,
                                   String mapName) {
        this.activity = activity;
        this.mapServerClient = mapServerClient;
        this.mapView = mapView;
        this.mapName = mapName;
    }

    @Override
    protected String doInBackground(Void... params) {
        String resourceXml = null;
        try {
            resourceXml = getXml(mapName);
            parseBoundingBox(resourceXml);
            parseZoomLevels(resourceXml);
            publishResourceData();
        } catch (IOException e) {
            Log.e(TAG, "Failed to download tileMapResource.xml: " + e);
            success = false;
        } catch (NicException e) {
            Log.e(TAG, "Failed to download tileMapResource.xml: " + e);
            success = false;
        }

        success = true;

        return resourceXml;
    }

    /**
     * Get the XML from somewhere. Here, we get it directly from the server. Overwrite this method to get it from
     * somewhere else or to have it cached (see CachedDownloadMapResourceTask).
     * @param mapName the name of the map
     * @return the xml as a string
     * @throws IOException when the download failed (e.g. server not reachable)
     * @throws NicException when the server answered something other than OK
     */
    protected String getXml(String mapName) throws IOException, NicException {
        return mapServerClient.getTilemapresourceXml(mapName);
    }

    /**
     * Depending on how we were constructed we need to either update the MapView or store our data into an IndoorMap.
     */
    private void publishResourceData() {
        if (mapView != null) {
            updateMapView();
        }
        if (map != null) {
            map.setZoomLevels(minZoomLevel, maxZoomLevel);
            map.setBoundingBox(boundingBox);
        }
    }

    private void updateMapView() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.setMinZoomLevel(minZoomLevel);
                mapView.setMaxZoomLevel(maxZoomLevel);
                mapView.zoomToBoundingBox(boundingBox);
                mapView.setScrollableAreaLimit(boundingBox);
                mapView.invalidate();
            }
        });
    }

    private void parseZoomLevels(String resourceXml) throws NicException {
        final Matcher m = ZOOM_LEVEL_PATTERN.matcher(resourceXml);
        // The pattern will be found once for each zoomlevel, in order. So the first one will be the minimum and the
        // last one will be the maximum.
        if (m.find()) {
            minZoomLevel = Integer.parseInt(m.group(1))+ MIN_ZOOM_LEVEL_PLUS;
            maxZoomLevel = minZoomLevel;
            while (m.find()) {
                maxZoomLevel = Integer.parseInt(m.group(1));
            }
        } else {
            throw new NicException("Can't find zoom levels in tileMapResource.xml!");
        }
    }

    // TODO: Review: Method does more than just parse the bounding box!!!
    private void parseBoundingBox(String resourceXml) throws NicException {
        int south;
        int west;
        int north;
        int east;

        // TODO: find out why the order in the xml doesn't match the names/values as we expect them.
        final Matcher matcher = BOUNDING_BOX_PATTERN.matcher(resourceXml);
        if (matcher.find()) {
            // We take the parts from before and after the decimal-point and parse the combined strings:
            south = Integer.parseInt(matcher.group(1) + matcher.group(2));
            west  = Integer.parseInt(matcher.group(3) + matcher.group(4));
            north = Integer.parseInt(matcher.group(5) + matcher.group(6));
            east  = Integer.parseInt(matcher.group(7) + matcher.group(8));
        } else {
            throw new NicException("Can't find boundingbox in tileMapResource.xml!");
        }

        boundingBox = new BoundingBoxE6(north, east, south, west);
    }

}
