package de.tarent.nic.android.base.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * An OsmParser is a parser that parses osm-data-files like those produced by JOSM. The osm-files can contain many
 * different types of information Depending on the data, that an application is interested in, different parsers could
 * be used.
 */
public interface OsmParser {

    /**
     * Get all the collections that were parsed from the input. The keys to these collections come from OsmParserKeys
     * and its extensions.
     *
     * @return a map of result collections
     */
    Map<String, Collection> getResults();

    /**
     * Parse the input stream and and extract all the collections that the implementing class knows about
     *
     * @param input the input stream (with the OSM-json)
     * @throws IOException when the stream fails for some reason
     */
    void parse(InputStream input) throws IOException;

}
