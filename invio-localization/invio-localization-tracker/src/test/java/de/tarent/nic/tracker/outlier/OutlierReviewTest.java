package de.tarent.nic.tracker.outlier;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.exception.NicTrackerException;
import de.tarent.nic.tracker.geopoint.XYPoint;
import de.tarent.nic.tracker.geopoint.XYPointFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;


public class OutlierReviewTest {

    private CentroidMedianEliminator cme;

    @Before
    public void setup() {
        // variable setup configuration
        cme = new CentroidMedianEliminator(new XYPointFactory(), 2);
    }

    @Test
    public void test1() throws NicTrackerException {
        Set<NicGeoPoint> points = new HashSet<NicGeoPoint>();
        points.add(createXYPoint(498651.86,	5646389.29));
        points.add(createXYPoint(498656.88,	5646395.19));
        points.add(createXYPoint(498652.17,	5646398.64));
        points.add(createXYPoint(498651.45,	5646400.98));
        points.add(createXYPoint(498657.63,	5646382.84));

        Set<NicGeoPoint> outlier = new HashSet<NicGeoPoint>(points);
        outlier.removeAll(cme.removeOutliers(points));
        System.out.println("1 reduced set: "+outlier);
    }

    @Test
    public void test2() throws NicTrackerException {
        Set<NicGeoPoint> points = new HashSet<NicGeoPoint>();
        points.add(createXYPoint(498651.86,5646389.29));
        points.add(createXYPoint(498656.88,5646395.19));
        points.add(createXYPoint(498657.63,5646382.84));
        points.add(createXYPoint(498661.42,5646389.07));
        points.add(createXYPoint(498651.45,5646400.98));

        Set<NicGeoPoint> outlier = new HashSet<NicGeoPoint>(points);
        outlier.removeAll(cme.removeOutliers(points));
        System.out.println("2 reduced set: "+outlier);
    }

    @Test
    public void test3() throws NicTrackerException {
        Set<NicGeoPoint> points = new HashSet<NicGeoPoint>();
        points.add(createXYPoint(498651.86,5646389.29));
        points.add(createXYPoint(498656.88,5646395.19));
        points.add(createXYPoint(498657.63,5646382.84));
        points.add(createXYPoint(498652.17,5646398.64));
        points.add(createXYPoint(498663.47,5646377.16));

        Set<NicGeoPoint> outlier = new HashSet<NicGeoPoint>(points);
        outlier.removeAll(cme.removeOutliers(points));
        System.out.println("3 reduced set: "+outlier);
    }

    @Test
    public void test4() throws NicTrackerException {
        Set<NicGeoPoint> points = new HashSet<NicGeoPoint>();
        points.add(createXYPoint(498651.86,5646389.29));
        points.add(createXYPoint(498656.88,5646395.19));
        points.add(createXYPoint(498657.63,5646382.84));
        points.add(createXYPoint(498661.42,5646389.07));
        points.add(createXYPoint(498654.0, 5646376.49));

        Set<NicGeoPoint> outlier = new HashSet<NicGeoPoint>(points);
        outlier.removeAll(cme.removeOutliers(points));
        System.out.println("4 reduced set: "+outlier);
    }


    @Test
    public void test5() throws NicTrackerException {
        Set<NicGeoPoint> points = new HashSet<NicGeoPoint>();
        points.add(createXYPoint(498651.86,5646389.29));
        points.add(createXYPoint(498651.45,5646400.98));
        points.add(createXYPoint(498656.88,5646395.19));
        points.add(createXYPoint(498657.63,5646382.84));
        points.add(createXYPoint(498660.6,5646411));

        Set<NicGeoPoint> outlier = new HashSet<NicGeoPoint>(points);
        outlier.removeAll(cme.removeOutliers(points));
        System.out.println("5 reduced set: "+outlier);
        System.out.println("5 original   : "+points);
    }



    private NicGeoPoint createXYPoint(double x, double y){
        NicGeoPoint point = new XYPoint();
        point.setXY(x,y);
        return point;
    }
}
