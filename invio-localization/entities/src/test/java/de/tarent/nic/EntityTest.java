package de.tarent.nic;

import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.WifiScanResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This test collects boring entity-tests, because they should not have any significant logic anyway.
 */
public class EntityTest {

    @Test
    public void testWifiScanResult() {
        WifiScanResult wifiScanResult = new WifiScanResult("name", -10);
        assertEquals("name", wifiScanResult.getBssid());
        assertEquals(-10, wifiScanResult.getLevel());
    }

    @Test
    public void testHistogram() {
        Histogram histogram = new Histogram();
        histogram.setId("MyId");
        assertEquals("MyId", histogram.getId());

        histogram = new Histogram("some other id");
        assertEquals("some other id", histogram.getId());
        histogram.setId("MyId");
        assertEquals("MyId", histogram.getId());
    }
}
