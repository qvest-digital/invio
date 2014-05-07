package de.tarent.nic.tracker.wifi;


import de.tarent.nic.entities.Histogram;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RouterLevelHistogramTest {

    // A test-histogram, containing measurements for 3 accesspoints:
    Histogram histogram;

    @Before
    public void setUp() {
        histogram = new Histogram("myHisto");

        // Make three maps of level-distributions, one for each accesspoint:
        Map<Integer, Float> levelsA = new HashMap<Integer, Float>();
        levelsA.put(-20, 0.1f);
        levelsA.put(-23, 0.9f);
        Map<Integer, Float> levelsB = new HashMap<Integer, Float>();
        levelsB.put(-42, 1f);
        Map<Integer, Float> levelsC = new HashMap<Integer, Float>();
        levelsC.put(-35, 0.2f);
        levelsC.put(-32, 0.3f);
        levelsC.put(-31, 0.1f);
        levelsC.put(-39, 0.35f);
        levelsC.put(-99, 0.05f);

        // Add them to the histogram:
        histogram.put("00:01:02:03:04:05", levelsA);
        histogram.put("a0:b0:c0:d0:e0:f0", levelsB);
        histogram.put("01:23:45:67:89:ab", levelsC);
    }


    @Test
    public void testMedianFactory() {
        RouterLevelHistogram rlh = RouterLevelHistogram.makeMedianHistogram(histogram);

        // The RouterLevelHistogram has the same number of accesspoints:
        assertEquals(3, rlh.size());
        // And these are the calculated meadian-strength-values for each of them:
        assertEquals(-21.5f, rlh.get("00:01:02:03:04:05"), 0.00001);
        assertEquals(-42f, rlh.get("a0:b0:c0:d0:e0:f0"), 0.00001);
        assertEquals(-35f, rlh.get("01:23:45:67:89:ab"), 0.00001);
    }

    @Test
    public void testAverageFactory() {
        RouterLevelHistogram rlh = RouterLevelHistogram.makeAverageHistogram(histogram);

        // The RouterLevelHistogram has the same number of accesspoints:
        assertEquals(3, rlh.size());
        // And these are the calculated average-strength-values for each of them:
        assertEquals(-22.7f, rlh.get("00:01:02:03:04:05"), 0.00001);
        assertEquals(-42f, rlh.get("a0:b0:c0:d0:e0:f0"), 0.00001);
        assertEquals(-38.3f, rlh.get("01:23:45:67:89:ab"), 0.00001);
    }

    @Test
    public void testNormalizer() {
        // It doesn't really matter which type we use as source-histogram:
        RouterLevelHistogram rlh = RouterLevelHistogram.makeAverageHistogram(histogram);

        RouterLevelHistogram normalRlh = RouterLevelHistogram.makeNormalizedHistogram(rlh);

        // The normalized RouterLevelHistogram has the same number of accesspoints:
        assertEquals(3, normalRlh.size());
        // And the levels are really normalized:
        float sum = normalRlh.get("00:01:02:03:04:05") +
                    normalRlh.get("a0:b0:c0:d0:e0:f0") +
                    normalRlh.get("01:23:45:67:89:ab");
        assertEquals(1.0f, sum, 0.00001);
        // And have the correct normalized values:
        assertEquals(0.22038835f, normalRlh.get("00:01:02:03:04:05"), 0.00001);
        assertEquals(0.40776699f, normalRlh.get("a0:b0:c0:d0:e0:f0"), 0.00001);
        assertEquals(0.37184466f, normalRlh.get("01:23:45:67:89:ab"), 0.00001);
    }

    @Test
    public void testNormlizeEmptyHistogram() {
        Histogram emptyHistogram = new Histogram("myHisto");
        RouterLevelHistogram rlh = RouterLevelHistogram.makeAverageHistogram(emptyHistogram);
        RouterLevelHistogram normalRlh = RouterLevelHistogram.makeNormalizedHistogram(rlh);

        assertEquals(rlh, normalRlh);
    }

    @Test
    public void testCopyConstructor() {
        RouterLevelHistogram p = RouterLevelHistogram.makeAverageHistogram(histogram);
        RouterLevelHistogram q = new RouterLevelHistogram(p);

        assertEquals(p, q);

        q.remove("00:01:02:03:04:05");

        assertEquals(3, p.size());
        assertEquals(2, q.size());
    }

    @Test
    public void testCut() {
        RouterLevelHistogram p = RouterLevelHistogram.makeAverageHistogram(histogram);
        p.put("ff:ff:ff:ff:ff:ff", null);
        p.put("aa:bb:cc:dd:ee:ff", null);

        RouterLevelHistogram q = new RouterLevelHistogram(p);
        q.remove("00:01:02:03:04:05");
        q.remove("01:23:45:67:89:ab");
        q.remove("aa:bb:cc:dd:ee:ff");

        int removedCount = p.intersect(q);

        assertEquals(3, removedCount);
        assertEquals(2, p.size());
        assertTrue(p.containsKey("a0:b0:c0:d0:e0:f0"));
        assertTrue(p.containsKey("ff:ff:ff:ff:ff:ff"));
    }

    // TODO: tests for copyconstructor & intersect with empty histograms.

}
