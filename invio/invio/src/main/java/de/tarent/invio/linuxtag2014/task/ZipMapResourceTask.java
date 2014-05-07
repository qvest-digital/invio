package de.tarent.invio.linuxtag2014.task;

import android.util.Log;
import de.tarent.invio.linuxtag2014.map.InvioIndoorMap;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.mapserver.exception.NicException;
import org.apache.commons.io.FileUtils;
import org.osmdroid.util.BoundingBoxE6;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: This class contains A LOT of duplicate code from {@link de.tarent.nic.android.base.task.DownloadMapResourceTask}
 * TODO: It needs to be refactored
 */
public class ZipMapResourceTask extends DownloadTask {

    private static final String TAG = ZipMapResourceTask.class.getCanonicalName();

    // This pattern matches numbers like 50.45621323455456 and takes the 50 and 6 digits after the decimal point.
    // We then parse this as an integer because some numbers might not be valid floats. But we want E6-integers anyway.
    private static final Pattern BOUNDING_BOX_PATTERN = Pattern.compile(
            "<BoundingBox minx=\"(-?\\d+).(\\d{6})\\d+\" miny=\"(-?\\d+).(\\d{6})\\d+\"" +
                    " maxx=\"(-?\\d+).(\\d{6})\\d+\" maxy=\"(-?\\d+).(\\d{6})\\d+\"/>");

    private static final Pattern ZOOM_LEVEL_PATTERN = Pattern.compile(
            "<TileSet .*? order=\"([\\d]+)\"/>");

    private int minZoomLevel = 0;

    // This default seems to be the maximum that osmdroid supports:
    private int maxZoomLevel = 22;

    private BoundingBoxE6 boundingBox;


    //The concrete map directory inside the unzipped group data directory
    private final InvioIndoorMap indoorMap;


    public ZipMapResourceTask(final DownloadListener listener, final InvioIndoorMap indoorMap) {
        downloadListeners.add(listener);
        this.indoorMap = indoorMap;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            final String xmlString = getXmlString();
            parseZoomLevels(xmlString);
            parseBoundingBox(xmlString);
            //TODO: Strange way to set following directly in the map and not passing it through as a result!
            indoorMap.setBoundingBox(boundingBox);
            indoorMap.setZoomLevels(minZoomLevel, maxZoomLevel);
            success = true;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            success = false;
        } catch (NicException e) {
            Log.e(TAG, e.getMessage());
            success = false;
        }
        return indoorMap;
    }

    protected String getXmlString() throws IOException {
        final File resourceFile = new File(indoorMap.getMapDirectory()
                + File.separator + "tiles" + File.separator + "tilemapresource.xml");
        if(resourceFile.exists()) {
            final String result = FileUtils.readFileToString(resourceFile, "UTF-8");
            return result;
        } else {
            throw new FileNotFoundException("ERROR: No tilemapresource found! " +
                    "Was expected here: " + resourceFile.getPath());
        }
    }

    //TODO: This is duplicate code from DownloadMapResourceTask
    private void parseZoomLevels(String resourceXml) throws NicException {
        final Matcher m = ZOOM_LEVEL_PATTERN.matcher(resourceXml);
        // The pattern will be found once for each zoomlevel, in order. So the first one will be the minimum and the
        // last one will be the maximum.
        if (m.find()) {
            minZoomLevel = Integer.parseInt(m.group(1));
            maxZoomLevel = minZoomLevel;
            while (m.find()) {
                maxZoomLevel = Integer.parseInt(m.group(1));
            }
        } else {
            throw new NicException("Can't find zoom levels in tileMapResource.xml!");
        }
    }

    //TODO: This is duplicate code from DownloadMapResourceTask
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
