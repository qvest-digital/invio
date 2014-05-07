package de.tarent.nic;

import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EdgeTest {

    @Test
    public void testConstruction() {
        NicGeoPoint p1 = mock(NicGeoPoint.class);
        NicGeoPoint p2 = mock(NicGeoPoint.class);
        Edge edge1 = new Edge(p1, p2);

        assertEquals(p1, edge1.getPointA());
        assertEquals(p2, edge1.getPointB());
    }

    @Test
    public void testEquals() {
        NicGeoPoint p1 = mock(NicGeoPoint.class);
        NicGeoPoint p2 = mock(NicGeoPoint.class);
        Edge edge1 = new Edge(p1, p2);
        Edge edge2 = new Edge(p1, p2);
        Edge edge3 = new Edge(p2, p1);
        Edge edge4 = new Edge(p1, p1);
        Edge edge5 = new Edge(p2, p2);
        Edge edge6 = new Edge(p1, null);
        Edge edge6a = new Edge(p1, null);
        Edge edge7 = new Edge(null, p2);
        Edge edge7a = new Edge(null, p2);
        Edge edge8 = new Edge(null, null);
        Edge edge8a = new Edge(null, null);
        Edge edge9 = new Edge(null, p1);
        Edge edge10 = new Edge(p2, null);

        assertTrue(edge1.equals(edge1));
        assertTrue(edge1.equals(edge2));
        assertTrue(edge6.equals(edge6a));
        assertTrue(edge7.equals(edge7a));
        assertTrue(edge8.equals(edge8a));

        assertFalse(edge1.equals(null));
        assertFalse(edge1.equals(this));
        assertFalse(edge1.equals(edge4));
        assertFalse(edge1.equals(edge5));
        assertFalse(edge1.equals(edge6));
        assertFalse(edge1.equals(edge7));
        assertFalse(edge1.equals(edge8));
        assertFalse(edge8.equals(edge1));
        assertFalse(edge9.equals(edge7));
        assertFalse(edge7.equals(edge9));
        assertFalse(edge6.equals(edge10));
        assertFalse(edge10.equals(edge6));

        // TODO: fix this in Edge, the order of points should be irrelevant!
        // assertTrue(edge1.equals(edge3));
    }

    @Test
    public void testHashCode() {
        NicGeoPoint p1 = mock(NicGeoPoint.class);
        NicGeoPoint p2 = mock(NicGeoPoint.class);
        Edge edge1 = new Edge(p1, p2);
        Edge edge2 = new Edge(p1, p2);
        Edge edge3 = new Edge(p2, p1);
        Edge edge4 = new Edge(p1, p1);
        Edge edge5 = new Edge(p2, p2);
        Edge edge6 = new Edge(p1, null);
        Edge edge7 = new Edge(null, p2);

        assertEquals(edge1.hashCode(), edge2.hashCode());
        assertEquals(edge1.hashCode(), edge2.hashCode());
        assertFalse(edge1.hashCode() == edge4.hashCode());
        assertFalse(edge1.hashCode() == edge5.hashCode());
        assertFalse(edge6.hashCode() == edge1.hashCode());
        assertFalse(edge7.hashCode() == edge1.hashCode());
        assertFalse(edge7.hashCode() == edge6.hashCode());

        // TODO: fix this in Edge, the order of points should be irrelevant!
        //assertEquals(edge1.hashCode(), edge3.hashCode());
    }
}
