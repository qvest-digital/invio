package de.tarent.invio.linuxtag2014.task;

import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParser;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * The {@link ZipMapDataTask} will try to parse the concrete map data from the unzipped group directory.
 */
public class ZipMapDataTask extends DownloadTask<Void, Void, Map<String, Collection>> {

    //The concrete map directory inside the unzipped group data directory
    private final File unzippedMapDir;

    //The {@link OsmParser}
    private final OsmParser parser;


    /**
     * Constructor.
     *
     * @param downloadListener where to push the map data
     * @param unzippedMapDir   directory of the concrete unzipped map
     * @param parser           the parser that shall be used to parse the map data
     */
    public ZipMapDataTask (final DownloadListener<Map<String, Collection>> downloadListener,
                           final File unzippedMapDir,
                           final OsmParser parser) {
        downloadListeners.add(downloadListener);
        this.unzippedMapDir = unzippedMapDir;
        this.parser = parser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Collection> doInBackground(final Void... params) {
        final  Map<String, Collection> result = new HashMap<String, Collection>();
        try {
            parser.parse(getXmlStream());
            result.putAll(parser.getResults());
            success = true;
        } catch (IOException e) {
            success = false;
            // TODO: what should we do?
        }

        return result;
    }

    protected InputStream getXmlStream() throws FileNotFoundException {
        final String mapDataPath = unzippedMapDir + File.separator + "data" + File.separator +
                unzippedMapDir.getName()+".osm";
        final File mapDataFile = new File(mapDataPath);
        if(mapDataFile.exists()) {
            final InputStream stream = new FileInputStream(mapDataFile);
            return stream;
        } else {
            throw new FileNotFoundException("ERROR: No map data found! Was expected here: " + mapDataPath);
        }

    }
}
