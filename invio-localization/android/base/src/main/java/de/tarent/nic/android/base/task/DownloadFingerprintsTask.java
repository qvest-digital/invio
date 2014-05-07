package de.tarent.nic.android.base.task;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.android.base.wifi.MultiLevelFingerprintManager;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;


/**
 * This is the AsyncTask which downloads the fingerprints from the server, in the background.
 */
public class DownloadFingerprintsTask extends DownloadTask<Void, Void, String> {

    public static final String TAG = "DownloadFingerprintsTask";

    protected final String mapName;

    protected final MapServerClient mapServerClient;

    private FingerprintManager fingerprintManager;

    private Activity activity;

    private Integer level = null;

    /**
     * Construct a new DownloadFingerprintsTask, which belongs to a specific FingerprintManager
     *
     * @param fingerprintManager the owner of this task
     * @param activity           the activity, to get the R and for UI-access
     * @param mapServerClient    the client which should be used for talking to the map-server
     * @param mapName            the name of the map for which we shall download the fingerprints
     */
    public DownloadFingerprintsTask(FingerprintManager fingerprintManager,
                                    Activity activity,
                                    MapServerClient mapServerClient,
                                    String mapName) {
        this.fingerprintManager = fingerprintManager;
        this.activity = activity;
        this.mapServerClient = mapServerClient;
        this.mapName = mapName;
    }

    /**
     * Construct a new DownloadFingerprintsTask, which belongs to a specific FingerprintManager
     *
     * @param fingerprintManager the owner of this task
     * @param activity           the activity, to get the R and for UI-access
     * @param mapServerClient    the client which should be used for talking to the map-server
     * @param mapName            the name of the map for which we shall download the fingerprints
     * @param listener           the DownloadListener that wants to be notified when we are done here
     * @param level              the level for which we download the fingerprints
     */
    public DownloadFingerprintsTask(MultiLevelFingerprintManager fingerprintManager,
                                    Activity activity,
                                    MapServerClient mapServerClient,
                                    String mapName,
                                    DownloadListener<String> listener,
                                    int level) {
        this(fingerprintManager, activity, mapServerClient, mapName);
        this.level = level;
        downloadListeners.add(listener);
    }

    @Override
    protected String doInBackground(Void... params) {
        final StringWriter stringWriter = new StringWriter();
        try {
            IOUtils.copy(getInputStream(), stringWriter, "UTF-8");
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "Failed to read fingerprints: " + e);
        }
        final String fingerprintsJson = stringWriter.toString();

        if (level != null) {
            ((MultiLevelFingerprintManager) fingerprintManager).setFingerprintsJson(fingerprintsJson, level);
        } else {
            fingerprintManager.setFingerprintsJson(fingerprintsJson);
            showToast();
        }

        return fingerprintsJson;
    }

    private void showToast() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final String msg = activity.getString(R.string.toast_localisation_started);
                Toast.makeText(activity,
                        msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: give notification in case of failure. Don't just log the errors. Do something :-)
    private String downloadFingerprints() {
        String json = "[]";
        try {
            json = mapServerClient.downloadFingerprintsData(mapName);
            success = true;
        } catch (NicException e) {
            success = false;
            Log.e(TAG, "Failed to download fingerprints: " + e);
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "Failed to download fingerprints: " + e);
        }

        return json;
    }

    /**
     * Get the {@link InputStream} with the fingerprints from somewhere. In this case: from the mapserver.
     * But you can override this method to get it from somewhere else.
     *
     * @return the InputStream from which the fingerprints can be read.
     * @throws UnsupportedEncodingException when the given encoding is invalid
     * @throws FileNotFoundException        when overwriting methods cannot find the existing fingerprints data file
     */
    protected InputStream getInputStream() throws UnsupportedEncodingException, FileNotFoundException {
        final String json = downloadFingerprints();
        final InputStream stream = new ByteArrayInputStream(json.getBytes("UTF-8"));
        return stream;
    }

}
