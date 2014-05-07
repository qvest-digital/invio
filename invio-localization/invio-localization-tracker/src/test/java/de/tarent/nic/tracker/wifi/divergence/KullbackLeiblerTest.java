package de.tarent.nic.tracker.wifi.divergence;


import de.tarent.nic.tracker.wifi.RouterLevelHistogram;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KullbackLeiblerTest {

    private RouterLevelDivergence kl;

    @Before
    public void setup() {
        kl = new KullbackLeibler();
    }


    @Test
    public void testBothEmpty() {
        RouterLevelHistogram p = new RouterLevelHistogram();
        RouterLevelHistogram q = new RouterLevelHistogram();

        kl.init(p, q);

        assertTrue(Float.compare(0f, kl.getConfidence()) == 0);
        assertTrue(Float.compare(0f, kl.getDivergence()) == 0);
    }

    @Test
    public void testOneEmpty() {
        RouterLevelHistogram p = new RouterLevelHistogram();
        p.put("00:01:02:03:04:05", -25.0f);
        p.put("a0:b0:c0:d0:e0:f0", -30.5f);
        RouterLevelHistogram q = new RouterLevelHistogram();

        kl.init(p, q);

        assertTrue(Float.compare(0f, kl.getConfidence()) == 0);
        assertTrue(Float.compare(0f, kl.getDivergence()) == 0);
    }

    @Test
    public void testIdentity() {
        RouterLevelHistogram p = new RouterLevelHistogram();
        p.put("00:01:02:03:04:05", -25.0f);
        p.put("a0:b0:c0:d0:e0:f0", -30.5f);

        kl.init(p, p);

        assertTrue(Float.compare(1f, kl.getConfidence()) == 0);
        assertTrue(Float.compare(0f, kl.getDivergence()) == 0);
    }

    @Test
    public void testDivergingHistograms() {
        RouterLevelHistogram p = new RouterLevelHistogram();
        p.put("01:01:02:03:04:05", -25.0f);
        p.put("02:00:00:00:00:01", -25.0f);
        p.put("03:23:45:67:89:ab", -40.0f);
        p.put("04:b0:c0:d0:e0:f0", -30.5f);
        p.put("05:77:77:77:77:77", -40.0f);
        RouterLevelHistogram q = new RouterLevelHistogram();
        q.put("01:01:02:03:04:05", -25.0f);
        q.put("02:00:00:00:00:01", -20.0f);
        q.put("03:23:45:67:89:ab", -30.0f);
        q.put("06:bb:cc:dd:ee:ff", -50.0f);

        kl.init(p, q);

        assertEquals(0.01526f / 3f, kl.getDivergence(), 0.00001);
        assertEquals(0.66667f, kl.getConfidence(), 0.00001);
    }

    @Test
    public void testCompare() {
        RouterLevelHistogram p = new RouterLevelHistogram();
        p.put("01:01:02:03:04:05", -25.0f);
        p.put("02:00:00:00:00:01", -25.0f);
        p.put("03:23:45:67:89:ab", -40.0f);
        p.put("04:b0:c0:d0:e0:f0", -30.5f);
        p.put("05:77:77:77:77:77", -40.0f);
        RouterLevelHistogram q = new RouterLevelHistogram();
        q.put("01:01:02:03:04:05", -25.0f);
        q.put("02:00:00:00:00:01", -20.0f);
        q.put("03:23:45:67:89:ab", -30.0f);
        q.put("06:bb:cc:dd:ee:ff", -50.0f);
        RouterLevelHistogram r = new RouterLevelHistogram();
        r.put("02:00:00:00:00:01", -19.0f);
        r.put("03:23:45:67:89:ab", -29.0f);
        r.put("06:bb:cc:dd:ee:ff", -49.0f);

        kl.init(p, q);
        float divergencePQ = kl.getDivergence();

        kl.init(q, r);
        float divergenceQR = kl.getDivergence();

        kl.init(r, p);
        float divergenceRP = kl.getDivergence();

        // These proportions are pretty obvious:
        assertTrue(divergencePQ > divergenceQR);
        assertTrue(divergenceRP > divergenceQR);
        assertTrue(divergencePQ > divergenceRP);

        // We do not check the triangle inequality because it need not be true for our KL-divergence. The reasons is
        // for that is the removal of the non-matching accesspoints. That way PQ and RQ could be compared according to
        // completely disjunct accesspoints.
    }

    @Test
    public void testSymmetry() {
        RouterLevelHistogram p = new RouterLevelHistogram();
        p.put("00:01:02:03:04:05", -25.0f);
        p.put("a0:b0:c0:d0:e0:f0", -30.5f);
        RouterLevelHistogram q = new RouterLevelHistogram();
        q.put("00:01:02:03:04:05", -20.0f);
        q.put("aa:bb:cc:dd:ee:ff", -40.0f);

        kl.init(p, q);
        float divergencePQ = kl.getDivergence();
        float confidencePQ = kl.getConfidence();

        kl.init(q, p);
        float divergenceQP = kl.getDivergence();
        float confidenceQP = kl.getConfidence();

        assertTrue(Float.compare(divergencePQ, divergenceQP) == 0);
        assertTrue(Float.compare(confidencePQ, confidenceQP) == 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testNoInitNoDivergence() {
        kl.getDivergence();
    }

    @Test(expected = IllegalStateException.class)
    public void testNoIniNoConfidence() {
        kl.getConfidence();
    }

}
