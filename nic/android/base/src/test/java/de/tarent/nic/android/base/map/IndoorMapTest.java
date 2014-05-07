package de.tarent.nic.android.base.map;

import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.task.DownloadMapDataTask;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.android.base.wifi.FingerprintItem;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class IndoorMapTest {

    private ItemizedIconOverlay overlay = mock(ItemizedIconOverlay.class);

    @Test
    public void testSetters() {
        IndoorMap map = new IndoorMap(overlay);

        map.setZoomLevels(1, 10);
        assertEquals(1, map.getMinZoomLevel());
        assertEquals(10, map.getMaxZoomLevel());

        List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
        Fingerprint fp1 = new Fingerprint(mock(Histogram.class), null);
        Fingerprint fp2 = new Fingerprint(mock(Histogram.class), null);
        fingerprints.add(fp1);
        fingerprints.add(fp2);

        map.setFingerprints(fingerprints);
        verify(overlay, times(2)).addItem(any(FingerprintItem.class));

        BoundingBoxE6 box = new BoundingBoxE6(0, 1, 2, 3);
        map.setBoundingBox(box);
        assertEquals(box, map.getBoundingBox());
    }

    @Test
    public void testDownload() {
        Set<Edge> edges = new HashSet<Edge>();
        Edge e1 = new Edge(null, null);
        Edge e2 = new Edge(mock(NicGeoPoint.class), mock(NicGeoPoint.class));
        edges.add(e1);
        edges.add(e2);
        Map<String, Collection> downloadResult = new HashMap<String, Collection>();
        downloadResult.put("foo", null);
        downloadResult.put(OsmParserKeys.EDGES, edges);

        IndoorMap map = new IndoorMap(overlay);

        // No edges in the beginning:
        assertTrue((map.getEdges() == null) || (map.getEdges().size() == 0));

        // Wrong type of DownloadTask. No edges, but no crash either:
        DownloadTask wrongTask = mock(DownloadTask.class);
        map.onDownloadFinished(wrongTask, true, null);
        assertTrue((map.getEdges() == null) || (map.getEdges().size() == 0));

        // Correct task, but failed:
        DownloadTask rightTask = mock(DownloadMapDataTask.class);
        map.onDownloadFinished(rightTask, false, downloadResult);
        assertTrue((map.getEdges() == null) || (map.getEdges().size() == 0));

        map.onDownloadFinished(rightTask, true, downloadResult);
        assertEquals(2, map.getEdges().size());
        assertEquals(edges, map.getEdges());
    }
}
