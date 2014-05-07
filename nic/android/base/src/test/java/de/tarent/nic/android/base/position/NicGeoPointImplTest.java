package de.tarent.nic.android.base.position;

import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Test;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import static de.tarent.nic.android.base.position.NicGeoPointImpl.LATITUDE_LINE_DISTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Désirée Amling <d.amling@tarent.de>
 * @author Dino Omanovic <d.omanovic@tarent.de>
 */
public class NicGeoPointImplTest {

    /**
     * Delta is the floatingpoint tolerance for Latitude/Longitude value differences.
     */
    private final double delta = 0.01;


    @Test
    public void testThatNicGeoPointImplHoldsCorrectValuesWhenGivenAnOldGeoPoint() {
        int longitude = 12345;
        int latitude = 54321;
        IGeoPoint geoPoint = new GeoPoint(latitude, longitude);

        NicGeoPoint nicGeoPoint = new NicGeoPointImpl(geoPoint);

        assertEquals(longitude, nicGeoPoint.getLongitudeE6());
        assertEquals(latitude, nicGeoPoint.getLatitudeE6());
    }

    /**
     * 3 different degree tests on the {@link NicGeoPointImpl#getX()} method. (0, 1, 15)
     */
    @Test
    public void testGetXWhenLatitudeAndLongitudeAre0Degrees() {
        NicGeoPointImpl nicGeoPoint = new NicGeoPointImpl(0, 0);

        final double expectedResult = 0;

        assertEquals(expectedResult, nicGeoPoint.getX(), 0);
    }

    @Test
    public void testGetXWhenLatitudeAndLongitudeAre1Degree() {
        NicGeoPoint nicGeoPointDecimal = new NicGeoPointImpl(1.0, 1.0);
        NicGeoPoint nicGeoPointE6 = new NicGeoPointImpl(1000000, 1000000);

        final double expectedResult = 111302.616;

        assertEquals(expectedResult, nicGeoPointDecimal.getX(), delta);
        assertEquals(expectedResult, nicGeoPointE6.getX(), delta);
    }

    @Test
    public void testGetXWhenLatitudeAndLongitudeAre15Degrees() {
        NicGeoPoint nicGeoPointDecimal = new NicGeoPointImpl(15.0, 15.0);
        NicGeoPoint nicGeoPointE6 = new NicGeoPointImpl(15000000, 15000000);

        final double expectedResult = 107550.455 * 15;

        assertEquals(expectedResult, nicGeoPointDecimal.getX(), delta);
        assertEquals(expectedResult, nicGeoPointE6.getX(), delta);
    }

    /**
     * 3 different degree tests on the {@link NicGeoPointImpl#getY()} method. (0, 1, 15)
     */
    @Test
    public void testGetYWhenLatitudeIs0Degrees() throws NoSuchFieldException {
        NicGeoPoint nicGeoPoint = new NicGeoPointImpl(0, 0);

        final double expectedResult = LATITUDE_LINE_DISTANCE * 0;

        assertEquals(expectedResult, nicGeoPoint.getY(), 0);
    }

    @Test
    public void testGetYWhenLatitudeIs1Degree() throws NoSuchFieldException {
        NicGeoPoint nicGeoPointDecimal = new NicGeoPointImpl(1.0, 1.0);
        NicGeoPoint nicGeoPointE6 = new NicGeoPointImpl(1000000, 1000000);

        final double expectedResult = LATITUDE_LINE_DISTANCE * 1;

        assertEquals(expectedResult, nicGeoPointDecimal.getY(), delta);
        assertEquals(expectedResult, nicGeoPointE6.getY(), delta);
    }

    @Test
    public void testGetYWhenLatitudeIs15Degrees() {
        NicGeoPoint nicGeoPointDecimal = new NicGeoPointImpl(15.0, 15.0);
        NicGeoPoint nicGeoPointE6 = new NicGeoPointImpl(15000000, 15000000);

        final double expectedResult = LATITUDE_LINE_DISTANCE * 15;

        assertEquals(expectedResult, nicGeoPointDecimal.getY(), delta);
        assertEquals(expectedResult, nicGeoPointE6.getY(), delta);
    }

    /**
     * Tests both {@link NicGeoPointImpl#getX()} and {@link NicGeoPointImpl#getY()} methods with the exact Bonn
     * coordinates.
     */
    @Test
    public void testGetXAndGetYWhenLatitudeAndLongitudeEqualBonnWorldCoordinates() {
        final double bonnLatitude = 50.733992;
        final double bonnLongitude = 7.099814;

        NicGeoPoint nicGeoPoint = new NicGeoPointImpl(bonnLatitude, bonnLongitude);

        final double expectedY = LATITUDE_LINE_DISTANCE * bonnLatitude;
        final double expectedX = 70598.276 * bonnLongitude;

        assertEquals(expectedY, nicGeoPoint.getY(), delta);
        assertEquals(expectedX, nicGeoPoint.getX(), delta);
    }

    /**
     * Sets the {@link NicGeoPoint} location to the Bonn coordinates and then parses them from X and Y back
     * into Long Lat and then asserts them.
     */
    @Test
    public void testSetXYWithBonnCoordinates() {
        final double bonnLatitude = 50.733992;
        final double bonnLongitude = 7.099814;

        final NicGeoPoint nicGeoPoint = new NicGeoPointImpl(bonnLatitude, bonnLongitude);

        nicGeoPoint.setXY(nicGeoPoint.getX(), nicGeoPoint.getY());

        final int expectedLatitude = (int) (bonnLatitude * 1E6);
        final int expectedLongitude = (int) (bonnLongitude * 1E6);

        assertEquals(expectedLatitude, nicGeoPoint.getLatitudeE6());
        assertEquals(expectedLongitude, nicGeoPoint.getLongitudeE6());
    }

    @Test
    public void testSetXYWithChangingValues() {
        final NicGeoPoint nicGeoPoint = new NicGeoPointImpl(0, 0);

        nicGeoPoint.setXY(1234000, 4321000);

        final int expectedLatitude = 38816025;
        final int expectedLongitude = 14208363;

        assertEquals(expectedLatitude, nicGeoPoint.getLatitudeE6());
        assertEquals(expectedLongitude, nicGeoPoint.getLongitudeE6());
    }

    @Test
    public void testDistanceCalculationWithNormalValues() {

        final NicGeoPoint pointA = new NicGeoPointImpl();
        final NicGeoPoint pointB = new NicGeoPointImpl();

        pointA.setXY(10, 6);
        pointB.setXY(7, 10);

        double expected = 5.0;

        double result = pointA.calculateDistanceTo(pointB);

        assertEquals(expected, result, delta);
    }

    @Test
    public void testDistanceCalculationWithZeros() {

        final NicGeoPoint pointA = new NicGeoPointImpl();
        final NicGeoPoint pointB = new NicGeoPointImpl();

        pointA.setXY(0, 0);
        pointB.setXY(0, 0);

        double expected = 0.0;

        double result = pointA.calculateDistanceTo(pointB);

        assertEquals(expected, result, delta);
    }

    @Test
    public void testEquals() {
        final NicGeoPoint pointA = new NicGeoPointImpl();
        pointA.setXY(100, 200);

        assertTrue(pointA.equals(pointA));
        assertFalse(pointA.equals(null));
        assertFalse(pointA.equals("hallo!?"));

        final NicGeoPoint pointB = new NicGeoPointImpl();

        // Identical Points
        pointB.setXY(100, 200);
        assertTrue(pointA.equals(pointB));
        assertTrue(pointB.equals(pointA));

        // X is different
        pointB.setXY(101, 200);
        assertFalse(pointA.equals(pointB));
        assertFalse(pointB.equals(pointA));

        // Y is different
        pointB.setXY(100, 201);
        assertFalse(pointA.equals(pointB));
        assertFalse(pointB.equals(pointA));

        // Divergence is different.
        pointB.setXY(100, 200);
        pointB.setDivergence(1);
        assertFalse(pointA.equals(pointB));
        assertFalse(pointB.equals(pointA));

        // Identical Points
        pointA.setDivergence(1);
        assertTrue(pointA.equals(pointB));
        assertTrue(pointB.equals(pointA));
    }

    @Test
    public void testToString() {
        final NicGeoPoint nicGeoPoint = new NicGeoPointImpl();
        nicGeoPoint.setXY(1234000, 4321000);
        nicGeoPoint.setDivergence(3.14159);

        String description = nicGeoPoint.toString();
        // We just look for the expected string-fragments, to see if the string-representation contains all that we
        // want.
        assertTrue(description.contains("123"));
        assertTrue(description.contains("432"));
        assertTrue(description.contains("38816025"));
        assertTrue(description.contains("14208363"));
        assertTrue(description.contains("3.14159"));
    }

    @Test
    public void testCompare() {
        final NicGeoPoint pointA = new NicGeoPointImpl(1, 1);
        final NicGeoPoint pointB = new NicGeoPointImpl(0, 0);

        // Note: if the divergence is the same then the result is dependent on the coordinates - but should really
        //       be treated as undefined. We don't care which one comes first in this case! But just don't want the
        //       result to be 0, because that would be inconsistent with equals().
        assertEquals(1, pointA.compareTo(pointB));

        pointA.setDivergence(42);
        pointB.setDivergence(42);
        assertEquals(1, pointA.compareTo(pointB));
        assertEquals(-1, pointB.compareTo(pointA));

        pointA.setDivergence(41);
        assertEquals(-1, pointA.compareTo(pointB));
        assertEquals(1, pointB.compareTo(pointA));

        assertEquals(0, pointA.compareTo(pointA));
    }


}
