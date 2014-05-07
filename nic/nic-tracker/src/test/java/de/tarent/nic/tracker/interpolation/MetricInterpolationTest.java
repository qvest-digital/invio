package de.tarent.nic.tracker.interpolation;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.XYPoint;
import de.tarent.nic.tracker.geopoint.XYPointFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 14.11.13
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class MetricInterpolationTest {

    // Precision in meters... i.e. crappy ;-)  <- TODO: we should have better X/Y than geopoint lat/long-E6...
    static final float delta = 0.275f;

    PositionInterpolator interpolator;

    SortedSet<NicGeoPoint> distanceSet;

    NicGeoPoint p1, p2, p3;

    @Before
    public void setup() {
        interpolator = new MetricInterpolation(new XYPointFactory());

        distanceSet = new TreeSet<NicGeoPoint>();
        p1 = new XYPoint();
        p1.setXY(1, 3);
        p2 = new XYPoint();
        p2.setXY(4, 2);
        p3 = new XYPoint();
        p3.setXY(2, 2);
    }

    @Test
    public void testEmptySet() {
        NicGeoPoint dstPoint = interpolator.interpolatePosition(distanceSet);

        assertEquals(0.0, dstPoint.getX());
        assertEquals(0.0, dstPoint.getY());
    }

    @Test
    public void testSinglePoint() {
        p1.setDivergence(4.0f);
        distanceSet.add(p1);

        NicGeoPoint dstPoint = interpolator.interpolatePosition(distanceSet);

        assertEquals(p1.getX(), dstPoint.getX(), delta);
        assertEquals(p1.getY(), dstPoint.getY(), delta);
    }

    @Test
    public void testTwoPoints() {
        p1.setDivergence(0.5f);
        p2.setDivergence(0.3f);
        distanceSet.add(p1);
        distanceSet.add(p2);

        NicGeoPoint dstPoint = interpolator.interpolatePosition(distanceSet);

        assertEquals(2.875f, dstPoint.getX(), delta);
        assertEquals(2.375f, dstPoint.getY(), delta);
    }

    @Test
    public void testThreePoints() {
        p1.setDivergence(0.4f);
        p2.setDivergence(1.1f);
        p3.setDivergence(0.6f);
        distanceSet.add(p1);
        distanceSet.add(p2);
        distanceSet.add(p3);

        NicGeoPoint dstPoint = interpolator.interpolatePosition(distanceSet);

        assertEquals(1.8657f, dstPoint.getX(), delta);
        assertEquals(2.4925f, dstPoint.getY(), delta);
    }
}
