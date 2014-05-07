package de.tarent.invio.linuxtag2014;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Demo activty for the LinuxTag invio app.
 */
public class LaunchActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //The key of the location id. The name of the key must be known to both apps.
        final String locationIdKey = getString(R.string.linuxtag_intent_key_location_id);

        //Check if the intent contains the location id
        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().hasExtra(locationIdKey)) {
                final String locationId = (String) getIntent().getExtras().get(locationIdKey);
                //Show the location in the map
                showRequestInDialog(locationId);
                getIntent().removeExtra(locationIdKey);
            }
        } else {
            startMapView();
        }
    }

    private void startMapView() {
        final Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mapIntent.putExtra("MapName", getString(R.string.map_default_name));
        mapIntent.putExtra("multilevelMap", true);
        startActivity(mapIntent);
    }

    /**
     * This dialog is for testing purposes.
     *
     * @param locationId of an exhibitor or schedule
     */
    private void showRequestInDialog(final String locationId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request from LinuxTag Fahrplan");
        builder.setMessage("Received loaction id: " + locationId);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
