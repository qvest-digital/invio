package de.tarent.nic.android.base.task;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the abstract base class of all mapserver-download-something-tasks.
 *
 * @param <Params> this type parameter is inherited from AsyncTask and not used explicitly in this class.
 * @param <Progress> this type parameter is inherited from AsyncTask and not used explicitly in this class.
 * @param <Result> this is the type of the object that the DownloadTask will pass to its DownloadListener.
 */
public abstract class DownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected List<DownloadListener<Result>> downloadListeners = new ArrayList<DownloadListener<Result>>();

    protected boolean success;

    /**
     * Add a new DownloadListener to this task. If it already exists then nothing happens. Each listener will only get
     * called exactly once.
     *
     * @param listener  the new DownloadListener
     */
    public void addDownloadListener(DownloadListener<Result> listener) {
        downloadListeners.add(listener);
    }

    /**
     * Remove a DownloadListener from this task. If it was not registered first, nothing happens.
     *
     * @param listener  the DownloadListener we want to remove
     */
    public void removeDownloadListener(DownloadListener<Result> listener) {
        downloadListeners.remove(listener);
    }

    @Override
    protected  void onPostExecute(Result result) {
        super.onPostExecute(result);

        for (DownloadListener listener : downloadListeners) {
            listener.onDownloadFinished(this, success, result);
        }
    }

}
