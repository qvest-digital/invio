package de.tarent.nic.android.base.task;

import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This background task fetches the map-data from the server, parses the valid ways and the products from it with the
 * help of the OsmParser and passes the resulting objects to its DownloadListeners.<br/>
 * Because the AsyncTask can only pass one result-object we are passing a Map of all the different things.
 */
public class DownloadMapDataTask extends DownloadTask<Void, Void, Map<String, Collection>> {

    protected final String mapName;

    protected final MapServerClient mapServerClient;

    protected OsmParser parser;


    /**
     * Construct a new task for a specific map.
     *
     * @param downloadListener where to push the edges that we get from the server
     * @param mapServerClient  how to talk to the map server
     * @param mapName          which map we need the data for
     * @param parser           the parser that shall be used to parse the mapdata
     */
    public DownloadMapDataTask(final DownloadListener<Map<String, Collection>> downloadListener,
                               final MapServerClient mapServerClient,
                               final String mapName,
                               final OsmParser parser) {
        downloadListeners.add(downloadListener);
        this.mapServerClient = mapServerClient;
        this.mapName = mapName;
        this.parser = parser;
    }

    /**
     * Construct a new task for a specific map.
     *
     * @param mapServerClient  how to talk to the map server
     * @param mapName          which map we need the data for
     * @param parser           the parser that shall be used to parse the mapdata
     */
    public DownloadMapDataTask(final MapServerClient mapServerClient,
                               final String mapName,
                               final OsmParser parser) {
        this.mapServerClient = mapServerClient;
        this.mapName = mapName;
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
        } catch (NicException e) {
            success = false;
            // TODO: what should we do?
        }

        return result;
    }

    /**
     * Get the xml with the mapdata from somewhere. In this case: from the mapserver. But you can override this
     * method to get it from somewhere else.
     * @return the InputStream from which the xml can be read.
     * @throws IOException when the stream could not be created
     * @throws NicException when the mapserver could not be contacted or the xml could not be downloaded
     */
    protected InputStream getXmlStream() throws IOException, NicException {
        final String xml = mapServerClient.downloadMapData(mapName);
        final InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        return stream;
    }

}
