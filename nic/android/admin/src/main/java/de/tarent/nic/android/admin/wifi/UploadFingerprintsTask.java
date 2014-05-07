package de.tarent.nic.android.admin.wifi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;

import java.io.IOException;

/**
 * This is the AsyncTask which uploads the fingerprints to the server, in the background.
 */
class UploadFingerprintsTask extends AsyncTask {

    private static final String TAG = "UploadFingerprintsTask";

    private FingerprintManager fingerprintManager;

    private ProgressDialog progressDialog;

    private Activity activity;

    private final MapServerClient mapServerClient;

    private String mapName;

    /**
     * Construct a new UploadFingerprintsTask, which belongs to a specific FingerprintManager
     * @param fingerprintManager the owner of this task
     * @param activity the activity, for ressources and UI
     * @param mapServerClient the {@link MapServerClient}
     * @param mapName the name of the map to which we shall upload the fingerprints
     */
    public UploadFingerprintsTask(FingerprintManager fingerprintManager,
                                  Activity activity,
                                  final MapServerClient mapServerClient,
                                  String mapName) {

        this.fingerprintManager = fingerprintManager;
        this.activity = activity;
        this.mapServerClient = mapServerClient;
        this.mapName = mapName;
    }

    // TODO: give notification in case of failure. Don't just log the errors. Do something :-)
    @Override
    protected Object doInBackground(Object... params) {
        showProgressDialog();
        final String fingerprints = fingerprintManager.getFingerprintsJson();
        doUpload(fingerprints);
        progressDialog.dismiss();
        return null;
    }

    private void doUpload(String json) {
        try {
            mapServerClient.uploadFingerprintsData(mapName, json);
        } catch (NicException e) {
            Log.e(TAG, "Failed to upload fingerprints: " + e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to upload fingerprints: " + e);
        }
    }

    private void showProgressDialog() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog progress = new ProgressDialog(activity);
                progress.setCancelable(true);
                progress.setMessage("Speichere Fingerprints...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setCanceledOnTouchOutside(false);
                progress.show();
                progressDialog = progress;
            }
        });
    }
}
