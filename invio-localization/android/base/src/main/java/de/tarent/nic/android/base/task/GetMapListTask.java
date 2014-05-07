package de.tarent.nic.android.base.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.response.MapListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the AsyncTask which fetches the list of maps from the server, in the background. After it has finished
 * it will add the names to our list-adapter.
 */
public class GetMapListTask extends AsyncTask {

    /**
     * The tag for log-messages from this class:
     */
    private static final String TAG = "GetMapListTask";

    /**
     * This is the pattern to which the mapnames must conform so that we can detect them as levels of a multimap.
     * It supports only the levels 0-9. No sublevels and no multi-digit-levels. There must be no jumps/missing
     * levels, otherwise the MultiLevelFingerprintManager will become confused!
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(.*)-floor-(\\d)");

    /**
     * In this list we will store the map-names that we got from the server. This will happen in the background.
     * Then, onPostExecute will be called automatically, from the main-thread, and then the mapList will be applied.
     */
    private List<String> mapList = new ArrayList<String>();

    private final MapServerClient mapServerClient;

    private ProgressDialog progressDialog;

    private ArrayAdapter<String> adapter;

    /**
     * Constructor.
     *
     * @param mapServerClient the {@link MapServerClient}
     * @param progressDialog the {@link #progressDialog}
     * @param adapter        the {@link #adapter}
     */
    public GetMapListTask(final MapServerClient mapServerClient, final ProgressDialog progressDialog,
                          final ArrayAdapter<String> adapter) {

        this.mapServerClient = mapServerClient;
        this.progressDialog = progressDialog;
        this.adapter = adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doInBackground(final Object... params) {
        try {
            final MapListResponse response = mapServerClient.getMapList(5000);
            mapList = response.getMapList();
        } catch (IOException e) {
            Log.e(TAG, "Failed to download the map-list: " + e);
        }
        progressDialog.dismiss();

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostExecute(final Object result) {
        makeMultilevelMaps(mapList);
        adapter.addAll(mapList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Scan a list of map names and identify those that represent different levels of the same building.
     * Add virtual multilevel map names to the list.
     * @param mapList   the list of simple map-names, as it comes from the server
     */
    protected void makeMultilevelMaps(List<String> mapList) {
        final Map<String, List<String>> multiLevels =  findMultiMapLevels(mapList);

        // Turn the levels into strings and add them to the list model:
        for (Map.Entry<String, List<String>> entry : multiLevels.entrySet()) {
            final String mapName = entry.getKey();
            final List<String> levels = entry.getValue();
            if (levels.size() > 1) {
                final StringBuilder multiMap = buildMapLevels(mapName, levels);
                mapList.add(multiMap.toString());
            }
        }
    }

    private StringBuilder buildMapLevels(String mapName, List<String> levels) {
        final StringBuilder multiMap = new StringBuilder(mapName);
        multiMap.append(" [");
        Collections.sort(levels);
        for (String level : levels) {
            multiMap.append(level).append(", ");
        }
        multiMap.setLength(multiMap.length() - 2);
        multiMap.append("]");
        return multiMap;
    }

    /**
     * Find all levels in the mapList that belong to a multi-map.
     * @param mapList   the list of simple map-names, as it comes from the server
     * @return          the mapping of multiMap-names to their respective level-names
     */
    private Map<String, List<String>> findMultiMapLevels(List<String> mapList) {
        final Map<String, List<String>> multiLevels = new HashMap<String, List<String>>();

        for (String map : mapList) {
            final Matcher m = LEVEL_PATTERN.matcher(map);
            if (m.matches()) {
                final String mapName = m.group(1);
                final String level = m.group(2);
                List<String> levels = multiLevels.get(mapName);
                if (levels == null) {
                    levels = new ArrayList<String>();
                    multiLevels.put(mapName, levels);
                }
                levels.add(level);
            }
        }

        return multiLevels;
    }

}
