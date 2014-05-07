package de.tarent.nic.android.base.task;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.android.base.wifi.MultiLevelFingerprintManager;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * The CachedDownloadFingerprintsTask tries do download the fingerprints and store it locally. When the download failed
 * the file that was stored last time will be used. So it is more of a "fallback" than a "cache"...
 */
public class CachedDownloadFingerprintsTask extends DownloadFingerprintsTask {

    private static final String APP_FOLDER = "sellfio";

    private static final String FINGERPRINTS_DATA_FILE = "fingerprints_data";

    /**
     * Construct a new {@link CachedDownloadFingerprintsTask}, which belongs to a specific FingerprintManager
     *
     * @param fingerprintManager the owner of this task
     * @param activity           the activity, to get the R and for UI-access
     * @param mapServerClient    the client which should be used for talking to the map-server
     * @param mapName            the name of the map for which we shall download the fingerprints
     * @param listener           the DownloadListener that wants to be notified when we are done here
     * @param level              the level for which we download the fingerprints
     */
    public CachedDownloadFingerprintsTask(final MultiLevelFingerprintManager fingerprintManager,
                                          final Activity activity,
                                          final MapServerClient mapServerClient,
                                          final String mapName,
                                          DownloadListener<String> listener,
                                          int level) {
        super(fingerprintManager, activity, mapServerClient, mapName, listener, level);
    }

    /**
     * Construct a new {@link CachedDownloadFingerprintsTask}, which belongs to a specific FingerprintManager
     *
     * @param fingerprintManager the owner of this task
     * @param activity           the activity, to get the R and for UI-access
     * @param mapServerClient    the client which should be used for talking to the map-server
     * @param mapName            the name of the map for which we shall download the fingerprints
     */
    public CachedDownloadFingerprintsTask(final FingerprintManager fingerprintManager,
                                          final Activity activity,
                                          final MapServerClient mapServerClient,
                                          final String mapName) {
        super(fingerprintManager, activity, mapServerClient, mapName);
    }

    /**
     * Get the json with the fingerprints from somewhere. It will first try download it from the server and store it
     * locally. Regardless of the download success it will then provide the locally stored file, which may or may not
     * have been updated.
     * TODO: maybe check for updates before downloading the whole file again...
     *
     * @return the InputStream from which the json can be read.
     * @throws java.io.FileNotFoundException when the cached json file cannot be read
     */
    @Override
    protected InputStream getInputStream() throws FileNotFoundException {
        final File cache = new File(Environment.getExternalStorageDirectory() + File.separator +
                APP_FOLDER + File.separator + mapName + File.separator + FINGERPRINTS_DATA_FILE);

        try {
            final String json = mapServerClient.downloadFingerprintsData(mapName);
            persistFingerprintsJson(json, cache);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Download/persistence of fingerprints failed for " + mapName, e);
        } catch (NicException e) {
            Log.e(this.getClass().getName(), "Download/persistence of fingerprints failed for " + mapName, e);
        }

        final InputStream stream = new FileInputStream(cache);
        return stream;
    }

    private void persistFingerprintsJson(String json, File cache) throws IOException {
        ensureThatCacheDirExists(cache);
        final FileOutputStream out = new FileOutputStream(cache);
        try {
            out.write(json.getBytes("UTF-8"));
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
                Log.e(getClass().getName(), "CacheDir could not be created. Can't save fingerprints_data.");
                throw new IOException("CacheDir could not be created. Can't save fingerprints_data.");
            }
        }
    }

}
