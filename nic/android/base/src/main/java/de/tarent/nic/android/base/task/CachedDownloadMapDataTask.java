package de.tarent.nic.android.base.task;

import android.os.Environment;
import android.util.Log;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;


/**
 * The CachedDownloadMapDataTask tries do download the mapdata and store it locally. When the download failes it will
 * use the file that was stored last time. So it is more of a "fallback" than a "cache"...
 */
public class CachedDownloadMapDataTask extends DownloadMapDataTask {

    private static final String APP_FOLDER = "sellfio";

    private static final String MAPDATA_FILE = "mapdata.xml";

    /**
     * Constructor.
     *
     * @param downloadListener the {@link DownloadListener}
     * @param mapServerClient the {@link MapServerClient}
     * @param mapName the map name
     * @param parser           the parser that shall be used to parse the mapdata
     */
    public CachedDownloadMapDataTask(final DownloadListener<Map<String, Collection>> downloadListener,
                                     final MapServerClient mapServerClient,
                                     final String mapName,
                                     final OsmParser parser) {
        super(downloadListener, mapServerClient, mapName, parser);
    }

    /**
     * Constructor.
     *
     * @param mapServerClient the {@link MapServerClient}
     * @param mapName the map name
     * @param parser           the parser that shall be used to parse the mapdata
     */
    public CachedDownloadMapDataTask(final MapServerClient mapServerClient,
                                     final String mapName,
                                     final OsmParser parser) {
        super(mapServerClient, mapName, parser);
    }

    /**
     * Get the xml with the mapdata from somewhere. It will first try download it from the server and store it locally.
     * Regardless of the downloadsuccss it will then provide the locally stored file, which may or may not have been
     * updated.
     * TODO: maybe check for updates before downloading the whole file again...
     * @return the InputStream from which the xml can be read.
     * @throws FileNotFoundException when the cached xml file cannot be read
     */
    @Override
    protected InputStream getXmlStream() throws FileNotFoundException {
        final File cache = new File(Environment.getExternalStorageDirectory() + File.separator +
                                    APP_FOLDER + File.separator + mapName + File.separator + MAPDATA_FILE);

        try {
            final String xml = mapServerClient.downloadMapData(mapName);
            persistMapDataXml(xml, cache);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Download/persistence of mapdata failed for "+mapName, e);
        } catch (NicException e) {
            Log.e(this.getClass().getName(), "Download/persistence of mapdata failed for "+mapName, e);
        }

        final InputStream stream = new FileInputStream(cache);
        return stream;
    }

    private void persistMapDataXml(String xml, File cache) throws IOException {
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
                Log.d(getClass().getName(), "Created directory "+APP_FOLDER);
            } else {
                Log.e(getClass().getName(), "CacheDir could not be created. Can't save mapData.xml.");
                throw new IOException("CacheDir could not be created. Can't save mapData.xml.");
            }
        }
    }

}
