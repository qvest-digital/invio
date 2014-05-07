package de.tarent.nic.entities;


/**
 * A shared NicGeoPoint interface so that the client lib and the apps can communicate with the GeoPoints together.
 *
 * In addition it can contain a "divergence", which is some kind of quality-metric for the point. It makes the point
 * comparable/sortable and it will be included in the equals-test. If it is not needed then just don't set it and
 * everything should work out fine.
 * This divergence can be used to store and compare the quality of a number of points in relation to some common metric.
 * E.g. we use it for the weighted interpolation between a set of points.
 *
 * @author Atanas Alexandrov, tarent solutions GmbH
 * @author Désirée Amling <d.amling@tarent.de>
 */
public interface NicGeoPoint extends Comparable<NicGeoPoint> {

    /**
     * Returns the GeoPoints Latitude in E6 format.
     *
     * @return the E6 Latitude
     */
    int getLatitudeE6();

    /**
     * Set the latitude.
     * @param lat new latitude in E6 format.
     */
    void setLatitudeE6(int lat);

    /**
     * Returns the GeoPoints Longitude in E6 format.
     *
     * @return the E6 longitude
     */
    int getLongitudeE6();

    /**
     * Set the longitude.
     * @param lon new longitude in E6 format.
     */
    void setLongitudeE6(int lon);

    /**
     * Converts the longitude into an x value that is better understood by the tracker. This x value represents the
     * distance from Longitude 0 to the GeoPoints location as a Longitude value. This will also prevent
     * problems because the world is round, not flat and therefore cannot be easily split into even squares.
     *
     * @return the x position on map, measured in meters
     */
    double getX();

    /**
     * Converts the latitude into an y value that is better understood by the tracker. This y value represents the
     * distance from Latitude 0 to the GeoPoints location as a Latitude value. This will also prevent
     * problems because the world is round, not flat and therefore cannot be easily split into even squares.
     *
     * @return the y position on the map, measured in meters
     */
    double getY();

    /**
     * Converts given x and y coordinates into a longitude and a latitude in degree format and sets them.
     * This functionality is contained within a single function because the call order is of the utmost importance as
     * x (the distance to Longitude 0) depends on y (the distance to Latitude 0) resulting in a meaningless x if y is
     * not set.
     *
     * @param x the distance from Longitude 0 in meters
     * @param y the distance from Latitude 0 in meters
     */
    void setXY(final double x, final double y);

    /**
     * Calculates the euclidean distance in two dimensional space between this point and the given point, in meters.
     * @param point another point to calculate distance to
     * @return the distance
     */
    double calculateDistanceTo(final NicGeoPoint point);

    /**
     * Get the divergence-value of this point.
     * @return the divergence; 0 if it is not used.
     */
    double getDivergence();

    /**
     * Set the divergence-value for this point.
     * @param divergence divergence value
     */
    void setDivergence(double divergence);

}
