package de.tarent.nic.tracker.geopoint;

import de.tarent.nic.entities.NicGeoPoint;


/**
 * XYPoint is an implementatin of the NicGeoPoint interface, much like the NicGeoPointImpl, but without the osmdroid-
 * dependency. It can be used in the server/tracker/whatever. See {@link XYPointFactory}.
 */
public class XYPoint implements NicGeoPoint {

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

    private int latitudeE6;
    private int longitudeE6;

    /**
     * Construct a new XYPoint at (0,0).
     */
    public XYPoint() {
        latitudeE6 = 0;
        longitudeE6 = 0;
    }

    /**
     * Construct a new XYPoint at (x,y).
     *
     * @param x the initial x (lat) coordinate as E6-int
     * @param y the initial y (lon) coordinate as E6-int
     */
    public XYPoint(final int x, final int y) {
        latitudeE6 = x;
        longitudeE6 = y;
    }

    /**
     * Construct a new XYPoint at (x,y).

     * @param x the initial x (lat) coordinate
     * @param y the initial y (lon) coordinate
     */
    public XYPoint(final double x, final double y) {
        latitudeE6 = (int)(x*1E6);
        longitudeE6 = (int)(y*1E6);
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

    @Override
    public int getLatitudeE6() {
        return latitudeE6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLatitudeE6(int latitudeE6) {
        this.latitudeE6 = latitudeE6;
    }

    @Override
    public int getLongitudeE6() {
        return longitudeE6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLongitudeE6(int longitureE6) {
        this.longitudeE6 = longitureE6;
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
        int result = 0;
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
