package de.tarent.nic.tracker.geopoint;

import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PointListTest {

    private Set<NicGeoPoint> pointsLatLon;

    private Set<NicGeoPoint> pointsXY;

    private NicGeoPoint onePoint;

    private double delta = 0.25;

    @Before
    public void setup() {
        pointsLatLon = new HashSet<NicGeoPoint>();
        onePoint = new XYPoint(50002000, 7003000);
        pointsLatLon.add(onePoint);
        pointsLatLon.add(new XYPoint(50001000, 7000000));
        pointsLatLon.add(new XYPoint(50000000, 7004000));

        pointsXY = new HashSet<NicGeoPoint>();
        NicGeoPoint ngp1 = new XYPoint();
        ngp1.setXY(2,5);
        pointsXY.add(ngp1);
        NicGeoPoint ngp2 = new XYPoint();
        ngp2.setXY(4,15);
        pointsXY.add(ngp2);

    }

    @Test
    public void testBasics() {
        PointList pl = new PointList(new XYPointFactory());
        assertEquals(0, pl.size());

        pl.add(new XYPoint(1,1));
        assertEquals(1, pl.size());

        pl = new PointList(new XYPointFactory(), pointsLatLon);
        assertEquals(3, pl.size());
    }

    @Test
    public void testSortedXYLists() {
        PointList pl = new PointList(new XYPointFactory(), pointsLatLon);

        List<Double> x = pl.getXValues();

        assertTrue(x.get(0) < x.get(1));
        assertTrue(x.get(1) < x.get(2));

        assertEquals(501859.67, x.get(0), delta);
        assertEquals(502064.34, x.get(1), delta);
        assertEquals(502156.87, x.get(2), delta);

        List<Double> y = pl.getYValues();
        assertTrue(y.get(0) < y.get(1));
        assertTrue(y.get(1) < y.get(2));
        assertEquals(5566000.00, y.get(0), delta);
        assertEquals(5566111.32, y.get(1), delta);
        assertEquals(5566222.64, y.get(2), delta);
    }

    @Test
    public void testThatCalculateMedianReferencePointDistanceReturnsTheCorrectCalculatedDistance() {
        PointList pl = new PointList(new XYPointFactory(), pointsXY);

        NicGeoPoint referencePoint = new XYPoint();
        referencePoint.setXY(3, 10);

        assertEquals(5.1, pl.calculateMedianReferencePointDistance(referencePoint), delta);
    }

    @Test
    public void testMedian() {
        PointList pl = new PointList(new XYPointFactory(), pointsLatLon);

        NicGeoPoint median = pl.getMedianPoint();

        assertEquals(502064.34, median.getX(), delta);
        assertEquals(5566111.32, median.getY(), delta);

        assertEquals(5566111.32, pl.getMedianY(), delta);
        assertEquals(502064.34, pl.getMedianX(), delta);

        pl.add(new XYPoint(0, 0));
        assertEquals((501859.67 + 502064.34)/2, pl.getMedianX(), delta);
        assertEquals((5566000.00 + 5566111.32)/2,  pl.getMedianY(), delta);
    }

    // Here we call all the inherited methods that change something to see if the (cached) x/y-lists are recalculated
    // correctly. (We do this mostly by checking the size.)
    @Test
    public void testDirty() {
        PointList pl = new PointList(new XYPointFactory(), pointsLatLon);
        assertEquals(3, pl.getXValues().size());
        assertEquals(3, pl.getYValues().size());

        pl.add(2, new XYPoint());
        assertEquals(4, pl.getYValues().size());
        assertEquals(4, pl.getXValues().size());

        pl.addAll(new ArrayList<NicGeoPoint>() {{
            add(new XYPoint(0,0));
            add(new XYPoint(1,1));
        }});
        assertEquals(6, pl.getXValues().size());
        assertEquals(6, pl.getYValues().size());

        pl.addAll(3, new ArrayList<NicGeoPoint>() {{
            add(new XYPoint(1,1));
        }});
        assertEquals(7, pl.getXValues().size());
        assertEquals(7, pl.getYValues().size());

        pl.remove(5);
        assertEquals(6, pl.getXValues().size());
        assertEquals(6, pl.getYValues().size());

        pl.remove(onePoint);
        assertEquals(5, pl.getXValues().size());
        assertEquals(5, pl.getYValues().size());

        pl.add(onePoint);
        assertEquals(6, pl.getXValues().size());
        assertEquals(6, pl.getYValues().size());

        pl.removeAll(new ArrayList<NicGeoPoint>() {{
            add(onePoint);
        }});
        assertEquals(5, pl.getXValues().size());
        assertEquals(5, pl.getYValues().size());

        pl.set(0, onePoint);
        assertEquals(5, pl.getXValues().size());
        assertEquals(5, pl.getYValues().size());
        List<Double> x = pl.getXValues();
        assertEquals(0, x.get(0), delta); // just a quick sort-check :-)

        pl.retainAll(new ArrayList<NicGeoPoint>() {{
            add(onePoint);
        }});
        assertEquals(1, pl.getXValues().size());
        assertEquals(1, pl.getYValues().size());

        pl.removeRange(0, 1);
        assertEquals(0, pl.getXValues().size());
        assertEquals(0, pl.getYValues().size());

        pl.add(onePoint);
        assertEquals(1, pl.getXValues().size());
        assertEquals(1, pl.getYValues().size());

        pl.clear();
        assertEquals(0, pl.getXValues().size());
        assertEquals(0, pl.getYValues().size());
    }

    @Test
    public void testAverage() {
        PointList pl = new PointList(new XYPointFactory(), pointsLatLon);
        assertEquals((502156.86 + 501859.67+502064.34)/3, pl.getAverageX(), delta);
        assertEquals((5566000.00 + 5566111.32+5566222.64)/3,  pl.getAverageY(), delta);

        NicGeoPoint averagePoint = pl.getAveragePoint();
        assertEquals((502156.86 + 501859.67+502064.34)/3, averagePoint.getX(), delta);
        assertEquals((5566000.00 + 5566111.32+5566222.64)/3, averagePoint.getY(), delta);
    }

}
