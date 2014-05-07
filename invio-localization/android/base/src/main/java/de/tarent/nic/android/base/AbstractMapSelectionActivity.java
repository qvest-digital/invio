package de.tarent.nic.android.base;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.tarent.nic.android.base.task.GetMapListTask;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.MapServerClientImpl;


/**
 * This is the starting Activity. It lets the user chose a mapName from a list received from the mapServer. Then it
 * starts up our main NicActivity.
 */
public abstract class AbstractMapSelectionActivity extends ListActivity {

    /**
     * The {@link ProgressDialog} tells the user that something happens (i.e. the map-list-download).
     */
    private ProgressDialog progressDialog;

    /**
     * Gets the intent with the map name.
     *
     * @param mapName the map name
     * @return the {@link Intent}
     */
    public abstract Intent getIntentWithMapName(final String mapName);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNetworkAvailable()) {
            showProgressDialog();

            // Get the list of maps in the background:
            final String serverEndpoint = getResources().getString(R.string.server_endpoint);
            final MapServerClient mapServerClient = new MapServerClientImpl(serverEndpoint);
            new GetMapListTask(mapServerClient, progressDialog, getAdapter()).execute(progressDialog);
        }
    }

    /**
     * {@inheritDoc}
     * </p>
     * If a list item is clicked than that name will be passed on to the NicActivity, which will be started directly.
     */
    @Override
    protected void onListItemClick(final ListView listView, final View view, final int position, final long id) {
        final String mapName = ((TextView) view).getText().toString();
        startActivity(getIntentWithMapName(mapName));
    }

    /**
     * Creates a {@link ProgressDialog} for the fetching of the map list and then proceeds to show it.
     */
    protected void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Hole Kartenliste...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * Initializes and returns the adapter that powers the list of our activity. The list adapter is also set to
     * the created adapter.
     *
     * @return the {@link ArrayAdapter}
     */
    private ArrayAdapter<String> getAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();

        return adapter;
    }

    /**
     * Checks whether or not the device is connected to a network.
     *
     * @return whether or not a network is available
     */
    protected boolean isNetworkAvailable() {
        final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = manager.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }
}
