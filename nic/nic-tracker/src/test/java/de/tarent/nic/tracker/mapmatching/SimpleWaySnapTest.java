package de.tarent.nic.tracker.mapmatching;

import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.XYPoint;
import de.tarent.nic.tracker.geopoint.XYPoint;
import de.tarent.nic.tracker.geopoint.XYPointFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class SimpleWaySnapTest {

    private Set<Edge> edges = new HashSet<Edge>();

    @Before
    public void setup() {
        setupEdges();
    }

    @Test
    public void testThatSnapReturnsTheGeoPointAsItWasWhenEdgesIsNull() {
        Set<Edge> edges = null;
        SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), edges);

        NicGeoPoint geoPointIn = new XYPoint(1000000, 1000000);
        NicGeoPoint geoPointOut = simpleWaySnap.snap(geoPointIn);

        assertEquals(geoPointIn.getLatitudeE6(), geoPointOut.getLatitudeE6());
        assertEquals(geoPointIn.getLongitudeE6(), geoPointOut.getLongitudeE6());
    }

    @Test
    // This point is located so that the nearest point onto the line, on which the edge lies, does NOT lie on
    // the edge, i.e. not between the two points of the edge, but outside of it. It needs to be clipped to one of the
    // points.
    public void testThatSnapReturnsTheCorrectClippedGeoPoints() {
        final SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), edges);

        NicGeoPoint geoPointIn1 = new XYPoint(50720000, 7061000);
        NicGeoPoint geoPointOut1 = simpleWaySnap.snap(geoPointIn1);
        assertEquals(50721722, geoPointOut1.getLatitudeE6());
        assertEquals(7061680, geoPointOut1.getLongitudeE6());
    }

    @Test
    // This point already lies on the edge p1-p2, so it should not be snapped at all.
    public void testThatSnapReturnsTheCorrectGeoPointsOnTheEdge() {
        final SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), edges);

        NicGeoPoint geoPointIn = new XYPoint(50721722, 7061714);
        NicGeoPoint geoPointOut = simpleWaySnap.snap(geoPointIn);
        assertEquals(50721722, geoPointOut.getLatitudeE6());
        assertEquals(7061714, geoPointOut.getLongitudeE6());
    }

    @Test
    public void testThatSnapSnapsToTheLineWhenEitherLatOrLongCrossesALine() {
        SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), edges);

        // Longitude crosses a horizontal line.
        NicGeoPoint geoPointIn1 = new XYPoint(50721752, 7061710);
        NicGeoPoint geoPointOut1 = simpleWaySnap.snap(geoPointIn1);
        assertEquals(50721722, geoPointOut1.getLatitudeE6());
        assertEquals(7061710, geoPointOut1.getLongitudeE6());

        // Latitude crosses a vertical line.
        NicGeoPoint geoPointIn2 = new XYPoint(50722081, 7061520);
        NicGeoPoint geoPointOut2 = simpleWaySnap.snap(geoPointIn2);
        assertEquals(50722081, geoPointOut2.getLatitudeE6());
        assertEquals(7061511, geoPointOut2.getLongitudeE6());
    }

    @Test
    public void testThatSnapSnapsCorrectlyToADiagonalLine() {
        SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), edges);

        NicGeoPoint geoPointIn1 = new XYPoint(50722200, 7061630);
        NicGeoPoint geoPointOut1 = simpleWaySnap.snap(geoPointIn1);
        assertEquals(50722199, geoPointOut1.getLatitudeE6());
        assertEquals(7061635, geoPointOut1.getLongitudeE6());
    }

    @Test
    public void testCornerScenario(){
        Set<Edge> cornerEdges = new HashSet<Edge>();
        cornerEdges.add(new Edge(createXYPoint(1,1), createXYPoint(1,4)));
        cornerEdges.add(new Edge(createXYPoint(1,1), createXYPoint(4,1)));

        SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), cornerEdges);
        NicGeoPoint geoPointIn1 = createXYPoint(2,4);
        NicGeoPoint geoPointOut1 = simpleWaySnap.snap(geoPointIn1);
        NicGeoPoint expected1 = createXYPoint(1,4);

        assertEquals(expected1, geoPointOut1);

        NicGeoPoint geoPointIn2 = createXYPoint(4, 2);
        NicGeoPoint geoPointOut2 = simpleWaySnap.snap(geoPointIn2);
        NicGeoPoint expected2 = createXYPoint(4,1);

        assertEquals(expected2, geoPointOut2);

        NicGeoPoint geoPointIn3 = createXYPoint(2,2);
        NicGeoPoint geoPointOut3 = simpleWaySnap.snap(geoPointIn3);
        NicGeoPoint expected3 = createXYPoint(2,1);

        assertEquals(expected3, geoPointOut3);
    }

    @Test
    public void testJumpToDifferentCorner() {
        Set<Edge> cornerEdges = new HashSet<Edge>();
        cornerEdges.add(new Edge(createXYPoint(0,0), createXYPoint(0,4)));
        cornerEdges.add(new Edge(createXYPoint(0,0), createXYPoint(4,0)));
        cornerEdges.add(new Edge(createXYPoint(4,4), createXYPoint(0,4)));

        SimpleWaySnap simpleWaySnap = new SimpleWaySnap(new XYPointFactory(), cornerEdges);

        // First point is above the third edge, which is NOT part of the tested corner:
        NicGeoPoint geoPointIn1 = createXYPoint(2,5);
        NicGeoPoint geoPointOut1 = simpleWaySnap.snap(geoPointIn1);
        NicGeoPoint expected1 = createXYPoint(2,4);

        assertEquals(expected1, geoPointOut1);

        // Second point is in the corner and should be snapped to any one of the first two edges:
        NicGeoPoint geoPointIn2 = createXYPoint(1,1);
        NicGeoPoint geoPointOut2 = simpleWaySnap.snap(geoPointIn2);
        NicGeoPoint expected2a = createXYPoint(1, 0);
        NicGeoPoint expected2b = createXYPoint(0,1);

        assertTrue(geoPointOut2.equals(expected2a) || geoPointOut2.equals(expected2b));

        // Third time, from the same pointIn again, should then again stay on the last edge/point:
        NicGeoPoint expected3 = geoPointOut2;
        NicGeoPoint geoPointOut3 = simpleWaySnap.snap(geoPointIn2);

        assertEquals(expected3, geoPointOut3);
    }

    private NicGeoPoint createXYPoint(int x, int y){
        NicGeoPoint point = new XYPoint();
        point.setXY(x,y);
        return point;
    }


    private void setupEdges() {
        // Edges from the map "mapMatching_1OG" on 16.12.2013
        // id -38
        edges.add(new Edge(new XYPoint(50.72172210272521, 7.061748586260691), new XYPoint(50.72172210272521, 7.061680103240046)));

        // id -32
        edges.add(new Edge(new XYPoint(50.72180024751303, 7.061749569903855), new XYPoint(50.72172210272521, 7.061748586260691)));
        edges.add(new Edge(new XYPoint(50.72172210272521, 7.061748586260691), new XYPoint(50.72172210272521, 7.061811114236064)));

        // id -28
        edges.add(new Edge(new XYPoint(50.7218006454753, 7.061634589067908), new XYPoint(50.72180024751303, 7.061749569903855)));
        edges.add(new Edge(new XYPoint(50.72180024751303, 7.061749569903855), new XYPoint(50.7218000173249, 7.061816076773792)));

        // id -24
        edges.add(new Edge(new XYPoint(50.72204130042474, 7.061511376957296), new XYPoint(50.72204130042474, 7.061452819012107)));

        // id -20
        edges.add(new Edge(new XYPoint(50.7221223561873, 7.061511376957296), new XYPoint(50.7221223561873, 7.061450833997015)));

        // id -16
        edges.add(new Edge(new XYPoint(50.72212298283619, 7.0616356863825755), new XYPoint(50.7221223561873, 7.061511376957296)));
        edges.add(new Edge(new XYPoint(50.7221223561873, 7.061511376957296), new XYPoint(50.72204130042474, 7.061511376957296)));
        edges.add(new Edge(new XYPoint(50.72204130042474, 7.061511376957296), new XYPoint(50.72204130042474, 7.06158482251567)));

        // id -8
        edges.add(new Edge(new XYPoint(50.722342274216615, 7.061637432908011), new XYPoint(50.72212298283619, 7.0616356863825755)));
        edges.add(new Edge(new XYPoint(50.72212298283619, 7.0616356863825755), new XYPoint(50.7218006454753, 7.061634589067908)));
        edges.add(new Edge(new XYPoint(50.7218006454753, 7.061634589067908), new XYPoint(50.72175917501065, 7.061634447892921)));
    }
}
