package de.tarent.nic.android.base.position;

import de.tarent.nic.entities.NicGeoPoint;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

/**
 * The NicGeoPointImpl combines the GeoPoint (from osmdroid) with the NicGeoPoint, which is a platform-independent
 * interface. That way we have a proper implementation here, for the tracker/apps, but the client-lib can deal with
 * fingerprints in a way that is not dependent on android-classes (by not using this class, but XYPoint instead, through
 * the appropriate PointFactory).
 */
public class NicGeoPointImpl extends GeoPoint implements NicGeoPoint {

    /**
     * The static distance in meters between two lines of latitude.
     */
    protected static final double LATITUDE_LINE_DISTANCE = 111320;

    /**
     * Three constant values are used in the calculation because of the earth's irregular (not completely spherical)
     * shape.
     * <p/>
     * Calculation source: http://www.nymanz.org/sandcollection/swaplist/Latitude%20and%20Longitude.pdf
     */
    private static final double[] EARTH_SHAPE_CONSTANTS = {111412.84, -93.5, 0.118};

    /**
     * The divergence that is assigned to this point. It is used to describe the quality of this point, for neighbour-
     * ordering and interpolation.
     */
    private double divergence;


    /**
     * Construct a new NicGeoPointImpl at (0,0).
     */
    public NicGeoPointImpl() {
        super(0, 0);
    }

    /**
     * Construct a new NicGeoPointImpl from an existing IGeoPoint.
     *
     * @param geoPoint the geoPoint whose coordinates we want to wrap.
     */
    public NicGeoPointImpl(final IGeoPoint geoPoint) {
        super(geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6());
    }

    /**
     * Construct a new NicGeoPointImpl from individual coordinates.
     *
     * @param latitudeE6  the latitude in E6 format
     * @param longitudeE6 the longitude in E6 format
     */
    public NicGeoPointImpl(final int latitudeE6, final int longitudeE6) {
        super(latitudeE6, longitudeE6);
    }

    /**
     * Constructor.
     *
     * @param latitude  the latitude in degrees format (-90 to 90)
     * @param longitude the longitude in degrees format (-180 to 180)
     */
    public NicGeoPointImpl(final double latitude, final double longitude) {
        super(latitude, longitude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getX() {
        /*
         * Because the longitude is saved in E6 format, it needs to be converted back to its degree format before being
         * processed.
         */
        final double longitude = convertE6ToDegrees(getLongitudeE6());

        final double x = longitude * getLongitudeDistanceInMeters();

        return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getY() {
       /*
        * Because the latitude is saved in E6 format, it needs to be converted back to its degree format before being
        * processed.
        */
        final double latitude = convertE6ToDegrees(getLatitudeE6());
        final double y = latitude * LATITUDE_LINE_DISTANCE;
        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setXY(final double x, final double y) {
       /*
        * IMPORTANT: setY() MUST be called BEFORE setX()!!!
        */
        setY(y);
        setX(x);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateDistanceTo(final NicGeoPoint point) {
        final double dx = point.getX() - this.getX();
        final double dy = point.getY() - this.getY();

        final double distance = Math.sqrt((dx * dx) + (dy * dy));
        return distance;
    }

    @Override
    public double getDivergence() {
        return divergence;
    }

    @Override
    public void setDivergence(double divergence) {
        this.divergence = divergence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof NicGeoPoint)) {
            return false;
        }
        final NicGeoPoint nicGeoPoint = (NicGeoPoint) o;

        // Y needs to be tested before X because X changes based on the value of Y. So if Y is not the same, X will
        // likely not be the same either.
        return (((getY() == nicGeoPoint.getY()) &&
                 (getX() == nicGeoPoint.getX()) &&
                 (divergence == nicGeoPoint.getDivergence())));
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(divergence);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getX());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * If the divergence of two points is the same than the result should be treated as undefined (but stable)!
     * {@inheritDoc}
     */
    @Override
    public int compareTo(NicGeoPoint other) {
        if (this.divergence > other.getDivergence()) {
            return 1;
        }
        if (this.divergence < other.getDivergence()) {
            return -1;
        }
        // When the divergence is the same we select an arbitrary order based on the coordinates, in order to be
        // consistent with equals (i.e. compareTo should not return 0 when equals returns false).
        final long a = (getLatitudeE6() + (((long)getLongitudeE6()) << 32));
        final long b = (other.getLatitudeE6() + (((long)other.getLongitudeE6()) << 32));
        if (a > b) {
            return 1;
        }
        if (a < b) {
            return -1;
        }

        return 0;
    }

    @Override
    public String toString() {
        return "LatLonE6: "+this.getLatitudeE6()+", "+this.getLongitudeE6()+"; XY: "+this.getX()+", "+this.getY()+"; " +
                "Divergence: "+this.getDivergence();
    }


    private void setX(final double x) {
        final double longitude = x / getLongitudeDistanceInMeters();
        final int longitudeE6 = convertDegreesToE6(longitude);

        setLongitudeE6(longitudeE6);
    }

    private void setY(final double y) {
        final double latitude = y / LATITUDE_LINE_DISTANCE;
        final int latitudeE6 = convertDegreesToE6(latitude);

        setLatitudeE6(latitudeE6);
    }

    private double convertE6ToDegrees(final int e6) {
        final double degrees = e6 / 1E6;

        return degrees;
    }

    private int convertDegreesToE6(final double degrees) {
        final int e6 = (int) (degrees * 1E6);

        return e6;
    }

    private double getLatitudeInRadians() {
        final double latitude = convertE6ToDegrees(getLatitudeE6());

        return Math.toRadians(latitude);
    }

    private double getLongitudeDistanceInMeters() {
        final double latitudeInRadians = getLatitudeInRadians();

        final double longitudeDistanceInMeters = (EARTH_SHAPE_CONSTANTS[0] * Math.cos(latitudeInRadians)) +
                (EARTH_SHAPE_CONSTANTS[1] * Math.cos(3 * latitudeInRadians)) +
                (EARTH_SHAPE_CONSTANTS[2] * Math.cos(5 * latitudeInRadians));

        return longitudeDistanceInMeters;
    }

}
