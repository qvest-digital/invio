package de.tarent.invio.linuxtag2014.task;

import android.util.Log;
import de.tarent.invio.linuxtag2014.map.InvioIndoorMap;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 */
public class ZipFingerprintsTask extends DownloadTask<Void, Void, String> {

    private final static String TAG = ZipFingerprintsTask.class.getCanonicalName();

    private final InvioIndoorMap indoorMap;


    public ZipFingerprintsTask(final DownloadListener listener, final InvioIndoorMap indoorMap) {
        addDownloadListener(listener);
        this.indoorMap = indoorMap;
    }

    @Override
    protected String doInBackground(final Void... params) {
        String jsonString = new String();
        try {
            jsonString = getJsonString();
            success = true;
        } catch (IOException e) {
            success = false;
            Log.e(TAG, e.getMessage());
        }
        return jsonString;
    }


    protected String getJsonString() throws IOException {
        final File fingerprintsFile = new File(indoorMap.getMapDirectory()
                + File.separator + "fingerprints" + File.separator + "fingerprints_data.json");
        if(fingerprintsFile.exists()) {
            final String result = FileUtils.readFileToString(fingerprintsFile, "UTF-8");
            return result;
        } else {
            throw new FileNotFoundException("ERROR: No fingerprints file found! " +
                    "Was expected here: " + fingerprintsFile.getPath());
        }
    }
}
