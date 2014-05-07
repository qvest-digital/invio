package de.tarent.nic.android.base.wifi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import de.tarent.nic.android.base.sensor.SensorCollector;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.WifiScanResult;
import de.tarent.nic.tracker.wifi.HistogramBuilder;
import de.tarent.nic.tracker.wifi.HistogramConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * The WifiCapturer captures wifi-histograms. It will work in the background, but it will continuously update an
 * optional ProgressDialog and will add the histogram to a HistogramConsumer. When it has collected enough scan results
 * it will dismiss the ProgressDialog.
 * The WifiCapturer has two modes:
 * - fingerprint-mode, for the offline-phase, where it does a number of scans, produces one histogram, and then stops.
 * - continuous-mode, for the online-phase, where it goes on scanning and producing histograms until it is stopped.
 * The mode is selected via the ProgressDialog-parameter. If it is null then continuous-mode is selected.
 *
 * TODO:
 * - the number of scans could be made configurable.
 * - there should be a configurable timeout, in case the scanning takes much longer than expected.
 */
public class WifiCapturer implements SensorCollector {

    public static final String TAG ="WifiCapturer";

    /**
     * How many scans we want to do, unless we are in "continuous-scanning-mode" (i.e. have not ProgressDialog):
     */
    public static final int NUMBER_SCANS = 5;

    private Activity activity;

    /**
     * The progressDialog will be used to signal the progress back to the UI and to count our scans.
     * If we don't have one then nobody will be informed of the progress and we won't stop scanning by ourselves.
     */
    private ProgressDialog progressDialog;

    /**
     * We will hand the new histograms to this consumer. If we have a ProgressDialog then this will happen at the end.
     * If we don't have a ProgressDialog we are in continuous-background-scanning-mode and will produce a new histogram
     * after each scan.
     */
    private HistogramConsumer fingerprintHistogramConsumer;

    private HistogramConsumer trackinghistogramConsumer;

    private HistogramBuilder histogramBuilder;

    private WifiManager wifi;

    /**
     * The maximum age in milliseconds that a scanresult may have to be included in the histogram.
     * A maxAge of 0 is used to disable age-filtering.
     */
    private int maxAge;

    /**
     * The WifiReceiver will receive the scanresults from the WifiManager. We register it with the activity and
     * unregister it again, when we are done.
     */
    private WifiReceiver wifiReceiver;


    /**
     * Construct a new WifiCapturer for a specific Activity. This constructor cannot be used for tracking, only for
     * making fingerprints!
     * @param activity the Activity which will be used for Context and to run UI-stuff on.
     */
    public WifiCapturer(final Activity activity) {
        this.activity = activity;
        this.maxAge = 0;
    }

    /**
     * Construct a new WifiCapturer for a specific Activity.
     * @param activity the Activity which will be used for Context and to run UI-stuff on.
     * @param consumer the HistogramConsumer that needs the results that we generate periodically.
     * @param maxAge the maximum age in milliseconds that a scanresult may have to be included in the histogram.
     *               A maxAge of 0 is used to disable age-filtering.
     */
    public WifiCapturer(final Activity activity, final HistogramConsumer consumer, final int maxAge) {
        this.activity = activity;
        this.trackinghistogramConsumer = consumer;
        this.maxAge = maxAge;
    }

    /**
     * Start the scanning-process. Should not be called a second time before the asynchronous scanning-process has
     * finished or has been stopped explicitly.
     *
     * @param progressDialog a ProgressDialog to which the WifiCapturer can report its progress and which it can
     *                        dismiss, when it has finished scanning.
     * @param consumer the HistogramConsumer that will be called with the collected histogram.
     * TODO: if startScan is called a second time before stopScan we might leak a WifiReceiver.
     */
    public void makeFingerprint(final  ProgressDialog progressDialog, final HistogramConsumer consumer) {
        this.progressDialog = progressDialog;
        this.fingerprintHistogramConsumer = consumer;

        startSensors();
    }

    @Override
    public void startSensors() {
        histogramBuilder = new HistogramBuilder("my histo", maxAge);

        wifiReceiver = new WifiReceiver();
        activity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifi = (WifiManager) activity.getSystemService(activity.WIFI_SERVICE);
        wifi.startScan();
    }

    /**
     * Stop scanning. Don't receive scan results. That means we don't start any new scans either, because that would
     * happen in onReceive.
     * TODO: test what happens when stopSensors is called before startSensors.
     */
    @Override
    public void stopSensors() {
        if(wifiReceiver == null){
            return;
        }
        try {
            activity.unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e){
            Log.e(TAG, "The reciever was not registered or already unregistered: "+e.getMessage());
        }
    }


    /**
     * The callback-class that will receive the SCAN_RESULTS_AVAILABLE_ACTION notifications. It will integrate the
     * scan results into the Capturers HistogramBuilder and will start a new scan, until the planned scan-number
     * is reached.
     */
    class WifiReceiver extends BroadcastReceiver {

        /**
         * This method will be called by the Android-System when the WifiManager has completed a scan.
         *
         * @param c      ignored. We already have our parent-Activity.
         * @param intent ignored. We are only registered for one type of action and we know what to do.
         */
        @Override
        public void onReceive(final Context c, final Intent intent) {
            final List<ScanResult> scan = wifi.getScanResults();

            histogramBuilder.addScanResults(copyScanResults(scan));

            // Without a progressDialog we will just keep on scanning until stopScan() has turned off the callbacks to
            // this method.
            if (progressDialog == null) {
                final Histogram histogram = histogramBuilder.build();
                trackinghistogramConsumer.addHistogram(histogram);
                wifi.startScan();
            } else {
                progress();
            }
        }

        /**
         * Copy a list of android ScanResult into a list of our own WifiScanResult.
         * @param scanResults the List of scan results that the WifiManager supplied.
         * @return a List of our platform independent WifiScanResult
         */
        private List<WifiScanResult> copyScanResults(List<ScanResult> scanResults) {
            final List<WifiScanResult> wifiScanResults = new ArrayList<WifiScanResult>();
            for (ScanResult scan : scanResults) {
                wifiScanResults.add(new WifiScanResult(scan.BSSID, scan.level));
            }
            return wifiScanResults;
        }

        /**
         * Progress to the next step in fingerprint-mode: either do another scan, or finish scanning, depending on what
         * the currently recorded progress says.
         */
        private void progress() {
            final int max = progressDialog.getMax();
            final int newProgress = progressDialog.getProgress() + (max / NUMBER_SCANS);
            if (newProgress < max) {
                doAnotherIteration(newProgress);
            } else {
                finishScanning();
            }
        }

        private void finishScanning() {
            stopSensors();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Histogram histogram = histogramBuilder.build();
                    fingerprintHistogramConsumer.addHistogram(histogram);
                    progressDialog.dismiss();
                }
            });
        }

        private void doAnotherIteration(final int newProgress) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setProgress(newProgress);
                }
            });
            wifi.startScan();
        }
    }
}
