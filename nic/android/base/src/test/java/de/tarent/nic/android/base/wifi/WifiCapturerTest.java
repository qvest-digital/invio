package de.tarent.nic.android.base.wifi;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.tracker.wifi.HistogramConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class WifiCapturerTest {

    // The Object under Test:
    private WifiCapturer wifiCapturer;

    // Here we store the instance of the Capturers inner WifiReceiver:
    private BroadcastReceiver receiver;

    @Mock
    private Activity activity;

    @Mock
    private ProgressDialog progressDialog;

    @Mock
    private WifiManager wifiManager;

    @Mock
    private SensorManager sensorManager;

    @Mock
    private HistogramConsumer consumer;

    @Mock
    ScanResult scan1;

    @Mock
    ScanResult scan2;

    @Mock
    ScanResult scan3;


    @Before
    public void setUp() {
        initMocks(this);

        // The WifiCapturer will want to get a WifiManager from the system, so we provide our fake-WifiManager:
        when(activity.getSystemService(Activity.WIFI_SERVICE)).thenReturn(wifiManager);
        when(activity.getSystemService(Activity.SENSOR_SERVICE)).thenReturn(sensorManager);

        // Construct the WifiCapturer with our fake-activity which we can use to pass it various mocks:
        wifiCapturer = new WifiCapturer(activity, consumer, 0);

        // Our progressbar goes up to 100 (%):
        when(progressDialog.getMax()).thenReturn(100);
    }

    /**
     * Here we test the first part of the scanning-process only. It is a foreground-scan, i.e. one that is used to
     * collect several scans for a complete fingerprint.
     */
    @Test
    public void testFirstScan() {

        // Trigger the scan and pass our progressDialog-mock as callback-object:
        wifiCapturer.makeFingerprint(progressDialog, consumer);

        // Get us the BroadcastReceiver that the Captor has created for the wifi-notifications:
        receiver = getReceiver();

        // The progressDialog-mock starts at 0%, so it returns 0 when the progress is queried:
        when(progressDialog.getProgress()).thenReturn(0);

        // Send fake-notification to the receiver:
        receiver.onReceive(null, null);

        // We need the Runnable that the WifiReceiver creates because we need to execute it to see what it does:
        ArgumentCaptor<Runnable> uiRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(activity).runOnUiThread(uiRunnableArgumentCaptor.capture());
        Runnable uiRunnable = uiRunnableArgumentCaptor.getValue();

        // We have no uiThread so we call the Runnable ourselves.
        uiRunnable.run();

        // The Runnable was supposed to update the progressDialog to the first step:
        verify(progressDialog).setProgress(100 / WifiCapturer.NUMBER_SCANS);

        // There is also supposed to be a second scan triggered (by the receiver), after the initial one:
        verify(wifiManager, times(2)).startScan();

        // And our HistogramConsumer should NOT YET have been passed a histogram, because we are in
        // progress/foreground/fingerprint-mode.
        verify(consumer, never()).addHistogram(any(Histogram.class));
    }

    /**
     * Here we test the final iteration of the scanning-process.
     */
    @Test
    public void testLastScan() {
        // Setup: trigger the scan and pass our progressDialog-mock as callback-object:
        wifiCapturer.makeFingerprint(progressDialog, consumer);

        // Get us the BroadcastReceiver that the Captor has created for the wifi-notifications:
        receiver = getReceiver();

        // The progressDialog-mock is now supposed to indicate the last scan:
        when(progressDialog.getProgress()).thenReturn(100);

        // Send fake-notification to the receiver:
        receiver.onReceive(null, null);

        // We need the Runnable that the WifiReceiver creates because we need to execute it to see what it does:
        ArgumentCaptor<Runnable> uiRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(activity).runOnUiThread(uiRunnableArgumentCaptor.capture());
        Runnable uiRunnable = uiRunnableArgumentCaptor.getValue();

        // We have no uiThread so we call the Runnable ourselves.
        uiRunnable.run();

        // The Runnable was supposed dismiss the progressDialog:
        verify(progressDialog).dismiss();

        // Because we are done there should not be a second scan:
        verify(wifiManager, times(1)).startScan();

        // And our HistogramConsumer should have been passed a histogram, because we finished.
        verify(consumer).addHistogram(any(Histogram.class));
    }

    /**
     * Test the correct histogram-collection/calculation after 3 scans.
     */
    @Test
    public void testFingerprintHistogram() {
        wifiCapturer.makeFingerprint(progressDialog, consumer);

        // Get us the BroadcastReceiver that the Captor has created for the wifi-notifications:
        receiver = getReceiver();

        // The progressDialog-mock will indicate "unfinished" two times, and "100%" on the third call:
        when(progressDialog.getProgress()).thenReturn(1, 2, 100);

        // The WifiManager-mock will return three different scanresults, one on each call to getScanResults:
        List<ScanResult> scanResult1 = makeScanResult(scan1, -50);
        List<ScanResult> scanResult2 = makeScanResult(scan2, -40);
        List<ScanResult> scanResult3 = makeScanResult(scan3, -50);
        when(wifiManager.getScanResults()).thenReturn(scanResult1, scanResult2, scanResult3);

        // Send 3 fake-notifications to the receiver:
        receiver.onReceive(null, null);
        receiver.onReceive(null, null);
        receiver.onReceive(null, null);

        // We need the Runnable that the WifiReceiver creates because we need to execute it to see what it does:
        ArgumentCaptor<Runnable> uiRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        Runnable uiRunnable;

        // The ArgumentCaptor will automagically capture the arguments for each invocation:
        verify(activity, times(3)).runOnUiThread(uiRunnableArgumentCaptor.capture());

        // Now we get the three runnables and execute them all. The last should build our histogram, and the first two
        // should simply not break anything :-)
        uiRunnable = uiRunnableArgumentCaptor.getAllValues().get(0);
        uiRunnable.run();
        uiRunnable = uiRunnableArgumentCaptor.getAllValues().get(1);
        uiRunnable.run();
        uiRunnable = uiRunnableArgumentCaptor.getAllValues().get(2);
        uiRunnable.run();

        // After the third time the histogram should be built and we can verify its contents, step by step:
        ArgumentCaptor<Histogram> consumerArgumentCaptor = ArgumentCaptor.forClass(Histogram.class);
        verify(consumer).addHistogram(consumerArgumentCaptor.capture());
        Histogram histogram =  consumerArgumentCaptor.getValue();

        assertEquals(1, histogram.size());
        assertTrue(histogram.keySet().contains("00:01:02:03:04:05"));
        Map<Integer, Float> levels = histogram.get("00:01:02:03:04:05");
        assertEquals(2, levels.size());
        // Stupid floats need not be exactly equal:
        assertEquals(1.0 / 3.0, levels.get(-40), 0.00001);
        assertEquals(2.0 / 3.0, levels.get(-50), 0.00001);
    }

    /**
     * This test tests the continuous scanning, which is used for the localisation.
     */
    @Test
    public void testScanBuffer() {
        wifiCapturer.startSensors();

        // Get us the BroadcastReceiver that the Captor has created for the wifi-notifications:
        receiver = getReceiver();

        // The WifiManager-mock will return three different scanresults, one on each call to getScanResults:
        List<ScanResult> scanResult1 = makeScanResult(scan1, -50);
        List<ScanResult> scanResult2 = makeScanResult(scan2, -40);
        List<ScanResult> scanResult3 = makeScanResult(scan3, -50);
        when(wifiManager.getScanResults()).thenReturn(scanResult1, scanResult2, scanResult3);

        // Send 3 fake-notifications to the receiver:
        receiver.onReceive(null, null);
        receiver.onReceive(null, null);
        receiver.onReceive(null, null);

        // The activity should not have been bothered with any ui-stuff, because in this mode the scan is supposed to
        // happen completely in the background.
        verify(activity, never()).runOnUiThread(any(Runnable.class));

        // Collect the three Histograms, that our consumer hopefully received, one for each scan.
        // The ArgumentCaptor will automagically capture the arguments for each invocation:
        ArgumentCaptor<Histogram> consumerArgumentCaptor = ArgumentCaptor.forClass(Histogram.class);
        verify(consumer, times(3)).addHistogram(consumerArgumentCaptor.capture());
        Histogram histogram1 =  consumerArgumentCaptor.getAllValues().get(0);
        Histogram histogram2 =  consumerArgumentCaptor.getAllValues().get(1);
        Histogram histogram3 =  consumerArgumentCaptor.getAllValues().get(2);

        // All histograms should have the same size (there was always only one accesspoint visible):
        assertEquals(1, histogram1.size());
        assertEquals(1, histogram2.size());
        assertEquals(1, histogram3.size());

        // And it was really the same accesspoint each time:
        assertTrue(histogram1.keySet().contains("00:01:02:03:04:05"));
        assertTrue(histogram2.keySet().contains("00:01:02:03:04:05"));
        assertTrue(histogram3.keySet().contains("00:01:02:03:04:05"));

        // What's different is the levels that were collected for the different histograms, because the first one
        // had only one scanresult, the second had two, and the third histogram had all three scans.
        Map<Integer, Float> levels1 = histogram1.get("00:01:02:03:04:05");
        assertEquals(1, levels1.size());
        assertEquals(1.0f, levels1.get(-50), 0.00001);

        Map<Integer, Float> levels2 = histogram2.get("00:01:02:03:04:05");
        assertEquals(2, levels2.size());
        // Stupid floats need not be exactly equal:
        assertEquals(0.5f, levels2.get(-40), 0.00001);
        assertEquals(0.5f, levels2.get(-50), 0.00001);

        Map<Integer, Float> levels3 = histogram3.get("00:01:02:03:04:05");
        assertEquals(2, levels3.size());
        assertEquals(1.0 / 3.0, levels3.get(-40), 0.00001);
        assertEquals(2.0 / 3.0, levels3.get(-50), 0.00001);
    }

    /**
     * This test checks that the continuous-scan-mode can be interrupted.
     */
    @Test
    public void testStopScan() {
        wifiCapturer.startSensors();

        // Get us the BroadcastReceiver that the Captor has created for the wifi-notifications:
        receiver = getReceiver();

        // Send a fake-notification to the receiver. This will instantly trigger a second scan.
        receiver.onReceive(null, null);

        // Now we stop the scan-process:
        wifiCapturer.stopSensors();

        // We expect that this unregisters the broadcastreceiver in the activity
        verify(activity).unregisterReceiver(receiver);

        // And we expect that there were no more scans after that.
        verify(wifiManager, times(2)).startScan();
    }

    /**
     * The wifiCapturer should not unregister stuff that was not registered.
     */
    @Test
    public void testStopWithoutStart() {
        wifiCapturer.stopSensors();
        verifyZeroInteractions(activity);
    }


    // All WifiCapturer-tests will cause a WifiReceiver to be created. We need this receiver to call it with
    // our fake wifi-results-available-event:
    private BroadcastReceiver getReceiver() {
        ArgumentCaptor<BroadcastReceiver> receiverArgumentCaptor = ArgumentCaptor.forClass(BroadcastReceiver.class);
        verify(activity).registerReceiver(receiverArgumentCaptor.capture(), any(IntentFilter.class));
        return receiverArgumentCaptor.getValue();
    }

    // The trick is that the ScanResults must be mocks (no constructor..) but we cannot easily reuse them with
    // different values. So we use each one only once.
    private List<ScanResult> makeScanResult(ScanResult scan, int level) {
        scan.BSSID = "00:01:02:03:04:05";
        scan.level = level;
        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        scanResults.add(scan);
        return scanResults;
    }

}
