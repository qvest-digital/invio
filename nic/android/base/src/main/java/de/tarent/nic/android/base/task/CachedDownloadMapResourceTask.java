package de.tarent.nic.android.base.task;


import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import de.tarent.nic.android.base.map.IndoorMap;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;
import org.apache.commons.io.IOUtils;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;


/**
 * The CachedDownloadMapResourceTask is a DownloadMapResourceTask that has a fallback to a local tilemapresource.xml if
 * that is available from an earlier download.
 * Note: we should probably pull the cache-feature out of the 3 download-tasks into one class and reuse it.
 */
public class CachedDownloadMapResourceTask extends DownloadMapResourceTask {

    private static final String APP_FOLDER = "sellfio";

    private static final String MAPRESOURCE_FILE = "tilemapresource.xml";

    /**
     * Constructor, just a pass-through to the base class.
     *
     * @param activity the activity, to get the R and for UI-access
     * @param mapServerClient the client which should be used for talking to the map-server
     * @param mapName the name of the map for which we shall download the resource-xml
     * @param map the IndoorMap for which the resources need to be downloaded.
     *            If this is not null then this IndoorMap will receive the data from the resources.
     * @param listener the DownloadListener that wants to be notified when we are done here
     */
    public CachedDownloadMapResourceTask(Activity activity, MapServerClient mapServerClient, String mapName,
                                         IndoorMap map, DownloadListener<String> listener) {
        super(activity, mapServerClient, mapName, map, listener);
    }

    /**
     * Constructor, just a pass-through to the base class.
     *
     * @param activity the activity, to get the R and for UI-access
     * @param mapServerClient the client which should be used for talking to the map-server
     * @param mapView the MapView which is to be configured according to the resources that we will have downloaded.
     *                Ii will be zoomed/bounded according to the resources.
     * @param mapName the name of the map for which we shall download the resource-xml
     */
    public CachedDownloadMapResourceTask(Activity activity, MapServerClient mapServerClient, MapView mapView,
                                         String mapName) {
        super(activity, mapServerClient, mapView, mapName);
    }


    @Override
    protected String getXml(String mapName) throws FileNotFoundException {
        final File cache = new File(Environment.getExternalStorageDirectory() + File.separator +
                APP_FOLDER + File.separator + mapName + File.separator + MAPRESOURCE_FILE);

        try {
            final String xml = super.getXml(mapName);
            persistMapresourceXml(xml, cache);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Download/persistence of tilemapresource.xml failed for " + mapName, e);
        } catch (NicException e) {
            Log.e(this.getClass().getName(), "Download/persistence of tilemapresource.xml failed for " + mapName, e);
        }

        final String cachedXml = readCachedXml(cache);

        return cachedXml;
    }

    private String readCachedXml(File cache) {
        final StringWriter stringWriter = new StringWriter();
        try {
            IOUtils.copy(new FileInputStream(cache), stringWriter, "UTF-8");
            return stringWriter.toString();
        } catch (FileNotFoundException e) {
            Log.e(this.getClass().getName(),
                  "Reading of local tilemapresource.xml failed for " + cache.getAbsolutePath(), e);
        } catch (IOException e) {
            Log.e(this.getClass().getName(),
                  "Reading of local tilemapresource.xml failed for " + cache.getAbsolutePath(), e);
        }
        return "";
    }

    private void persistMapresourceXml(String xml, File cache) throws IOException {
        ensureThatCacheDirExists(cache);
        final FileOutputStream out = new FileOutputStream(cache);
        try {
            out.write(xml.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private void ensureThatCacheDirExists(File cache) throws IOException {
        final File cacheDir = cache.getParentFile();
        if (!cacheDir.exists()) {
            if (cache.getParentFile().mkdirs()) {
                Log.d(getClass().getName(), "Created directory " + APP_FOLDER);
            } else {
                Log.e(getClass().getName(), "CacheDir could not be created. Can't save tilemapresource.xml.");
                throw new IOException("CacheDir could not be created. Can't save tilemapresource.xml.");
            }
        }
    }
}
