package de.tarent.nic.android.base.task;

/**
 * A DownloadListener is a class that wants to be notified when some DownloadTask has finished.
 *
 * @param <T> T is the type of data that is to be downloaded,
 *            or at least the part of it that this listener will receive.
 */
public interface DownloadListener<T> {

    /**
     * The method onDownloadFinished will be called by the DownloadTask after is has finished downloading its data.
     * @param task the DownloadTask that has finished
     * @param success whether it succeeded or failed
     * @param data the data that it has downloaded
     */
    void onDownloadFinished(DownloadTask task, boolean success, T data);

}
