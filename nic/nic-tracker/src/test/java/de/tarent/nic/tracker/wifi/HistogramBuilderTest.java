package de.tarent.nic.tracker.wifi;

import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.WifiScanResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(MockitoJUnitRunner.class)
public class HistogramBuilderTest {

    @Mock
    WifiScanResult scan1;

    @Mock
    WifiScanResult scan2;

    @Mock
    WifiScanResult scan3;

    /**
     * Add scan-results to the HistogramBuilder and check that it builds the correct histogram.
     */
    @Test
    public void testBuild() {
        initMocks(this); // We need to mock the ScanResult-objects, because they don't have public constructors.

        HistogramBuilder builder;
        Histogram histogram;

        builder = new HistogramBuilder("some histogram");

        histogram = builder.build();
        assertEquals("some histogram", histogram.getId());

        // In the beginning we have no accesspoints:
        assert (histogram.keySet().isEmpty());

        // Then we add a scan with one accesspoint:
        scan1 = new WifiScanResult("00:01:02:03:04:05", -50);
        List<WifiScanResult> scanResults = new ArrayList<WifiScanResult>();
        scanResults.add(scan1);
        builder.addScanResults(scanResults);
        histogram = builder.build();
        assertEquals(1, histogram.keySet().size());
        Map<Integer, Float> levels = histogram.get("00:01:02:03:04:05");
        assertEquals(1, levels.size());
        assert (levels.get(-50) == 1.0f);

        // In the second scan we have two accesspoints, a new one and the old one again, now stronger:
        scan1 = new WifiScanResult("00:01:02:03:04:05", -30);
        scan2 = new WifiScanResult("a0:b0:c0:d0:e0:f0", -40);
        scanResults.clear();
        scanResults.add(scan1);
        scanResults.add(scan2);
        builder.addScanResults(scanResults);
        histogram = builder.build();
        assertEquals(2, histogram.keySet().size());
        assert (histogram.containsKey("00:01:02:03:04:05"));
        assert (histogram.keySet().contains("a0:b0:c0:d0:e0:f0"));

        // And both accesspoints contain the correct levels:
        levels = histogram.get("00:01:02:03:04:05");
        assertEquals (2, levels.size());
        assert (levels.get(-30) == 0.5f);
        assert (levels.get(-50) == 0.5f);
        levels = histogram.get("a0:b0:c0:d0:e0:f0");
        assert (levels.size() == 1);
        assert (levels.get(-40) == 1f);

        // Now for a third scan, again with a new accesspoint.
        scan1 = new WifiScanResult("00:01:02:03:04:05", -30);
        scan2 = new WifiScanResult("a0:b0:c0:d0:e0:f0", -40);
        scan3 = new WifiScanResult("01:23:45:67:89:ab", -20);
        scanResults.clear();
        scanResults.add(scan1);
        scanResults.add(scan2);
        scanResults.add(scan3);
        builder.addScanResults(scanResults);
        histogram = builder.build();
        assertEquals(3, histogram.keySet().size());
        assert (histogram.containsKey("00:01:02:03:04:05"));
        assert (histogram.containsKey("a0:b0:c0:d0:e0:f0"));
        assert (histogram.containsKey("01:23:45:67:89:ab"));

        // And all accesspoints contain the correct levels:
        levels = histogram.get("00:01:02:03:04:05");
        assert(levels.size() == 2);
        assertEquals(2.0f / 3.0f, levels.get(-30), 0.000001);
        assertEquals(1.0f / 3.0f, levels.get(-50), 0.000001);
        levels = histogram.get("a0:b0:c0:d0:e0:f0");
        assert(levels.size() == 1);
        assert(levels.get(-40) == 1f);
        levels = histogram.get("01:23:45:67:89:ab");
        assert(levels.size() == 1);
        assert(levels.get(-20) == 1f);
    }

    /**
     * Add scan-results to the HistogramBuilder and check that it builds the correct histogram even if some of the
     * scans are outdated.
     */
    @Test
    public void testAgingBuild() throws InterruptedException {
        initMocks(this); // We need to mock the ScanResult-objects, because they don't have public constructors.

        HistogramBuilder builder;
        Histogram histogram;

        // We specify a maximum age of 5 milliseconds.
        builder = new HistogramBuilder("some histogram", 5);

        // We add a scan with one accesspoint:
        List<WifiScanResult> scanResults = new ArrayList<WifiScanResult>();
        scan1 = new WifiScanResult("00:01:02:03:04:05", -50);
        scanResults.add(scan1);
        builder.addScanResults(scanResults);

        // In the second scan we have the same accesspoints, now stronger:
        scan1 = new WifiScanResult("00:01:02:03:04:05", -30);
        scanResults.clear();
        scanResults.add(scan1);
        builder.addScanResults(scanResults);

        // Now we wait for the previous scans to grow old...
        Thread.sleep(6);

        // And add two more scans:
        scan1 = new WifiScanResult("00:01:02:03:04:05", -40);
        scanResults.clear();
        scanResults.add(scan1);
        builder.addScanResults(scanResults);

        scan1 = new WifiScanResult("00:01:02:03:04:05", -44);
        scanResults.clear();
        scanResults.add(scan1);
        builder.addScanResults(scanResults);

        // Build the histogram, which should now contain only the two newer scans:
        histogram = builder.build();
        assertEquals(1, histogram.keySet().size());
        Map<Integer, Float> levels = histogram.get("00:01:02:03:04:05");

        // And the levels of our only accesspoint are...
        assertEquals (2, levels.size());
        assert (levels.get(-40) == 0.5f);
        assert (levels.get(-44) == 0.5f);
    }

}
