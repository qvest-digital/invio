package de.tarent.invio.linuxtag2014.task;

import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.mapserver.MapServerClient;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Override;
import java.lang.String;
import java.lang.Void;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
public class CachedDownloadGroupDataTask extends DownloadTask<Void, Void, File> {

    private static final String TAG = CachedDownloadGroupDataTask.class.getName();

    final private static String GROUP_DATA_DIR = Environment.getExternalStorageDirectory() + File.separator + "invio";

    final private String groupName;

    final private MapServerClient client;

    public CachedDownloadGroupDataTask(final DownloadListener<File> listener, final String groupName, MapServerClient client) {
        downloadListeners.add(listener);
        this.groupName = groupName;
        this.client = client;
    }

    @Override
    protected File doInBackground(final Void... params) {

        final File groupDataFile = new File(GROUP_DATA_DIR+ File.separator + groupName + "_data.zip");
        ensureDirExists(groupDataFile);
        final File unzipDir = new File(GROUP_DATA_DIR+ File.separator + groupName);
        ensureDirExists(unzipDir);
        try {
            final InputStream in = client.getGroupData(groupName, 6000);
            final OutputStream os = new FileOutputStream(groupDataFile);
            IOUtils.copy(in, os);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
            unzipFile(groupDataFile, unzipDir);
            success = true;
        } catch (IOException e) {
            Log.e(TAG, "Map group data download/persistence or unzip failed! Falling back to cache (if such exists).");

        }

        // If we don't have any maps inside the unzipped group directory, then something went wrong and we must signal
        // it.
        if(unzipDir.listFiles() == null || unzipDir.listFiles().length == 0 ) {
            success = false;
        } else {
            success = true;
        }

        return unzipDir;
    }

    private void ensureDirExists(final File directory){
        File groupDataFileParent = directory.getParentFile();
        if(!groupDataFileParent.exists()) {
            if(groupDataFileParent.mkdirs()) {
                Log.d(TAG, "Created directory: " + directory.getPath());
            } else {
                Log.e(TAG, "Could not create directory: " + directory.getPath());
            }
        }
    }

    protected void unzipFile (final File fileToUnzip, final File directory) throws IOException {
        final ZipFile zipFile = new ZipFile(fileToUnzip);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            final File entryFile = new File(directory, entry.getName());
            ensureDirExists(entryFile);
            if(entry.isDirectory()) {
                entryFile.mkdirs();
            } else {
                final InputStream is = zipFile.getInputStream(entry);
                final OutputStream os = new FileOutputStream(entryFile);
                IOUtils.copy(is, os);
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }
        }
    }


}
