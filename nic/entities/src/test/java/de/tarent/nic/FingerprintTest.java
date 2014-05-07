package de.tarent.nic;

import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class FingerprintTest {

    @Mock
    NicGeoPoint point;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testConstruction() {
        Histogram histo = new Histogram("my histogram");

        Fingerprint fingerprint = new Fingerprint(histo, point);

        assertEquals(histo, fingerprint.getHistogram());
        assertEquals(point, fingerprint.getPoint());
        assertEquals("my histogram", fingerprint.getId());
    }

}
