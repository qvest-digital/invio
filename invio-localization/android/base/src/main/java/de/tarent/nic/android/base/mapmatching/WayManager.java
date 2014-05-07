package de.tarent.nic.android.base.mapmatching;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tarent.nic.android.base.json.NicGeoPointDeserializer;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * The {@link WayManager} manages all the way related tasks and keeps necessary information.
 */
public class WayManager implements DownloadListener<Map<String, Collection>> {

    private static final String TAG = "WayManager";

    protected Collection<Edge> edges;

    /**
     * Create a new {@link WayManager}.
     */
    public WayManager() {
        edges = new HashSet<Edge>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDownloadFinished(final DownloadTask task, final boolean success, final Map<String, Collection> data) {
        if (success) {
            setEdges(data.get(OsmParserKeys.EDGES));
        } else {
            Log.e(TAG, "Edge-download signals failure. We have no ways to manage.");
        }
    }

    /**
     * Create edges from json-string.
     *
     * @param json the json that contains the serialized edges
     * @return the set of edges
     */
    protected Collection<Edge> parseJson(final String json) {
        final Type fooType = new TypeToken<HashSet<Edge>>() {}.getType();
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NicGeoPoint.class, new NicGeoPointDeserializer());

        final Collection<Edge> edges = gsonBuilder.create().fromJson(json, fooType);
        return edges;
    }

    /**
     * Get a json-string that contains a serialization of the current edges.
     *
     * @return the edges as json
     */
    public String getEdgesJson() {
        final String json = new Gson().toJson(getEdges());
        return json;
    }

    /**
     * Deserialize a json-string into edges.
     *
     * @param json the edges as json
     */
    public void setEdgesJson(final String json) {
        final Collection<Edge> edges = parseJson(json);
        setEdges(edges);
    }

    public Collection<Edge> getEdges() {
        return edges;
    }

    public void setEdges(final Collection<Edge> edges) {
        this.edges = edges;
    }
}
